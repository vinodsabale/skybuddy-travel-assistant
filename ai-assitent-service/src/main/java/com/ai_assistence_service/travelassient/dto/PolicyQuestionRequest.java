package com.ai_assistence_service.travelassient.dto;
import jakarta.validation.constraints.NotBlank;

public record PolicyQuestionRequest(@NotBlank(message = "question is required") String question) {
}
