package com.example.ai.banking.agent;

import com.example.ai.banking.dto.LoanRiskReport;
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
 * Agent 2 in the loan risk pipeline.
 *
 * Receives a JSON-serialized LoanWindowSummary (aggregated from a 5-minute tumbling
 * window) and uses the LLM to generate a final underwriting decision: APPROVE, REVIEW,
 * or DECLINE, along with a detailed justification list.
 *
 * If the decision is REVIEW, the LLM will call the notifyUnderwriter tool before
 * producing its JSON output — ensuring human oversight for borderline cases.
 */
public class LoanRiskReportAgent extends Agent {

    private static final ObjectMapper MAPPER =
            new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    // System prompt establishes the underwriter persona and strict output format.
    // The tool instruction is embedded in the prompt so the LLM knows when to call it.
    private static final String SYSTEM_PROMPT =
            "You are a senior loan underwriter. Based on aggregated risk data from multiple " +
            "loan applications for the same applicant, generate a final lending decision.\n\n" +
            "Input format:\n" +
            "{\"applicantId\": \"C001\", \"applicationCount\": 3, \"avgRiskScore\": 7.2, " +
            "\"allRiskFactors\": [\"low credit score\", \"high debt ratio\"], " +
            "\"recommendations\": [\"REVIEW\", \"DECLINE\", \"REVIEW\"]}\n\n" +
            "If the final decision is REVIEW, you MUST call the notifyUnderwriter tool " +
            "before producing output.\n\n" +
            "Respond ONLY with valid JSON:\n" +
            "{\"finalDecision\": \"DECLINE\", \"justification\": [\"persistent low credit score\", " +
            "\"debt exceeds 60% of income\"], \"avgRiskScore\": 7.2}";

    @Prompt
    public static org.apache.flink.agents.api.prompt.Prompt loanRiskReportPrompt() {
        return org.apache.flink.agents.api.prompt.Prompt.fromMessages(
                Arrays.asList(
                        new ChatMessage(MessageRole.SYSTEM, SYSTEM_PROMPT),
                        new ChatMessage(MessageRole.USER, "{input}")));
    }

    // The tool is listed so the framework registers it for LLM invocation
    @ChatModelSetup
    public static ResourceDescriptor loanRiskReportModel() {
        return ResourceDescriptor.Builder.newBuilder(ResourceName.ChatModel.OPENAI_SETUP)
                .addInitialArgument("connection", "groqConnection")
                .addInitialArgument("model", "llama-3.3-70b-versatile")
                .addInitialArgument("prompt", "loanRiskReportPrompt")
                .addInitialArgument("tools", Collections.singletonList("notifyUnderwriter"))
                .build();
    }

    // --- TOOL ---

    /**
     * Notifies a human underwriter when the AI decision is REVIEW (borderline case).
     * This ensures no REVIEW decision is silently dropped — a human must evaluate it.
     */
    @Tool(description = "Notify a human underwriter to manually review a borderline loan application. Call this when the final decision is REVIEW.")
    public static void notifyUnderwriter(
            @ToolParam(name = "applicantId") String applicantId,
            @ToolParam(name = "summary") String summary) {
        System.out.printf("[LOAN REVIEW] Manual review required for applicant %s. Summary: %s%n",
                applicantId, summary);
        // Replace with: send email via JavaMail (javax.mail) to underwriter@bank.com, or POST
        // to internal loan management system webhook, or push notification via Firebase/SNS —
        // e.g. using JavaMail:
        //   Properties props = new Properties();
        //   props.put("mail.smtp.host", "smtp.bank.com");
        //   Session session = Session.getInstance(props);
        //   MimeMessage message = new MimeMessage(session);
        //   message.setFrom(new InternetAddress("flink-pipeline@bank.com"));
        //   message.addRecipient(Message.RecipientType.TO, new InternetAddress("underwriter@bank.com"));
        //   message.setSubject("Loan Review Required: " + applicantId);
        //   message.setText(summary);
        //   Transport.send(message);
    }

    // --- INPUT ACTION ---

    /**
     * Receives the JSON window summary string from the upstream aggregation step,
     * stores key metadata in short-term memory, and sends a ChatRequestEvent.
     */
    @Action(listenEvents = {InputEvent.class})
    public static void processInput(InputEvent event, RunnerContext ctx) throws Exception {
        // Input is the JSON string produced by AggregateLoanWindow in the job
        String windowSummaryJson = (String) event.getInput();

        // Parse the summary to extract values for short-term memory
        JsonNode summaryNode = MAPPER.readTree(windowSummaryJson);
        String applicantId = summaryNode.path("applicantId").asText();
        int applicationCount = summaryNode.path("applicationCount").asInt();
        double avgRiskScore = summaryNode.path("avgRiskScore").asDouble();

        // Store in memory — these values will be merged with the LLM response later
        ctx.getShortTermMemory().set("applicantId", applicantId);
        ctx.getShortTermMemory().set("applicationCount", applicationCount);
        ctx.getShortTermMemory().set("avgRiskScore", avgRiskScore);

        // Inject the full window summary as {input} into the prompt template
        ChatMessage msg = new ChatMessage(MessageRole.USER, "", Map.of("input", windowSummaryJson));
        ctx.sendEvent(new ChatRequestEvent("loanRiskReportModel", List.of(msg)));
    }

    // --- RESPONSE ACTION ---

    /**
     * Parses the LLM's final underwriting decision and builds a LoanRiskReport
     * combining the parsed decision with the metadata stored in short-term memory.
     */
    @Action(listenEvents = ChatResponseEvent.class)
    public static void processChatResponse(ChatResponseEvent event, RunnerContext ctx) throws Exception {
        JsonNode root = MAPPER.readTree(event.getResponse().getContent());

        String finalDecision = root.path("finalDecision").asText("REVIEW");
        double avgRiskScore = root.path("avgRiskScore").asDouble();

        List<String> justification = new ArrayList<>();
        JsonNode justNode = root.path("justification");
        if (justNode.isArray()) {
            for (JsonNode item : justNode) {
                justification.add(item.asText());
            }
        }

        // Prefer memory values for applicantId/applicationCount — they are ground truth
        // from the window aggregation, not LLM-generated
        String applicantId = (String) ctx.getShortTermMemory().get("applicantId").getValue();
        int applicationCount = (Integer) ctx.getShortTermMemory().get("applicationCount").getValue();

        LoanRiskReport report = new LoanRiskReport(
                applicantId,
                applicationCount,
                finalDecision,
                Collections.unmodifiableList(justification),
                avgRiskScore);

        ctx.sendEvent(new OutputEvent(report));
    }
}
