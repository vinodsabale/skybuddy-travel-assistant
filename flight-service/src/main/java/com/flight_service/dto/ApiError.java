package com.flight_service.dto;

import java.time.LocalDateTime;

public record ApiError(LocalDateTime timestamp, int status, String message) {
}
