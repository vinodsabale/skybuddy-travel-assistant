package com.ai_assistence_service.travelassient.dto;

import java.time.LocalDateTime;

public record ApiError(LocalDateTime timestamp, int status, String message) {
}
