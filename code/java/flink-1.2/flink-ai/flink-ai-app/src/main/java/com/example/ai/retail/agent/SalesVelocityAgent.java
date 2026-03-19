package com.example.ai.retail.agent;

import com.example.ai.retail.dto.SaleEvent;
import com.example.ai.retail.dto.SkuVelocityResult;
import com.fasterxml.jackson.databind.DeserializationFeature;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Agent 1 in the inventory reorder pipeline.
 *
 * Receives a SaleEvent and uses the LLM to assess the current sales velocity
 * and estimate how many days until stockout. The output SkuVelocityResult is
 * keyed by skuId for downstream window aggregation into a SkuWindowSummary.
 *
 * Uses the Workflow pattern: @Prompt defines system + user message template,
 * and the input handler injects the serialized SaleEvent as {input}.
 */
public class SalesVelocityAgent extends Agent {

    private static final ObjectMapper MAPPER =
            new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    // Inventory analyst persona with a concrete input/output example to guide the LLM
    private static final String SYSTEM_PROMPT =
            "You are an inventory analyst. Given a sale event, assess the current sales " +
            "velocity and estimate how many days until stockout.\n\n" +
            "Input format:\n" +
            "{\"skuId\": \"SKU-001\", \"productName\": \"Wireless Headphones\", " +
            "\"quantitySold\": 50, \"currentStock\": 120, \"warehouseId\": \"WH-East\"}\n\n" +
            "Respond ONLY with valid JSON:\n" +
            "{\"skuId\": \"SKU-001\", \"productName\": \"Wireless Headphones\", " +
            "\"salesVelocity\": \"HIGH\", \"estimatedDaysToStockout\": 2, " +
            "\"stockConcerns\": [\"stock below safety threshold\", \"high sell-through rate\"]}";

    @Prompt
    public static org.apache.flink.agents.api.prompt.Prompt salesVelocityPrompt() {
        return org.apache.flink.agents.api.prompt.Prompt.fromMessages(
                Arrays.asList(
                        new ChatMessage(MessageRole.SYSTEM, SYSTEM_PROMPT),
                        new ChatMessage(MessageRole.USER, "{input}")));
    }

    // No tools needed — this agent only reads and classifies, no side effects
    @ChatModelSetup
    public static ResourceDescriptor salesVelocityModel() {
        return ResourceDescriptor.Builder.newBuilder(ResourceName.ChatModel.OPENAI_SETUP)
                .addInitialArgument("connection", "groqConnection")
                .addInitialArgument("model", "llama-3.3-70b-versatile")
                .addInitialArgument("prompt", "salesVelocityPrompt")
                .build();
    }

    // --- INPUT ACTION ---

    /**
     * Deserializes the SaleEvent, stores skuId and productName in short-term memory,
     * and dispatches a ChatRequestEvent with the event JSON as {input}.
     */
    @Action(listenEvents = {InputEvent.class})
    public static void processInput(InputEvent event, RunnerContext ctx) throws Exception {
        SaleEvent saleEvent = (SaleEvent) event.getInput();

        // Store identifiers — the response handler uses these as source of truth
        ctx.getShortTermMemory().set("skuId", saleEvent.getSkuId());
        ctx.getShortTermMemory().set("productName", saleEvent.getProductName());

        String payload = MAPPER.writeValueAsString(saleEvent);

        // Inject payload into the {input} placeholder defined in salesVelocityPrompt
        ChatMessage msg = new ChatMessage(MessageRole.USER, "", Map.of("input", payload));
        ctx.sendEvent(new ChatRequestEvent("salesVelocityModel", List.of(msg)));
    }

    // --- RESPONSE ACTION ---

    /**
     * Parses the LLM's velocity assessment and emits a SkuVelocityResult
     * for the downstream tumbling window keyed by skuId.
     */
    @Action(listenEvents = ChatResponseEvent.class)
    public static void processChatResponse(ChatResponseEvent event, RunnerContext ctx) throws Exception {
        JsonNode root = MAPPER.readTree(event.getResponse().getContent());

        JsonNode velocityNode = root.findValue("salesVelocity");
        JsonNode daysNode = root.findValue("estimatedDaysToStockout");
        JsonNode concernsNode = root.findValue("stockConcerns");

        if (velocityNode == null || daysNode == null || concernsNode == null) {
            throw new IllegalStateException(
                    "LLM response missing required fields: " + event.getResponse().getContent());
        }

        List<String> stockConcerns = new ArrayList<>();
        if (concernsNode.isArray()) {
            for (JsonNode concern : concernsNode) {
                stockConcerns.add(concern.asText());
            }
        }

        // Use memory values rather than LLM-echoed values for skuId/productName
        String skuId = (String) ctx.getShortTermMemory().get("skuId").getValue();
        String productName = (String) ctx.getShortTermMemory().get("productName").getValue();

        SkuVelocityResult result = new SkuVelocityResult(
                skuId,
                productName,
                velocityNode.asText(),
                daysNode.asInt(),
                Collections.unmodifiableList(stockConcerns));

        ctx.sendEvent(new OutputEvent(result));
    }
}
