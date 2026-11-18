package com.hotel_service.dto;

import java.math.BigDecimal;

public record HotelResponse(
        Long id,
        String name,
        String city,
        String address,
        BigDecimal pricePerNight,
        double rating,
        String amenities,
        int totalRooms) {
}
