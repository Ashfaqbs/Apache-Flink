package com.example.ai.retail.agent;

import com.example.ai.retail.dto.ReorderRecommendation;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Agent 2 in the inventory reorder pipeline.
 *
 * Receives a JSON-serialized SkuWindowSummary (aggregated from a 5-minute tumbling
 * window) and uses the LLM to generate a restock recommendation with a suggested
 * order quantity and urgency level.
 *
 * If urgency is CRITICAL or HIGH, the LLM calls the notifyProcurement and
 * sendRestockAlert tools before producing its JSON output.
 */
public class ReorderAgent extends Agent {

    private static final ObjectMapper MAPPER =
            new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    // Supply chain manager persona with concrete example and tool instruction
    private static final String SYSTEM_PROMPT =
            "You are a supply chain manager. Based on aggregated sales velocity data for a SKU " +
            "within a time window, generate a reorder recommendation with a suggested order quantity.\n\n" +
            "Input format:\n" +
            "{\"skuId\": \"SKU-001\", \"productName\": \"Wireless Headphones\", " +
            "\"warehouseId\": \"WH-East\", \"totalUnitsSold\": 200, \"avgDaysToStockout\": 2.5, " +
            "\"minStockSeen\": 45, \"allConcerns\": [\"high sell-through\", \"below safety stock\"]}\n\n" +
            "If urgency is CRITICAL or HIGH, you MUST call the notifyProcurement tool before " +
            "producing output.\n\n" +
            "Respond ONLY with valid JSON:\n" +
            "{\"urgency\": \"CRITICAL\", \"suggestedOrderQuantity\": 500, " +
            "\"recommendations\": [\"immediate reorder required\", " +
            "\"consider safety stock increase\"]}";

    @Prompt
    public static org.apache.flink.agents.api.prompt.Prompt reorderPrompt() {
        return org.apache.flink.agents.api.prompt.Prompt.fromMessages(
                Arrays.asList(
                        new ChatMessage(MessageRole.SYSTEM, SYSTEM_PROMPT),
                        new ChatMessage(MessageRole.USER, "{input}")));
    }

    // Both procurement tools registered so the LLM can call them when urgency is high
    @ChatModelSetup
    public static ResourceDescriptor reorderModel() {
        return ResourceDescriptor.Builder.newBuilder(ResourceName.ChatModel.OPENAI_SETUP)
                .addInitialArgument("connection", "groqConnection")
                .addInitialArgument("model", "llama-3.3-70b-versatile")
                .addInitialArgument("prompt", "reorderPrompt")
                .addInitialArgument("tools", List.of("notifyProcurement", "sendRestockAlert"))
                .build();
    }

    // --- TOOLS ---

    /**
     * Notifies the procurement team to initiate a supplier purchase order.
     * Called by the LLM when urgency is CRITICAL or HIGH.
     */
    @Tool(description = "Notify the procurement team to place an urgent reorder for a SKU. Call when urgency is CRITICAL or HIGH.")
    public static void notifyProcurement(
            @ToolParam(name = "skuId") String skuId,
            @ToolParam(name = "productName") String productName,
            @ToolParam(name = "urgency") String urgency,
            @ToolParam(name = "suggestedQty") String suggestedQty) {
        System.out.printf("[PROCUREMENT] SKU: %s (%s) | Urgency: %s | Qty: %s%n",
                skuId, productName, urgency, suggestedQty);
        // Replace with: send email to procurement@company.com via JavaMail, or POST to ERP
        // system (SAP/Oracle) reorder API, or publish to an internal procurement Kafka topic — e.g.:
        //   Properties props = new Properties();
        //   props.put("mail.smtp.host", "smtp.company.com");
        //   Session session = Session.getInstance(props);
        //   MimeMessage message = new MimeMessage(session);
        //   message.setFrom(new InternetAddress("flink-pipeline@company.com"));
        //   message.addRecipient(Message.RecipientType.TO,
        //       new InternetAddress("procurement@company.com"));
        //   message.setSubject("[" + urgency + "] Reorder Required: " + productName);
        //   message.setText("SKU: " + skuId + " | Suggested Qty: " + suggestedQty);
        //   Transport.send(message);
    }

