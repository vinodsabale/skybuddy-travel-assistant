package com.ai_assistence_service.travelassient.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

import com.ai_assistence_service.travelassient.dto.ChatMessageDto;
import com.ai_assistence_service.travelassient.dto.ChatReply;
import com.ai_assistence_service.travelassient.dto.ChatRequest;
import com.ai_assistence_service.travelassient.dto.PolicyAnswer;
import com.ai_assistence_service.travelassient.dto.TravelIntent;

import reactor.core.publisher.Flux;

@Service
public class ChatService {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("EEEE, yyyy-MM-dd", Locale.ENGLISH);

    private final ChatClient travelAssistantClient;
    private final ChatClient policyRagClient;
    private final ChatClient intentExtractionClient;
    private final ChatMemory chatMemory;
    private final String systemPrompt;

    public ChatService(@Qualifier("travelAssistantClient") ChatClient travelAssistantClient,
                       @Qualifier("policyRagClient") ChatClient policyRagClient,
                       @Qualifier("intentExtractionClient") ChatClient intentExtractionClient,
                       ChatMemory chatMemory,
                       @Value("classpath:/prompts/system-prompt.st") Resource systemPromptResource) {
        this.travelAssistantClient = travelAssistantClient;
        this.policyRagClient = policyRagClient;
        this.intentExtractionClient = intentExtractionClient;
        this.chatMemory = chatMemory;
        this.systemPrompt = readResource(systemPromptResource);
    }

    /** Normal (blocking) chat. */
    public ChatReply chat(ChatRequest request) {
        String conversationId = resolveConversationId(request.conversationId());
        String reply = prepare(request.message(), conversationId)
                .call()
                .content();
        return new ChatReply(conversationId, reply);
    }

    /** Streaming chat: tokens are sent to the client as they are generated. */
    public Flux<String> stream(String message, String conversationId) {
        return prepare(message, conversationId)
                .stream()
                .content();
    }

    /** Structured output: free text to a Java record. */
    public TravelIntent extractIntent(String message) {
        String userText = "Today's date is " + LocalDate.now().format(DATE_FORMAT) + ". User message: " + message;
        return intentExtractionClient.prompt()
                .user(userText)
                .call()
                .entity(TravelIntent.class);
    }

    /** Classic RAG over the uploaded documents. */
    public PolicyAnswer askPolicy(String question) {
        String answer = policyRagClient.prompt()
                .user(question)
                .call()
                .content();
        return new PolicyAnswer(question, answer);
    }

    /** Stored chat history of one conversation. */
    public List<ChatMessageDto> history(String conversationId) {
        return chatMemory.get(conversationId).stream()
                .filter(message -> message.getMessageType() == MessageType.USER
                        || message.getMessageType() == MessageType.ASSISTANT)
                .filter(message -> StringUtils.hasText(message.getText()))
                .map(this::toDto)
                .toList();
    }

    public void clearHistory(String conversationId) {
        chatMemory.clear(conversationId);
    }

    public String resolveConversationId(String conversationId) {
        return StringUtils.hasText(conversationId) ? conversationId.trim() : UUID.randomUUID().toString();
    }

    // ---------------------------------------------------------------- helpers

    private ChatClient.ChatClientRequestSpec prepare(String message, String conversationId) {
        return travelAssistantClient.prompt()
                .system(system -> system
                        .text(systemPrompt)
                        .param("currentDate", LocalDate.now().format(DATE_FORMAT)))
                .user(message)
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId));
    }

    private ChatMessageDto toDto(Message message) {
        return new ChatMessageDto(message.getMessageType().getValue(), message.getText());
    }

    private String readResource(Resource resource) {
        try {
            return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new UncheckedIOException("Could not read system prompt " + resource, ex);
        }
    }
}
