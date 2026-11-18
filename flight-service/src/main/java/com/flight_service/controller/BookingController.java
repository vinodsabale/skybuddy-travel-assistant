package com.flight_service.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.flight_service.dto.BookingRequest;
import com.flight_service.dto.BookingResponse;
import com.flight_service.dto.ChangeSeatRequest;
import com.flight_service.dto.UpdatePassengerRequest;
import com.flight_service.service.BookingService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse book(@Valid @RequestBody BookingRequest request) {
        return bookingService.book(request);
    }

    @GetMapping("/{pnr}")
    public BookingResponse getByPnr(@PathVariable String pnr) {
        return bookingService.getByPnr(pnr);
    }

    @GetMapping
    public List<BookingResponse> getByMobile(@RequestParam String mobile) {
        return bookingService.getByMobile(mobile);
    }

    @PutMapping("/{pnr}/passenger")
    public BookingResponse updatePassenger(@PathVariable String pnr,
                                           @Valid @RequestBody UpdatePassengerRequest request) {
        return bookingService.updatePassenger(pnr, request);
    }

    @PutMapping("/{pnr}/seat")
    public BookingResponse changeSeat(@PathVariable String pnr,
                                      @Valid @RequestBody ChangeSeatRequest request) {
        return bookingService.changeSeat(pnr, request);
    }

    @PutMapping("/{pnr}/cancel")
    public BookingResponse cancel(@PathVariable String pnr) {
        return bookingService.cancel(pnr);
    }
}
