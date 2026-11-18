package com.hotel_service.mapper;

import org.springframework.stereotype.Component;

import com.hotel_service.dto.HotelBookingResponse;
import com.hotel_service.dto.HotelResponse;
import com.hotel_service.entity.Hotel;
import com.hotel_service.entity.HotelBooking;

@Component
public class HotelMapper {

    public HotelResponse toResponse(Hotel hotel) {
        return new HotelResponse(
                hotel.getId(),
                hotel.getName(),
                hotel.getCity(),
                hotel.getAddress(),
                hotel.getPricePerNight(),
                hotel.getRating(),
                hotel.getAmenities(),
                hotel.getTotalRooms());
    }

    public HotelBookingResponse toResponse(HotelBooking booking) {
        return new HotelBookingResponse(
                booking.getReference(),
                booking.getStatus(),
                booking.getHotel().getName(),
                booking.getHotel().getCity(),
                booking.getGuestName(),
                booking.getMobile(),
                booking.getCheckIn(),
                booking.getCheckOut(),
                booking.getRooms(),
                booking.getTotalAmount(),
                booking.getBookedAt());
    }
}
