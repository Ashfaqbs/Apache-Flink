package com.example.ai.retail.agent;

import com.example.ai.retail.dto.ChurnAlert;
import com.example.ai.retail.dto.CustomerEvent;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.agents.api.InputEvent;
import org.apache.flink.agents.api.OutputEvent;
import org.apache.flink.agents.api.agents.Agent;
import org.apache.flink.agents.api.annotation.Action;
import org.apache.flink.agents.api.annotation.ChatModelSetup;
import org.apache.flink.agents.api.annotation.Tool;
import org.apache.flink.agents.api.annotation.ToolParam;
import org.apache.flink.agents.api.chat.messages.ChatMessage;
import org.apache.flink.agents.api.chat.messages.MessageRole;
import org.apache.flink.agents.api.context.RunnerContext;
import org.apache.flink.agents.api.event.ChatRequestEvent;
import org.apache.flink.agents.api.event.ChatResponseEvent;
import org.apache.flink.agents.api.resource.ResourceDescriptor;
import org.apache.flink.agents.api.resource.ResourceName;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ReAct agent for real-time retail customer churn risk detection.
 *
 * Receives a CustomerEvent (behavioral signal), reasons about churn risk,
 * conditionally invokes retention tools (triggerRetentionOffer, notifyCRMSystem,
 * escalateToHumanAgent), then emits a structured ChurnAlert.
 *
 * Uses the ReAct pattern: the LLM reasons step-by-step and decides when to
 * call tools — no fixed workflow required since churn signals vary widely.
 */
public class ChurnDetectionAgent extends Agent {

    private static final ObjectMapper MAPPER =
            new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    // List all three tools so the framework registers them for LLM invocation
    @ChatModelSetup
    public static ResourceDescriptor chatModel() {
        return ResourceDescriptor.Builder.newBuilder(ResourceName.ChatModel.OPENAI_SETUP)
                .addInitialArgument("connection", "groqConnection")
                .addInitialArgument("model", "meta-llama/llama-4-scout-17b-16e-instruct")
                .addInitialArgument("tools",
                        List.of("triggerRetentionOffer", "notifyCRMSystem", "escalateToHumanAgent"))
                .build();
    }

    // --- TOOLS ---

    /**
     * Triggers an automated retention offer for a customer at risk of churning.
     * The LLM selects the offer type based on the customer's event history.
     */
    @Tool(description = "Trigger an automated retention offer for a customer showing churn signals. Use when churn risk is HIGH or CRITICAL.")
    public static String triggerRetentionOffer(
            @ToolParam(name = "customerId") String customerId,
            @ToolParam(name = "offerType") String offerType) {
        System.out.printf("[RETENTION] Triggering offer '%s' for customer %s%n", offerType, customerId);
        // Replace with: POST to your promotions/offers microservice API, or publish to a Kafka
        // topic that the offers service consumes, or call a CRM campaign trigger endpoint — e.g.:
        //   HttpClient.newHttpClient().send(
        //       HttpRequest.newBuilder()
        //           .uri(URI.create("https://offers-service/api/v1/trigger"))
        //           .header("Content-Type", "application/json")
        //           .POST(HttpRequest.BodyPublishers.ofString(
        //               "{\"customerId\":\"" + customerId + "\",\"offerType\":\"" + offerType + "\"}"))
        //           .build(),
        //       HttpResponse.BodyHandlers.ofString());
        return "Retention offer '" + offerType + "' triggered for customer " + customerId;
    }

    /**
     * Updates the CRM system with the churn risk assessment so sales and support
     * teams have current context when they interact with the customer.
     */
    @Tool(description = "Notify the CRM system of a customer's churn risk level and signals. Call this alongside triggerRetentionOffer for HIGH/CRITICAL risk.")
    public static String notifyCRMSystem(
            @ToolParam(name = "customerId") String customerId,
            @ToolParam(name = "churnRisk") String churnRisk,
            @ToolParam(name = "signals") String signals) {
        System.out.printf("[CRM] Notifying CRM — Customer: %s, Risk: %s, Signals: %s%n",
                customerId, churnRisk, signals);
        // Replace with: HTTP POST to Salesforce/HubSpot CRM API, or send internal alert email
        // via JavaMail/SendGrid, or push to Slack retention channel via webhook — e.g.:
        //   HttpClient.newHttpClient().send(
        //       HttpRequest.newBuilder()
        //           .uri(URI.create("https://api.hubspot.com/crm/v3/objects/contacts/" + customerId))
        //           .header("Authorization", "Bearer " + System.getenv("HUBSPOT_API_KEY"))
        //           .PATCH(HttpRequest.BodyPublishers.ofString(
        //               "{\"properties\":{\"churn_risk\":\"" + churnRisk + "\"}}"))
        //           .build(),
        //       HttpResponse.BodyHandlers.ofString());
        return "CRM updated for customer " + customerId + " with risk " + churnRisk;
    }

