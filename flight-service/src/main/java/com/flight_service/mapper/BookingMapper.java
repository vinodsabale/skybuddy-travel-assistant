package com.flight_service.mapper;

import org.springframework.stereotype.Component;

import com.flight_service.dto.BookingResponse;
import com.flight_service.entity.Booking;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BookingMapper {

    private final FlightMapper flightMapper;

    public BookingResponse toResponse(Booking booking) {
        return new BookingResponse(
                booking.getPnr(),
                booking.getStatus(),
                booking.getPassengerName(),
                booking.getPassengerAge(),
                booking.getMobile(),
                booking.getSeatNumber(),
                booking.getAmountPaid(),
                booking.getRefundAmount(),
                booking.getBookedAt(),
                flightMapper.toResponse(booking.getFlight()));
    }
}
