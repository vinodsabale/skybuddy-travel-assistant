package com.hotel_service.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hotel_service.dto.HotelResponse;
import com.hotel_service.service.HotelService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/hotels")
@RequiredArgsConstructor
public class HotelController {

    private final HotelService hotelService;

    @GetMapping
    public List<HotelResponse> search(
            @RequestParam String city,
            @RequestParam(required = false) BigDecimal maxPrice) {
        return hotelService.search(city, maxPrice);
    }

    @GetMapping("/{id}")
    public HotelResponse getById(@PathVariable Long id) {
        return hotelService.getById(id);
    }
}
