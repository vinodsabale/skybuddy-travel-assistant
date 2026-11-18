package com.flight_service.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.flight_service.dto.FlightResponse;
import com.flight_service.exception.BusinessException;
import com.flight_service.exception.ResourceNotFoundException;
import com.flight_service.mapper.FlightMapper;
import com.flight_service.repository.FlightRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FlightService {

    private static final int DEFAULT_SEARCH_WINDOW_DAYS = 30;

    private final FlightRepository flightRepository;
    private final FlightMapper flightMapper;

    @Transactional(readOnly = true)
    public List<FlightResponse> search(String origin, String destination, LocalDate date) {
        if (origin.trim().equalsIgnoreCase(destination.trim())) {
            throw new BusinessException("Origin and destination cannot be the same");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime fromTime;
        LocalDateTime toTime;

        if (date == null) {
            fromTime = now;
            toTime = now.plusDays(DEFAULT_SEARCH_WINDOW_DAYS);
        } else {
            LocalDateTime startOfDay = date.atStartOfDay();
            fromTime = startOfDay.isBefore(now) ? now : startOfDay;
            toTime = date.plusDays(1).atStartOfDay();
        }

        return flightRepository.search(origin.trim(), destination.trim(), fromTime, toTime).stream()
                .map(flightMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public FlightResponse getById(Long id) {
        return flightRepository.findById(id)
                .map(flightMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Flight not found with id " + id));
    }
}
