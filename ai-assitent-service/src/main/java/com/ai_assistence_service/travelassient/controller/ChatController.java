package com.ai_assistence_service.travelassient.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ai_assistence_service.travelassient.dto.ChatMessageDto;
import com.ai_assistence_service.travelassient.dto.ChatReply;
import com.ai_assistence_service.travelassient.dto.ChatRequest;
import com.ai_assistence_service.travelassient.dto.TravelIntent;
import com.ai_assistence_service.travelassient.service.ChatService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class ChatController {

    private static final String CONVERSATION_HEADER = "X-Conversation-Id";

    private final ChatService chatService;

    @PostMapping("/chat")
    public ChatReply chat(@Valid @RequestBody ChatRequest request) {
        return chatService.chat(request);
    }

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<Flux<String>> stream(@Valid @RequestBody ChatRequest request) {
        String conversationId = chatService.resolveConversationId(request.conversationId());
        return ResponseEntity.ok()
                .header(CONVERSATION_HEADER, conversationId)
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .body(chatService.stream(request.message(), conversationId));
    }

    @PostMapping("/intent")
    public TravelIntent extractIntent(@Valid @RequestBody ChatRequest request) {
        return chatService.extractIntent(request.message());
    }

    @GetMapping("/history/{conversationId}")
    public List<ChatMessageDto> history(@PathVariable String conversationId) {
        return chatService.history(conversationId);
    }

    @DeleteMapping("/history/{conversationId}")
    public ResponseEntity<Void> clearHistory(@PathVariable String conversationId) {
        chatService.clearHistory(conversationId);
        return ResponseEntity.noContent().build();
    }
}
