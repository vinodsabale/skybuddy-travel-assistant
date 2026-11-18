package com.hotel_service.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record HotelBookingRequest(
        @NotNull(message = "hotelId is required")
        Long hotelId,

        @NotBlank(message = "Guest name is required")
        @Size(max = 100, message = "Guest name must be at most 100 characters")
        String guestName,

        @NotBlank(message = "Mobile number is required")
        @Pattern(regexp = "^[6-9]\\d{9}$", message = "Mobile number must be a valid 10 digit Indian number")
        String mobile,

        @NotNull(message = "checkIn date is required")
        LocalDate checkIn,

        @NotNull(message = "checkOut date is required")
        LocalDate checkOut,

        @NotNull(message = "Number of rooms is required")
        @Min(value = 1, message = "At least 1 room is required")
        @Max(value = 5, message = "At most 5 rooms can be booked at once")
        Integer rooms) {
}
