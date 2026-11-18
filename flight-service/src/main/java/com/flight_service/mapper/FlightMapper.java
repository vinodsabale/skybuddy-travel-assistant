package com.flight_service.mapper;

import org.springframework.stereotype.Component;

import com.flight_service.dto.FlightResponse;
import com.flight_service.entity.Flight;

@Component
public class FlightMapper {

    public FlightResponse toResponse(Flight flight) {
        return new FlightResponse(
                flight.getFlightId(),
                flight.getFlightNumber(),
                flight.getAirline(),
                flight.getOrigin(),
                flight.getDestination(),
                flight.getDepartureTime(),
                flight.getArrivalTime(),
                flight.getPrice(),
                flight.getAvailableSeats());
    }
}
