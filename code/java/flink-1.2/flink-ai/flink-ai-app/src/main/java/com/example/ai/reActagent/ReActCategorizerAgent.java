package com.example.ai.reActagent;

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
import org.apache.flink.agents.api.event.*;
import org.apache.flink.agents.api.resource.ResourceDescriptor;
import org.apache.flink.agents.api.resource.ResourceName;

import java.util.Collections;
import java.util.List;

public class ReActCategorizerAgent extends Agent {

    @ChatModelSetup
    public static ResourceDescriptor chatModel() {
        return ResourceDescriptor.Builder.newBuilder(ResourceName.ChatModel.OPENAI_SETUP)
                .addInitialArgument("connection", "groqConnection")
                .addInitialArgument("model", "meta-llama/llama-4-scout-17b-16e-instruct")
                .addInitialArgument("tools", Collections.singletonList("pageEngineer"))
                .build();
    }

    // --- THE TOOL ---
    @Tool(description = "Triggers a PagerDuty alert for on-call engineers. Use this ONLY for 'Critical' system events.")
    public static String pageEngineer(@ToolParam(name = "incidentDetails") String incidentDetails) {
        // logic to call slack api or whatsapp api or mail
        System.out.println("\n==================================================");
        System.out.println("🚨 [TOOL EXECUTION] PAGERDUTY ALERT TRIGGERED! 🚨");
        System.out.println("🚨 Incident: " + incidentDetails);
        System.out.println("==================================================\n");
        return "Alert successfully sent to on-call engineer.";
    }

    // --- INPUT ACTION ---
    @Action(listenEvents = {InputEvent.class})
    public static void processInput(InputEvent event, RunnerContext ctx) throws Exception {
        ReActEvent input = (ReActEvent) event.getInput(); // Using the new Entity

        ctx.getShortTermMemory().set("id", input.getId());
        ctx.getShortTermMemory().set("name", input.getName());
        ctx.getShortTermMemory().set("timestamp", input.getTimestamp());

        String prompt = "Analyze this system log: '" + input.getName() + "'.\n" +
                "Step 1: Determine if it is 'Routine', 'Warning', or 'Critical'.\n" +
                "Step 2: If it is 'Critical', you MUST call the 'pageEngineer' tool with the log details.\n" +
                "Step 3: After the tool finishes (or if no tool is needed), reply with ONLY the final category word.";

        ChatMessage msg = new ChatMessage(MessageRole.USER, prompt);
        ctx.sendEvent(new ChatRequestEvent("chatModel", List.of(msg)));
    }

    // --- RESPONSE ACTION ---
    @Action(listenEvents = ChatResponseEvent.class)
    public static void processChatResponse(ChatResponseEvent event, RunnerContext ctx) throws Exception {
        String aiCategory = event.getResponse().getContent().trim();

        int id = (Integer) ctx.getShortTermMemory().get("id").getValue();
        String originalName = (String) ctx.getShortTermMemory().get("name").getValue();
        String timestamp = (String) ctx.getShortTermMemory().get("timestamp").getValue();

        String enrichedName = originalName + " [AI Category: " + aiCategory + "]";

        // Create the new ReActEvent entity
        ReActEvent enrichedEvent = new ReActEvent(id, enrichedName, timestamp);

        ctx.sendEvent(new OutputEvent(enrichedEvent));
    }
}