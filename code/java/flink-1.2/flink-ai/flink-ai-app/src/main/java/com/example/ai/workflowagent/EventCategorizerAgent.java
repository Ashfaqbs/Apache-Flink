package com.example.ai.workflowagent;


import org.apache.flink.agents.api.*;
import org.apache.flink.agents.api.agents.Agent;
import org.apache.flink.agents.api.annotation.Action;
import org.apache.flink.agents.api.annotation.ChatModelSetup;
import org.apache.flink.agents.api.chat.messages.ChatMessage;
import org.apache.flink.agents.api.chat.messages.MessageRole;
import org.apache.flink.agents.api.context.RunnerContext;
import org.apache.flink.agents.api.event.ChatRequestEvent;
import org.apache.flink.agents.api.event.ChatResponseEvent;
import org.apache.flink.agents.api.resource.ResourceDescriptor;
import org.apache.flink.agents.api.resource.ResourceName;

import java.util.List;




public class EventCategorizerAgent extends Agent {


//    OLLAMA LOCAL MODELS
//    @ChatModelSetup
//    public static ResourceDescriptor chatModel() {
//        // Connects to the Ollama resource we will define in the main job
//        return ResourceDescriptor.Builder.newBuilder(ResourceName.ChatModel.OLLAMA_SETUP)
//                .addInitialArgument("connection", "ollamaConnection")
//                .addInitialArgument("model", "qwen3:8b")
//                .build();
//    }


//    GROQ OPEN AI CONFIG
    @ChatModelSetup
    public static ResourceDescriptor chatModel() {
        return ResourceDescriptor.Builder.newBuilder(ResourceName.ChatModel.OPENAI_SETUP)
                .addInitialArgument("connection", "groqConnection")
                .addInitialArgument("model", "meta-llama/llama-4-scout-17b-16e-instruct")
                .build();
    }

    @Action(listenEvents = {InputEvent.class})
    public static void processInput(InputEvent event, RunnerContext ctx) throws Exception {
        MyEvent input = (MyEvent) event.getInput();

        // 1. Save original event data to Short-Term Memory (Using POJO Getters)
        ctx.getShortTermMemory().set("id", input.getId());
        ctx.getShortTermMemory().set("name", input.getName());
        ctx.getShortTermMemory().set("timestamp", input.getTimestamp());

        // 2. Ask the LLM to categorize the event name
        String prompt = "Analyze this event name and categorize it as 'Routine', 'Warning', or 'Critical'. Reply with ONLY the category word: " + input.getName();
        ChatMessage msg = new ChatMessage(MessageRole.USER, prompt);

        ctx.sendEvent(new ChatRequestEvent("chatModel", List.of(msg)));
    }


    @Action(listenEvents = ChatResponseEvent.class)
    public static void processChatResponse(ChatResponseEvent event, RunnerContext ctx) throws Exception {
        // 1. Grab the AI's answer
        String aiCategory = event.getResponse().getContent().trim();

        // 2. Pull the original data back out of memory
        int id = (Integer) ctx.getShortTermMemory().get("id").getValue();
        String originalName = (String) ctx.getShortTermMemory().get("name").getValue();
        String timestamp = (String) ctx.getShortTermMemory().get("timestamp").getValue();

        // 3. Enrich the data!
        String enrichedName = originalName + " [AI Category: " + aiCategory + "]";
        MyEvent enrichedEvent = new MyEvent(id, enrichedName, timestamp);

        // 4. Send it down the stream to the Kafka Sink
        ctx.sendEvent(new OutputEvent(enrichedEvent));
    }
}