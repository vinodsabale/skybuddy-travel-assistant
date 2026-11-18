package com.ai_assistence_service.travelassient.dto;

import java.math.BigDecimal;

public record FlightDto(
        Long id,
        String flightNumber,
        String airline,
        String origin,
        String destination,
        String departureTime,
        String arrivalTime,
        BigDecimal price,
        int availableSeats) {
}
