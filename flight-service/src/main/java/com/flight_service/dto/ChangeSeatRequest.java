package com.flight_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ChangeSeatRequest(
        @NotBlank(message = "Seat number is required")
        @Pattern(regexp = "^\\d{1,2}[A-Fa-f]$", message = "Seat number must look like 12A (row number and letter A-F)")
        String seatNumber) {
}
