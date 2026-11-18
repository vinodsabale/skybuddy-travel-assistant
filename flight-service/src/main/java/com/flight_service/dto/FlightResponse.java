package com.flight_service.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FlightResponse(
        Long id,
        String flightNumber,
        String airline,
        String origin,
        String destination,
        LocalDateTime departureTime,
        LocalDateTime arrivalTime,
        BigDecimal price,
        int availableSeats) {
}