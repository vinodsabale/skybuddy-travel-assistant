package com.hotel_service.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotel_service.dto.HotelResponse;
import com.hotel_service.entity.Hotel;
import com.hotel_service.exception.ResourceNotFoundException;
import com.hotel_service.mapper.HotelMapper;
import com.hotel_service.repository.HotelRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HotelService {

    private final HotelRepository hotelRepository;
    private final HotelMapper hotelMapper;

    @Transactional(readOnly = true)
    public List<HotelResponse> search(String city, BigDecimal maxPrice) {
        List<Hotel> hotels = maxPrice == null
                ? hotelRepository.findByCityIgnoreCaseOrderByRatingDesc(city.trim())
                : hotelRepository.findByCityIgnoreCaseAndPricePerNightLessThanEqualOrderByRatingDesc(city.trim(), maxPrice);
        return hotels.stream().map(hotelMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public HotelResponse getById(Long id) {
        return hotelRepository.findById(id)
                .map(hotelMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id " + id));
    }
}
