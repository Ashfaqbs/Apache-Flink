package com.example.ai.reviewpipeline.agent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.flink.agents.api.InputEvent;
import org.apache.flink.agents.api.OutputEvent;
import org.apache.flink.agents.api.agents.Agent;
import org.apache.flink.agents.api.annotation.Action;
import org.apache.flink.agents.api.annotation.ChatModelSetup;
import org.apache.flink.agents.api.annotation.Prompt;
import org.apache.flink.agents.api.annotation.Tool;
import org.apache.flink.agents.api.annotation.ToolParam;
import org.apache.flink.agents.api.chat.messages.ChatMessage;
import org.apache.flink.agents.api.chat.messages.MessageRole;
import org.apache.flink.agents.api.context.RunnerContext;
import org.apache.flink.agents.api.event.ChatRequestEvent;
import org.apache.flink.agents.api.event.ChatResponseEvent;
import org.apache.flink.agents.api.resource.ResourceDescriptor;
import org.apache.flink.agents.api.resource.ResourceName;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.types.Row;

import com.example.ai.reviewpipeline.dto.ReviewAnalysisResult;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Agent 1 in the review pipeline.
 *
 * Receives product reviews from the Table API (as Flink Rows), calls Groq to
 * extract a satisfaction score (1-5) and reasons for dissatisfaction, and emits
 * a ReviewAnalysisResult downstream.
 *
 * Also has a tool: if the review mentions a shipping issue, the LLM will call
 * notifyShippingManager before producing its structured output.
 */
public class ReviewAnalysisAgent extends Agent {

    private static final ObjectMapper MAPPER =
            new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private static final String SYSTEM_PROMPT =
            "Analyze the product review and extract a satisfaction score (1-5) and potential "
            + "reasons for dissatisfaction.\n\n"
            + "Input format:\n"
            + "{\"id\": \"B001\", \"review\": \"Broke after one week. Very poor quality.\"}\n\n"
            + "Respond ONLY with valid JSON matching this exact structure:\n"
            + "{\"id\": \"B001\", \"score\": 1, \"reasons\": [\"poor quality\"]}\n\n"
            + "If the review mentions a shipping or delivery problem, call the "
            + "notifyShippingManager tool before producing your JSON output.";

    /** Key selector used when reading from Table API — keys by product id. */
    public static class RowKeySelector implements KeySelector<Object, String> {
        @Override
        public String getKey(Object value) {
            if (value instanceof Row row) {
                return (String) row.getField("id");
            }
            return "";
        }
    }

    @Prompt
    public static org.apache.flink.agents.api.prompt.Prompt reviewAnalysisPrompt() {
        return org.apache.flink.agents.api.prompt.Prompt.fromMessages(
                Arrays.asList(
                        new ChatMessage(MessageRole.SYSTEM, SYSTEM_PROMPT),
                        new ChatMessage(MessageRole.USER, "{input}")));
    }

    @ChatModelSetup
    public static ResourceDescriptor reviewAnalysisModel() {
        return ResourceDescriptor.Builder.newBuilder(ResourceName.ChatModel.OPENAI_SETUP)
                .addInitialArgument("connection", "groqConnection")
                .addInitialArgument("model", "llama-3.3-70b-versatile")
                .addInitialArgument("prompt", "reviewAnalysisPrompt")
                .addInitialArgument("tools", Collections.singletonList("notifyShippingManager"))
                .build();
    }

    /**
     * Tool invoked by the LLM when a review contains a shipping complaint.
     * In a real system this would call a Slack webhook, PagerDuty, or email service.
     */
    @Tool(description = "Notify the shipping team when a review explicitly complains about shipping or delivery damage.")
    public static void notifyShippingManager(
            @ToolParam(name = "productId") String productId,
            @ToolParam(name = "reviewContent") String reviewContent) {
        System.out.printf(
                "[SHIPPING ALERT] Product %s received a shipping complaint: %s%n",
                productId, reviewContent);
    }

    /** Receives a Row from Table API and sends a chat request to Groq. */
    @Action(listenEvents = {InputEvent.class})
    public static void processInput(InputEvent event, RunnerContext ctx) throws Exception {
        Row row = (Row) event.getInput();
        String productId = (String) row.getField("id");
        String reviewText = (String) row.getField("review");

        ctx.getShortTermMemory().set("id", productId);

        String payload = String.format("{\"id\": \"%s\", \"review\": \"%s\"}", productId, reviewText);
        ChatMessage msg = new ChatMessage(MessageRole.USER, "", Map.of("input", payload));
        ctx.sendEvent(new ChatRequestEvent("reviewAnalysisModel", List.of(msg)));
    }

    /** Parses the LLM JSON response and emits a ReviewAnalysisResult. */
    @Action(listenEvents = ChatResponseEvent.class)
    public static void processChatResponse(ChatResponseEvent event, RunnerContext ctx) throws Exception {
        JsonNode root = MAPPER.readTree(event.getResponse().getContent());
        JsonNode scoreNode = root.findValue("score");
        JsonNode reasonsNode = root.findValue("reasons");

        if (scoreNode == null || reasonsNode == null) {
            throw new IllegalStateException(
                    "LLM response missing 'score' or 'reasons': " + event.getResponse().getContent());
        }

        List<String> reasons = new ArrayList<>();
        if (reasonsNode.isArray()) {
            for (JsonNode node : reasonsNode) {
                reasons.add(node.asText());
            }
        }

        String productId = ctx.getShortTermMemory().get("id").getValue().toString();
        ctx.sendEvent(new OutputEvent(new ReviewAnalysisResult(productId, scoreNode.asInt(), reasons)));
    }
}
