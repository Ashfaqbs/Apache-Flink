package com.example.ai.banking.agent;

import com.example.ai.banking.dto.FraudAlert;
import com.example.ai.banking.dto.Transaction;
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
 * ReAct agent for real-time bank transaction fraud detection.
 *
 * Follows the ReAct (Reason + Act) pattern: the LLM reasons about the transaction,
 * may invoke tools (blockCard, alertFraudTeam), and then produces a structured
 * JSON response. No @Prompt annotation is used — the prompt is built manually.
 */
public class FraudDetectionAgent extends Agent {

    private static final ObjectMapper MAPPER =
            new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    // Register the Groq model with the two tools this agent can invoke.
    // Tools are listed by method name — the framework wires them automatically.
    @ChatModelSetup
    public static ResourceDescriptor chatModel() {
        return ResourceDescriptor.Builder.newBuilder(ResourceName.ChatModel.OPENAI_SETUP)
                .addInitialArgument("connection", "groqConnection")
                .addInitialArgument("model", "meta-llama/llama-4-scout-17b-16e-instruct")
                .addInitialArgument("tools", List.of("blockCard", "alertFraudTeam"))
                .build();
    }

    // --- TOOLS ---

    /**
     * Blocks the customer's card to prevent further fraudulent charges.
     * The LLM calls this when risk is HIGH or CRITICAL.
     */
    @Tool(description = "Block a customer's card immediately to prevent further fraudulent transactions. Call this when risk is HIGH or CRITICAL.")
    public static String blockCard(
            @ToolParam(name = "accountId") String accountId,
            @ToolParam(name = "reason") String reason) {
        System.out.printf("[FRAUD] Blocking card for account %s. Reason: %s%n", accountId, reason);
        // Replace with: call your card management API (Visa/Mastercard network API,
        // or internal card service REST endpoint) — e.g.:
        //   HttpClient.newHttpClient().send(
        //       HttpRequest.newBuilder()
        //           .uri(URI.create("https://card-service/api/v1/accounts/" + accountId + "/block"))
        //           .POST(HttpRequest.BodyPublishers.ofString("{\"reason\":\"" + reason + "\"}"))
        //           .build(),
        //       HttpResponse.BodyHandlers.ofString());
        return "Card blocked for account " + accountId;
    }

    /**
     * Sends a fraud incident alert to the fraud operations team.
     * The LLM calls this alongside blockCard for HIGH/CRITICAL risk transactions.
     */
    @Tool(description = "Alert the fraud team about a high-risk transaction incident. Call this when risk is HIGH or CRITICAL.")
    public static String alertFraudTeam(
            @ToolParam(name = "incidentDetails") String incidentDetails,
            @ToolParam(name = "severity") String severity) {
        System.out.printf("[FRAUD ALERT] Severity: %s | Details: %s%n", severity, incidentDetails);
        // Replace with: HTTP POST to PagerDuty/OpsGenie, or send email via JavaMail/SendGrid,
        // or publish to a Slack webhook URL — e.g.:
        //   HttpClient.newHttpClient().send(
        //       HttpRequest.newBuilder()
        //           .uri(URI.create("https://hooks.slack.com/services/YOUR/WEBHOOK/URL"))
        //           .header("Content-Type", "application/json")
        //           .POST(HttpRequest.BodyPublishers.ofString(
        //               "{\"text\":\"Fraud Alert [" + severity + "]: " + incidentDetails + "\"}"))
        //           .build(),
        //       HttpResponse.BodyHandlers.ofString());
        return "Fraud team alerted with severity " + severity;
    }

    // --- INPUT ACTION ---

    /**
     * Receives a Transaction from the input event, stores identifiers in short-term memory,
     * builds a detailed ReAct prompt instructing the LLM to assess fraud risk and
     * conditionally invoke tools, then dispatches a ChatRequestEvent.
     */
    @Action(listenEvents = {InputEvent.class})
    public static void processInput(InputEvent event, RunnerContext ctx) throws Exception {
        // Cast the input to our domain type — this agent only handles Transaction objects
        Transaction transaction = (Transaction) event.getInput();

        // Persist identifiers so the response handler can enrich the FraudAlert
        ctx.getShortTermMemory().set("transactionId", transaction.getTransactionId());
        ctx.getShortTermMemory().set("accountId", transaction.getAccountId());

        // Serialize the full transaction to JSON so the LLM has all fields available
        String transactionJson = MAPPER.writeValueAsString(transaction);

        // Build a structured ReAct prompt: reason through the transaction, act via tools,
        // then respond with a fixed JSON schema for easy parsing downstream
        String prompt = "You are a real-time bank fraud detection system. Analyze this transaction " +
                "and decide if it is fraudulent.\n\n" +
                "Transaction:\n" + transactionJson + "\n\n" +
                "Step 1: Assess fraud risk level — LOW, MEDIUM, HIGH, or CRITICAL.\n" +
                "Step 2: List the specific fraud signals you detected (e.g., unusual amount, " +
                "card not present, high-risk merchant category, foreign location).\n" +
                "Step 3: If risk is HIGH or CRITICAL, you MUST call the 'blockCard' tool with " +
                "the accountId and reason, then call 'alertFraudTeam' with the incident details.\n" +
                "Step 4: Respond with ONLY a JSON object in this exact format (no extra text):\n" +
                "{\"riskLevel\": \"HIGH\", \"fraudSignals\": [\"card_not_present\", " +
                "\"high_value_foreign\"], \"actionTaken\": \"card_blocked\"}";

        ChatMessage msg = new ChatMessage(MessageRole.USER, prompt);
        ctx.sendEvent(new ChatRequestEvent("chatModel", List.of(msg)));
    }

    // --- RESPONSE ACTION ---

    /**
     * Parses the LLM's JSON response and emits a FraudAlert.
     * Memory values from processInput are used to complete the alert object.
     */
    @Action(listenEvents = ChatResponseEvent.class)
    public static void processChatResponse(ChatResponseEvent event, RunnerContext ctx) throws Exception {
        String responseContent = event.getResponse().getContent().trim();

        // Parse the structured JSON the LLM was instructed to produce
        JsonNode root = MAPPER.readTree(responseContent);

        String riskLevel = root.path("riskLevel").asText("UNKNOWN");
        String actionTaken = root.path("actionTaken").asText("none");

        // Collect the fraudSignals array into a mutable list
        List<String> fraudSignals = new ArrayList<>();
        JsonNode signalsNode = root.path("fraudSignals");
        if (signalsNode.isArray()) {
            for (JsonNode signal : signalsNode) {
                fraudSignals.add(signal.asText());
            }
        }

        // Retrieve identifiers saved during processInput
        String transactionId = (String) ctx.getShortTermMemory().get("transactionId").getValue();
        String accountId = (String) ctx.getShortTermMemory().get("accountId").getValue();

        // Use current UTC time as the alert timestamp (transaction timestamp is in the source)
        String alertTimestamp = Instant.now().toString();

        FraudAlert alert = new FraudAlert(
                transactionId,
                accountId,
                riskLevel,
                Collections.unmodifiableList(fraudSignals),
                actionTaken,
                alertTimestamp);

        ctx.sendEvent(new OutputEvent(alert));
    }
}
