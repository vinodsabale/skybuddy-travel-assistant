package com.ai_assistence_service.travelassient.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatRequest(
        String conversationId,

        @NotBlank(message = "message is required")
        @Size(max = 2000, message = "message must be at most 2000 characters")
        String message) {
}