    /**
     * Escalates a high-value or complex churn case to a human retention agent
     * when automated offers are unlikely to be sufficient.
     */
    @Tool(description = "Escalate a critical churn risk customer to a human retention agent. Use when automated offers are insufficient or for VIP customers.")
    public static String escalateToHumanAgent(
            @ToolParam(name = "customerId") String customerId,
            @ToolParam(name = "reason") String reason) {
        System.out.printf("[ESCALATION] Escalating customer %s to human agent. Reason: %s%n",
                customerId, reason);
        // Replace with: create a ticket in Zendesk/Freshdesk API, or send SMS alert via Twilio
        // REST API, or trigger an internal escalation workflow — e.g.:
        //   HttpClient.newHttpClient().send(
        //       HttpRequest.newBuilder()
        //           .uri(URI.create("https://api.twilio.com/2010-04-01/Accounts/" +
        //               System.getenv("TWILIO_ACCOUNT_SID") + "/Messages.json"))
        //           .header("Authorization", "Basic " + Base64.getEncoder().encodeToString(
        //               (System.getenv("TWILIO_ACCOUNT_SID") + ":" +
        //                System.getenv("TWILIO_AUTH_TOKEN")).getBytes()))
        //           .POST(HttpRequest.BodyPublishers.ofString(
        //               "To=%2B1XXXXXXXXXX&From=%2B1YYYYYYYYYY&Body=Churn+Escalation:+" + customerId))
        //           .build(),
        //       HttpResponse.BodyHandlers.ofString());
        return "Customer " + customerId + " escalated to human agent";
    }

    // --- INPUT ACTION ---

    /**
     * Receives a CustomerEvent, stores the customerId in short-term memory,
     * builds a structured ReAct prompt, and dispatches a ChatRequestEvent.
     */
    @Action(listenEvents = {InputEvent.class})
    public static void processInput(InputEvent event, RunnerContext ctx) throws Exception {
        CustomerEvent customerEvent = (CustomerEvent) event.getInput();

        // Store customerId so the response handler can build the ChurnAlert
        ctx.getShortTermMemory().set("customerId", customerEvent.getCustomerId());

        // Serialize the full event so the LLM has all behavioral signals available
        String eventJson = MAPPER.writeValueAsString(customerEvent);

        // ReAct prompt: instruct the LLM to reason step-by-step and call tools
        // conditionally, then output a fixed JSON schema for reliable parsing
        String prompt = "You are a retail customer retention specialist. Analyze this customer " +
                "behavior event to assess churn risk.\n\n" +
                "Customer Event:\n" + eventJson + "\n\n" +
                "Step 1: Assess churn risk — LOW, MEDIUM, HIGH, or CRITICAL.\n" +
                "Step 2: Identify churn signals (e.g., increasing support tickets, declining " +
                "purchase frequency, cart abandonment, long inactivity).\n" +
                "Step 3: If risk is HIGH or CRITICAL, you MUST call 'triggerRetentionOffer' " +
                "with a suitable offer type, then call 'notifyCRMSystem' with the customer details.\n" +
                "Step 4: Respond with ONLY a JSON object:\n" +
                "{\"churnRisk\": \"HIGH\", \"churnSignals\": [\"28_days_inactive\", " +
                "\"3_support_tickets\"], \"actionTaken\": \"retention_offer_sent\"}";

        ChatMessage msg = new ChatMessage(MessageRole.USER, prompt);
        ctx.sendEvent(new ChatRequestEvent("chatModel", List.of(msg)));
    }

    // --- RESPONSE ACTION ---

    /**
     * Parses the LLM's JSON churn assessment and emits a ChurnAlert
     * enriched with the customerId from short-term memory and current timestamp.
     */
    @Action(listenEvents = ChatResponseEvent.class)
    public static void processChatResponse(ChatResponseEvent event, RunnerContext ctx) throws Exception {
        String responseContent = event.getResponse().getContent().trim();
        JsonNode root = MAPPER.readTree(responseContent);

        String churnRisk = root.path("churnRisk").asText("UNKNOWN");
        String actionTaken = root.path("actionTaken").asText("none");

        List<String> churnSignals = new ArrayList<>();
        JsonNode signalsNode = root.path("churnSignals");
        if (signalsNode.isArray()) {
            for (JsonNode signal : signalsNode) {
                churnSignals.add(signal.asText());
            }
        }

        String customerId = (String) ctx.getShortTermMemory().get("customerId").getValue();
        String alertTimestamp = Instant.now().toString();

        ChurnAlert alert = new ChurnAlert(
                customerId,
                churnRisk,
                Collections.unmodifiableList(churnSignals),
                actionTaken,
                alertTimestamp);

        ctx.sendEvent(new OutputEvent(alert));
    }
}
