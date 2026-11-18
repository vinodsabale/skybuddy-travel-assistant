package com.ai_assistence_service.travelassient.controller;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ai_assistence_service.travelassient.dto.DocumentUploadResponse;
import com.ai_assistence_service.travelassient.dto.PolicyAnswer;
import com.ai_assistence_service.travelassient.dto.PolicyQuestionRequest;
import com.ai_assistence_service.travelassient.service.ChatService;
import com.ai_assistence_service.travelassient.service.DocumentIngestionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentIngestionService documentIngestionService;
    private final ChatService chatService;

    /** Upload terms and conditions, refund policy, baggage rules (PDF, DOCX, TXT) into the vector store. */
    @PostMapping(value = "/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DocumentUploadResponse upload(@RequestParam("file") MultipartFile file) {
        return documentIngestionService.ingest(file);
    }

    /** Ask a question directly against the uploaded documents (classic RAG with QuestionAnswerAdvisor). */
    @PostMapping("/policy/ask")
    public PolicyAnswer askPolicy(@Valid @RequestBody PolicyQuestionRequest request) {
        return chatService.askPolicy(request.question());
    }
}
