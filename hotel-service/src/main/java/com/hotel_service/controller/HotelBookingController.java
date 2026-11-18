package com.hotel_service.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.hotel_service.dto.HotelBookingRequest;
import com.hotel_service.dto.HotelBookingResponse;
import com.hotel_service.service.HotelBookingService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/hotel-bookings")
@RequiredArgsConstructor
public class HotelBookingController {

    private final HotelBookingService hotelBookingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HotelBookingResponse book(@Valid @RequestBody HotelBookingRequest request) {
        return hotelBookingService.book(request);
    }

    @GetMapping("/{reference}")
    public HotelBookingResponse getByReference(@PathVariable String reference) {
        return hotelBookingService.getByReference(reference);
    }

    @PutMapping("/{reference}/cancel")
    public HotelBookingResponse cancel(@PathVariable String reference) {
        return hotelBookingService.cancel(reference);
    }
}
