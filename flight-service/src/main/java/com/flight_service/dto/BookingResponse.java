package com.flight_service.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.flight_service.entity.BookingStatus;

public record BookingResponse(
        String pnr,
        BookingStatus status,
        String passengerName,
        int passengerAge,
        String mobile,
        String seatNumber,
        BigDecimal amountPaid,
        BigDecimal refundAmount,
        LocalDateTime bookedAt,
        FlightResponse flight) {
}
