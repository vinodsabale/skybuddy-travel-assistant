package com.flight_service.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdatePassengerRequest(
        @Size(max = 100, message = "Passenger name must be at most 100 characters")
        String passengerName,

        @Min(value = 1, message = "Passenger age must be at least 1")
        @Max(value = 120, message = "Passenger age must be at most 120")
        Integer passengerAge,

        @Pattern(regexp = "^[6-9]\\d{9}$", message = "Mobile number must be a valid 10 digit Indian number")
        String mobile) {
}