    /**
     * Alerts the warehouse manager about a stock emergency via direct notification.
     * Called alongside notifyProcurement so warehouse staff can prepare receiving.
     */
    @Tool(description = "Send a restock alert to the warehouse manager for a specific warehouse. Call alongside notifyProcurement for CRITICAL/HIGH urgency.")
    public static void sendRestockAlert(
            @ToolParam(name = "warehouseId") String warehouseId,
            @ToolParam(name = "skuId") String skuId,
            @ToolParam(name = "message") String message) {
        System.out.printf("[WAREHOUSE ALERT] Warehouse: %s | SKU: %s | Message: %s%n",
                warehouseId, skuId, message);
        // Replace with: send SMS via Twilio API to warehouse manager's phone, or push notification
        // via FCM, or POST to warehouse management system (WMS) webhook — e.g.:
        //   HttpClient.newHttpClient().send(
        //       HttpRequest.newBuilder()
        //           .uri(URI.create("https://wms.company.com/api/v1/alerts"))
        //           .header("Content-Type", "application/json")
        //           .header("X-Api-Key", System.getenv("WMS_API_KEY"))
        //           .POST(HttpRequest.BodyPublishers.ofString(
        //               "{\"warehouseId\":\"" + warehouseId + "\",\"skuId\":\"" + skuId +
        //               "\",\"message\":\"" + message + "\"}"))
        //           .build(),
        //       HttpResponse.BodyHandlers.ofString());
    }

    // --- INPUT ACTION ---

    /**
     * Receives the JSON window summary, stores SKU metadata in short-term memory,
     * and dispatches a ChatRequestEvent with the summary as {input}.
     */
    @Action(listenEvents = {InputEvent.class})
    public static void processInput(InputEvent event, RunnerContext ctx) throws Exception {
        // Input is a JSON string produced by AggregateSkuWindow in the job
        String windowSummaryJson = (String) event.getInput();

        // Parse to extract keys that will be merged into the output recommendation
        JsonNode summaryNode = MAPPER.readTree(windowSummaryJson);
        ctx.getShortTermMemory().set("skuId", summaryNode.path("skuId").asText());
        ctx.getShortTermMemory().set("productName", summaryNode.path("productName").asText());
        ctx.getShortTermMemory().set("warehouseId", summaryNode.path("warehouseId").asText());

        // Inject window summary as {input} into the reorderPrompt template
        ChatMessage msg = new ChatMessage(MessageRole.USER, "", Map.of("input", windowSummaryJson));
        ctx.sendEvent(new ChatRequestEvent("reorderModel", List.of(msg)));
    }

    // --- RESPONSE ACTION ---

    /**
     * Parses the LLM's reorder recommendation and builds a ReorderRecommendation
     * combining LLM output with SKU metadata from short-term memory.
     */
    @Action(listenEvents = ChatResponseEvent.class)
    public static void processChatResponse(ChatResponseEvent event, RunnerContext ctx) throws Exception {
        JsonNode root = MAPPER.readTree(event.getResponse().getContent());

        String urgency = root.path("urgency").asText("MEDIUM");
        int suggestedOrderQuantity = root.path("suggestedOrderQuantity").asInt(0);

        List<String> recommendations = new ArrayList<>();
        JsonNode recNode = root.path("recommendations");
        if (recNode.isArray()) {
            for (JsonNode rec : recNode) {
                recommendations.add(rec.asText());
            }
        }

        // Memory values are ground truth — do not override with LLM-echoed values
        String skuId = (String) ctx.getShortTermMemory().get("skuId").getValue();
        String productName = (String) ctx.getShortTermMemory().get("productName").getValue();
        String warehouseId = (String) ctx.getShortTermMemory().get("warehouseId").getValue();

        ReorderRecommendation recommendation = new ReorderRecommendation(
                skuId,
                productName,
                warehouseId,
                urgency,
                suggestedOrderQuantity,
                Collections.unmodifiableList(recommendations));

        ctx.sendEvent(new OutputEvent(recommendation));
    }
}
