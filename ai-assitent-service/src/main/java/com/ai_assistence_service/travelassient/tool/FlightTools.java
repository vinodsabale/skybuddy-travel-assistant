package com.ai_assistence_service.travelassient.tool;

import java.util.List;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import com.ai_assistence_service.travelassient.client.FlightServiceClient;
import com.ai_assistence_service.travelassient.dto.BookFlightBody;
import com.ai_assistence_service.travelassient.dto.BookingDto;
import com.ai_assistence_service.travelassient.dto.ChangeSeatBody;
import com.ai_assistence_service.travelassient.dto.FlightDto;
import com.ai_assistence_service.travelassient.dto.UpdatePassengerBody;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FlightTools {

    private final FlightServiceClient flightServiceClient;

    @Tool(description = "Search available flights between two cities. Returns flight id, flight number, airline, "
            + "departure and arrival time, ticket price in INR and available seats. "
            + "If the date is not given, upcoming flights are returned.")
    public List<FlightDto> searchFlights(
            @ToolParam(description = "Departure city, for example Mumbai") String origin,
            @ToolParam(description = "Arrival city, for example Goa") String destination,
            @ToolParam(description = "Travel date in yyyy-MM-dd format", required = false) String date) {
        return flightServiceClient.searchFlights(origin, destination, date);
    }

    @Tool(description = "Book a flight ticket for one passenger. Call this only after the user has confirmed the "
            + "flight and the passenger details. A seat is assigned automatically and a PNR is returned.")
    public BookingDto bookFlight(
            @ToolParam(description = "Flight id taken from the searchFlights result") Long flightId,
            @ToolParam(description = "Full name of the passenger") String passengerName,
            @ToolParam(description = "Age of the passenger in years") Integer passengerAge,
            @ToolParam(description = "10 digit Indian mobile number of the passenger") String mobile) {
        return flightServiceClient.bookFlight(new BookFlightBody(flightId, passengerName, passengerAge, mobile));
    }

    @Tool(description = "Get the full details of a flight booking (status, passenger, seat, flight, price) using the PNR.")
    public BookingDto getBookingByPnr(
            @ToolParam(description = "6 character PNR, for example AB12CD") String pnr) {
        return flightServiceClient.getBooking(pnr);
    }

    @Tool(description = "List all flight bookings made with a given mobile number, newest first.")
    public List<BookingDto> getBookingsByMobile(
            @ToolParam(description = "10 digit mobile number used while booking") String mobile) {
        return flightServiceClient.getBookingsByMobile(mobile);
    }

    @Tool(description = "Update passenger details of a confirmed booking. Pass only the fields that must change.")
    public BookingDto updatePassengerDetails(
            @ToolParam(description = "PNR of the booking") String pnr,
            @ToolParam(description = "New passenger name", required = false) String passengerName,
            @ToolParam(description = "New passenger age", required = false) Integer passengerAge,
            @ToolParam(description = "New 10 digit mobile number", required = false) String mobile) {
        return flightServiceClient.updatePassenger(pnr, new UpdatePassengerBody(passengerName, passengerAge, mobile));
    }

    @Tool(description = "Change the seat of a confirmed booking. Seats look like 12A (row number and a letter from A to F).")
    public BookingDto changeSeat(
            @ToolParam(description = "PNR of the booking") String pnr,
            @ToolParam(description = "New seat number, for example 7C") String seatNumber) {
        return flightServiceClient.changeSeat(pnr, new ChangeSeatBody(seatNumber));
    }

    @Tool(description = "Cancel a confirmed flight booking and return the refund amount. "
            + "Call this only after the user has clearly confirmed the cancellation.")
    public BookingDto cancelBooking(
            @ToolParam(description = "PNR of the booking to cancel") String pnr) {
        return flightServiceClient.cancelBooking(pnr);
    }
}
