package com.ai_assistence_service.travelassient.dto;

import java.math.BigDecimal;

public record HotelDto(
        Long id,
        String name,
        String city,
        String address,
        BigDecimal pricePerNight,
        double rating,
        String amenities,
        int totalRooms) {
}
