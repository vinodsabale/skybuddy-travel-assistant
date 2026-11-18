package com.ai_assistence_service.travelassient.dto;

public record HotelBookingBody(
        Long hotelId,
        String guestName,
        String mobile,
        String checkIn,
        String checkOut,
        Integer rooms) {
}
