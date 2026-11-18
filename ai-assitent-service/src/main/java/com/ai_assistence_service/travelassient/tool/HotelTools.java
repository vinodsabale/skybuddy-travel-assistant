package com.ai_assistence_service.travelassient.tool;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import com.ai_assistence_service.travelassient.client.HotelServiceClient;
import com.ai_assistence_service.travelassient.dto.HotelBookingBody;
import com.ai_assistence_service.travelassient.dto.HotelBookingDto;
import com.ai_assistence_service.travelassient.dto.HotelDto;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class HotelTools {

    private final HotelServiceClient hotelServiceClient;

    @Tool(description = "Search hotels in a city, best rated first. Returns hotel id, name, address, price per night "
            + "in INR, rating and amenities.")
    public List<HotelDto> searchHotels(
            @ToolParam(description = "City name, for example Goa") String city,
            @ToolParam(description = "Maximum price per night in INR", required = false) BigDecimal maxPricePerNight) {
        return hotelServiceClient.searchHotels(city, maxPricePerNight);
    }

    @Tool(description = "Book a hotel. Call this only after the user has confirmed the hotel, dates and guest details.")
    public HotelBookingDto bookHotel(
            @ToolParam(description = "Hotel id taken from the searchHotels result") Long hotelId,
            @ToolParam(description = "Full name of the guest") String guestName,
            @ToolParam(description = "10 digit Indian mobile number of the guest") String mobile,
            @ToolParam(description = "Check-in date in yyyy-MM-dd format") String checkIn,
            @ToolParam(description = "Check-out date in yyyy-MM-dd format") String checkOut,
            @ToolParam(description = "Number of rooms (1 to 5)") Integer rooms) {
        return hotelServiceClient.bookHotel(new HotelBookingBody(hotelId, guestName, mobile, checkIn, checkOut, rooms));
    }

    @Tool(description = "Get the details of a hotel booking using its reference number, for example HTLAB12CD3.")
    public HotelBookingDto getHotelBooking(
            @ToolParam(description = "Hotel booking reference") String reference) {
        return hotelServiceClient.getHotelBooking(reference);
    }

    @Tool(description = "Cancel a hotel booking. Call this only after the user has clearly confirmed the cancellation.")
    public HotelBookingDto cancelHotelBooking(
            @ToolParam(description = "Hotel booking reference") String reference) {
        return hotelServiceClient.cancelHotelBooking(reference);
    }
}
