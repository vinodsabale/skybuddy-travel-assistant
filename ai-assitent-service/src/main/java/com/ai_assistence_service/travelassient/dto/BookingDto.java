package com.ai_assistence_service.travelassient.dto;

import java.math.BigDecimal;

public record BookingDto(
        String pnr,
        String status,
        String passengerName,
        int passengerAge,
        String mobile,
        String seatNumber,
        BigDecimal amountPaid,
        BigDecimal refundAmount,
        String bookedAt,
        FlightDto flight) {
}
