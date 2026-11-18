package com.hotel_service.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.hotel_service.entity.HotelBookingStatus;

public record HotelBookingResponse(
        String reference,
        HotelBookingStatus status,
        String hotelName,
        String city,
        String guestName,
        String mobile,
        LocalDate checkIn,
        LocalDate checkOut,
        int rooms,
        BigDecimal totalAmount,
        LocalDateTime bookedAt) {
}
