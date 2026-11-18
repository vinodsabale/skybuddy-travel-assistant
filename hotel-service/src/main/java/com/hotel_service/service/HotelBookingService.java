package com.hotel_service.service;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotel_service.dto.HotelBookingRequest;
import com.hotel_service.dto.HotelBookingResponse;
import com.hotel_service.entity.Hotel;
import com.hotel_service.entity.HotelBooking;
import com.hotel_service.entity.HotelBookingStatus;
import com.hotel_service.exception.BusinessException;
import com.hotel_service.exception.ResourceNotFoundException;
import com.hotel_service.mapper.HotelMapper;
import com.hotel_service.repository.HotelBookingRepository;
import com.hotel_service.repository.HotelRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HotelBookingService {

    private static final String REFERENCE_PREFIX = "HTL";
    private static final String REFERENCE_CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int REFERENCE_SUFFIX_LENGTH = 7;

    private final HotelRepository hotelRepository;
    private final HotelBookingRepository hotelBookingRepository;
    private final HotelMapper hotelMapper;
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public HotelBookingResponse book(HotelBookingRequest request) {
        LocalDate today = LocalDate.now();
        if (request.checkIn().isBefore(today)) {
            throw new BusinessException("Check-in date cannot be in the past");
        }
        if (!request.checkOut().isAfter(request.checkIn())) {
            throw new BusinessException("Check-out date must be after check-in date");
        }

        Hotel hotel = hotelRepository.findByIdForUpdate(request.hotelId())
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id " + request.hotelId()));

        long alreadyBooked = hotelBookingRepository.countBookedRooms(
                hotel.getId(), HotelBookingStatus.CONFIRMED, request.checkIn(), request.checkOut());
        long roomsLeft = hotel.getTotalRooms() - alreadyBooked;
        if (request.rooms() > roomsLeft) {
            throw new BusinessException("Only " + Math.max(roomsLeft, 0) + " room(s) available at "
                    + hotel.getName() + " for the selected dates");
        }

        long nights = ChronoUnit.DAYS.between(request.checkIn(), request.checkOut());
        BigDecimal totalAmount = hotel.getPricePerNight()
                .multiply(BigDecimal.valueOf(nights))
                .multiply(BigDecimal.valueOf(request.rooms()));

        HotelBooking booking = HotelBooking.builder()
                .reference(generateUniqueReference())
                .hotel(hotel)
                .guestName(request.guestName().trim())
                .mobile(request.mobile())
                .checkIn(request.checkIn())
                .checkOut(request.checkOut())
                .rooms(request.rooms())
                .totalAmount(totalAmount)
                .status(HotelBookingStatus.CONFIRMED)
                .bookedAt(LocalDateTime.now())
                .build();

        return hotelMapper.toResponse(hotelBookingRepository.save(booking));
    }

    @Transactional(readOnly = true)
    public HotelBookingResponse getByReference(String reference) {
        return hotelMapper.toResponse(findByReference(reference));
    }

    @Transactional
    public HotelBookingResponse cancel(String reference) {
        HotelBooking booking = findByReference(reference);

        if (booking.getStatus() == HotelBookingStatus.CANCELLED) {
            throw new BusinessException("Booking " + booking.getReference() + " is already cancelled");
        }
        if (!booking.getCheckIn().isAfter(LocalDate.now())) {
            throw new BusinessException("Stay has already started, booking cannot be cancelled");
        }

        booking.setStatus(HotelBookingStatus.CANCELLED);
        booking.setCancelledAt(LocalDateTime.now());
        return hotelMapper.toResponse(hotelBookingRepository.save(booking));
    }

    private HotelBooking findByReference(String reference) {
        return hotelBookingRepository.findByReference(reference.trim().toUpperCase(Locale.ROOT))
                .orElseThrow(() -> new ResourceNotFoundException("Hotel booking not found for reference " + reference));
    }

    private String generateUniqueReference() {
        String reference;
        do {
            StringBuilder builder = new StringBuilder(REFERENCE_PREFIX);
            for (int i = 0; i < REFERENCE_SUFFIX_LENGTH; i++) {
                builder.append(REFERENCE_CHARACTERS.charAt(secureRandom.nextInt(REFERENCE_CHARACTERS.length())));
            }
            reference = builder.toString();
        } while (hotelBookingRepository.existsByReference(reference));
        return reference;
    }
}
