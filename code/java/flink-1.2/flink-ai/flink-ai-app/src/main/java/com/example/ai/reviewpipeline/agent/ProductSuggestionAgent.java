package com.example.ai.reviewpipeline.agent;

import com.example.ai.reviewpipeline.dto.ProductSuggestion;
import com.example.ai.reviewpipeline.dto.ReviewWindowSummary;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.agents.api.InputEvent;
import org.apache.flink.agents.api.OutputEvent;
import org.apache.flink.agents.api.agents.Agent;
import org.apache.flink.agents.api.annotation.Action;
import org.apache.flink.agents.api.annotation.ChatModelSetup;
import org.apache.flink.agents.api.annotation.Prompt;
import org.apache.flink.agents.api.chat.messages.ChatMessage;
import org.apache.flink.agents.api.chat.messages.MessageRole;
import org.apache.flink.agents.api.context.RunnerContext;
import org.apache.flink.agents.api.event.ChatRequestEvent;
import org.apache.flink.agents.api.event.ChatResponseEvent;
import org.apache.flink.agents.api.resource.ResourceDescriptor;
import org.apache.flink.agents.api.resource.ResourceName;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Agent 2 in the review pipeline.
 *
 * Receives a ReviewWindowSummary (aggregated score histogram + all dissatisfaction
 * reasons from a 1-minute tumbling window) and calls Groq to generate three
 * actionable product improvement suggestions.
 *
 * The output (ProductSuggestion) is sinked to the Kafka topic: product-suggestions.
 */
public class ProductSuggestionAgent extends Agent {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String PROMPT_TEMPLATE =
            "You are a product improvement advisor.\n\n"
            + "Given a product's rating distribution and user dissatisfaction reasons, "
            + "generate exactly 3 short, actionable improvement suggestions.\n\n"
            + "Input format:\n"
            + "{\"id\": \"B001\", \"score_histogram\": [\"10%\",\"20%\",\"10%\",\"15%\",\"45%\"], "
            + "\"unsatisfied_reasons\": [\"poor build quality\", \"hard to use\"]}\n\n"
            + "Respond ONLY with valid JSON matching this exact structure:\n"
            + "{\"suggestion_list\": [\"suggestion1\", \"suggestion2\", \"suggestion3\"]}\n\n"
            + "Input:\n{input}";

    @Prompt
    public static org.apache.flink.agents.api.prompt.Prompt productSuggestionPrompt() {
        return org.apache.flink.agents.api.prompt.Prompt.fromText(PROMPT_TEMPLATE);
    }

    @ChatModelSetup
    public static ResourceDescriptor suggestionModel() {
        return ResourceDescriptor.Builder.newBuilder(ResourceName.ChatModel.OPENAI_SETUP)
                .addInitialArgument("connection", "groqConnection")
                .addInitialArgument("model", "llama-3.3-70b-versatile")
                .addInitialArgument("prompt", "productSuggestionPrompt")
                .build();
    }

    /** Receives windowed summary JSON and sends a chat request to Groq. */
    @Action(listenEvents = {InputEvent.class})
    public static void processInput(InputEvent event, RunnerContext ctx) throws Exception {
        String input = (String) event.getInput();
        ReviewWindowSummary summary = MAPPER.readValue(input, ReviewWindowSummary.class);

        ctx.getShortTermMemory().set("id", summary.getId());
        ctx.getShortTermMemory().set("scoreHistogram", summary.getScoreHistogram());

        String payload = String.format(
                "{\"id\": \"%s\", \"score_histogram\": %s, \"unsatisfied_reasons\": %s}",
                summary.getId(),
                MAPPER.writeValueAsString(summary.getScoreHistogram()),
                MAPPER.writeValueAsString(summary.getUnsatisfiedReasons()));

        ChatMessage msg = new ChatMessage(MessageRole.USER, "", Map.of("input", payload));
        ctx.sendEvent(new ChatRequestEvent("suggestionModel", List.of(msg)));
    }

    /** Parses the LLM JSON response and emits a ProductSuggestion. */
    @Action(listenEvents = {ChatResponseEvent.class})
    public static void processChatResponse(ChatResponseEvent event, RunnerContext ctx) throws Exception {
        JsonNode root = MAPPER.readTree(event.getResponse().getContent());
        JsonNode suggestionsNode = root.findValue("suggestion_list");

        if (suggestionsNode == null || !suggestionsNode.isArray()) {
            throw new IllegalStateException(
                    "LLM response missing 'suggestion_list': " + event.getResponse().getContent());
        }

        List<String> suggestions = new ArrayList<>();
        for (JsonNode node : suggestionsNode) {
            suggestions.add(node.asText());
        }

        String productId = ctx.getShortTermMemory().get("id").getValue().toString();
        @SuppressWarnings("unchecked")
        List<String> scoreHistogram = (List<String>) ctx.getShortTermMemory().get("scoreHistogram").getValue();

        ctx.sendEvent(new OutputEvent(new ProductSuggestion(productId, scoreHistogram, suggestions)));
    }
}
