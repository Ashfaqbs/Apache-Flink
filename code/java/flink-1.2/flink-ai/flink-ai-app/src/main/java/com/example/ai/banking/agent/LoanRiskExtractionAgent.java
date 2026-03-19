package com.example.ai.banking.agent;

import com.example.ai.banking.dto.LoanApplication;
import com.example.ai.banking.dto.LoanRiskSignals;
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
 * Agent 1 in the loan risk pipeline.
 *
 * Receives a single LoanApplication and uses the LLM to extract a numeric
 * risk score (1-10) plus a list of human-readable risk factors. The output
 * LoanRiskSignals is keyed by applicantId for downstream window aggregation.
 *
 * Uses the Workflow pattern: @Prompt defines the system + user prompt template,
 * and the input handler injects the serialized application as {input}.
 */
public class LoanRiskExtractionAgent extends Agent {

    private static final ObjectMapper MAPPER =
            new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    // System prompt instructs the LLM on its role and exact output schema.
    // Strict JSON-only output avoids parsing failures downstream.
    private static final String SYSTEM_PROMPT =
            "You are a loan risk analyst. Given a loan application, extract a risk score " +
            "(1-10, where 10 is highest risk) and identify specific risk factors.\n\n" +
            "Input format:\n" +
            "{\"applicationId\": \"A001\", \"applicantId\": \"C001\", \"requestedAmount\": 50000, " +
            "\"creditScore\": 620, \"annualIncome\": 45000, \"employmentYears\": 1, " +
            "\"existingDebtAmount\": 20000}\n\n" +
            "Respond ONLY with valid JSON:\n" +
            "{\"applicationId\": \"A001\", \"applicantId\": \"C001\", \"riskScore\": 7, " +
            "\"riskFactors\": [\"low credit score\", \"high debt-to-income ratio\", " +
            "\"short employment history\"], \"approvalRecommendation\": \"REVIEW\"}";

    // @Prompt links the system prompt + user template to this agent's model setup.
    // The {input} placeholder is replaced with the serialized LoanApplication at runtime.
    @Prompt
    public static org.apache.flink.agents.api.prompt.Prompt loanRiskExtractionPrompt() {
        return org.apache.flink.agents.api.prompt.Prompt.fromMessages(
                Arrays.asList(
                        new ChatMessage(MessageRole.SYSTEM, SYSTEM_PROMPT),
                        new ChatMessage(MessageRole.USER, "{input}")));
    }

    // Reference the prompt by its method name so the framework wires it correctly.
    // No tools needed for extraction — this agent only reads and classifies.
    @ChatModelSetup
    public static ResourceDescriptor loanRiskExtractionModel() {
        return ResourceDescriptor.Builder.newBuilder(ResourceName.ChatModel.OPENAI_SETUP)
                .addInitialArgument("connection", "groqConnection")
                .addInitialArgument("model", "llama-3.3-70b-versatile")
                .addInitialArgument("prompt", "loanRiskExtractionPrompt")
                .build();
    }

    // --- INPUT ACTION ---

    /**
     * Deserializes the LoanApplication, stores identifiers in short-term memory,
     * and dispatches a ChatRequestEvent with the application JSON as {input}.
     */
    @Action(listenEvents = {InputEvent.class})
    public static void processInput(InputEvent event, RunnerContext ctx) throws Exception {
        // The upstream Kafka source emits LoanApplication objects
        LoanApplication application = (LoanApplication) event.getInput();

        // Store IDs for use in the response handler — memory survives across events
        ctx.getShortTermMemory().set("applicationId", application.getApplicationId());
        ctx.getShortTermMemory().set("applicantId", application.getApplicantId());

        // Serialize the full application as the user message content
        String payload = MAPPER.writeValueAsString(application);

        // The Map.of("input", payload) substitutes into the {input} placeholder
        // defined in loanRiskExtractionPrompt — this is the workflow pattern
        ChatMessage msg = new ChatMessage(MessageRole.USER, "", Map.of("input", payload));
        ctx.sendEvent(new ChatRequestEvent("loanRiskExtractionModel", List.of(msg)));
    }

    // --- RESPONSE ACTION ---

    /**
     * Parses the LLM's JSON risk assessment and emits a LoanRiskSignals object
     * for downstream windowing by applicantId.
     */
    @Action(listenEvents = ChatResponseEvent.class)
    public static void processChatResponse(ChatResponseEvent event, RunnerContext ctx) throws Exception {
        JsonNode root = MAPPER.readTree(event.getResponse().getContent());

        // Extract required fields — fail loudly if the LLM deviated from schema
        JsonNode riskScoreNode = root.findValue("riskScore");
        JsonNode riskFactorsNode = root.findValue("riskFactors");
        JsonNode recommendationNode = root.findValue("approvalRecommendation");

        if (riskScoreNode == null || riskFactorsNode == null || recommendationNode == null) {
            throw new IllegalStateException(
                    "LLM response missing required fields: " + event.getResponse().getContent());
        }

        List<String> riskFactors = new ArrayList<>();
        if (riskFactorsNode.isArray()) {
            for (JsonNode factor : riskFactorsNode) {
                riskFactors.add(factor.asText());
            }
        }

        // The LLM response includes these IDs, but we use memory as the source of truth
        // in case the LLM truncates or alters them
        String applicationId = (String) ctx.getShortTermMemory().get("applicationId").getValue();
        String applicantId = (String) ctx.getShortTermMemory().get("applicantId").getValue();

        LoanRiskSignals signals = new LoanRiskSignals(
                applicationId,
                applicantId,
                riskScoreNode.asInt(),
                Collections.unmodifiableList(riskFactors),
                recommendationNode.asText());

        ctx.sendEvent(new OutputEvent(signals));
    }
}
