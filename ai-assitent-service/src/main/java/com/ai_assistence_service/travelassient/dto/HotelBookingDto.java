package com.ai_assistence_service.travelassient.dto;

import java.math.BigDecimal;

public record HotelBookingDto(
        String reference,
        String status,
        String hotelName,
        String city,
        String guestName,
        String mobile,
        String checkIn,
        String checkOut,
        int rooms,
        BigDecimal totalAmount,
        String bookedAt) {
}
