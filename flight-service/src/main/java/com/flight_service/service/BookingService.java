package com.flight_service.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.flight_service.dto.BookingRequest;
import com.flight_service.dto.BookingResponse;
import com.flight_service.dto.ChangeSeatRequest;
import com.flight_service.dto.UpdatePassengerRequest;
import com.flight_service.entity.Booking;
import com.flight_service.entity.BookingStatus;
import com.flight_service.entity.Flight;
import com.flight_service.exception.BusinessException;
import com.flight_service.exception.ResourceNotFoundException;
import com.flight_service.mapper.BookingMapper;
import com.flight_service.repository.BookingRepository;
import com.flight_service.repository.FlightRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookingService {

    private static final String SEAT_LETTERS = "ABCDEF";
    private static final String PNR_CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int PNR_LENGTH = 6;
    private static final long FULL_REFUND_WINDOW_HOURS = 24;
    private static final BigDecimal REFUND_PERCENT_EARLY = BigDecimal.valueOf(80);
    private static final BigDecimal REFUND_PERCENT_LATE = BigDecimal.valueOf(50);
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final BookingRepository bookingRepository;
    private final FlightRepository flightRepository;
    private final BookingMapper bookingMapper;
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public BookingResponse book(BookingRequest request) {
        Flight flight = lockFlight(request.flightId());

        if (!flight.getDepartureTime().isAfter(LocalDateTime.now())) {
            throw new BusinessException("Flight " + flight.getFlightNumber() + " has already departed");
        }
        if (flight.getAvailableSeats() <= 0) {
            throw new BusinessException("No seats available on flight " + flight.getFlightNumber());
        }

        Set<String> takenSeats = confirmedSeats(flight.getFlightId());
        String seat = allSeats(flight.getTotalSeats()).stream()
                .filter(candidate -> !takenSeats.contains(candidate))
                .findFirst()
                .orElseThrow(() -> new BusinessException("No seats available on flight " + flight.getFlightNumber()));

        flight.setAvailableSeats(flight.getAvailableSeats() - 1);

        Booking booking = Booking.builder()
                .pnr(generateUniquePnr())
                .flight(flight)
                .passengerName(request.passengerName().trim())
                .passengerAge(request.passengerAge())
                .mobile(request.mobile())
                .seatNumber(seat)
                .status(BookingStatus.CONFIRMED)
                .amountPaid(flight.getPrice())
                .bookedAt(LocalDateTime.now())
                .build();

        return bookingMapper.toResponse(bookingRepository.save(booking));
    }

    @Transactional(readOnly = true)
    public BookingResponse getByPnr(String pnr) {
        return bookingMapper.toResponse(findByPnr(pnr));
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getByMobile(String mobile) {
        return bookingRepository.findByMobileOrderByBookedAtDesc(mobile).stream()
                .map(bookingMapper::toResponse)
                .toList();
    }

    @Transactional
    public BookingResponse updatePassenger(String pnr, UpdatePassengerRequest request) {
        Booking booking = findConfirmedBooking(pnr);

        boolean nothingToUpdate = isBlank(request.passengerName())
                && request.passengerAge() == null
                && isBlank(request.mobile());
        if (nothingToUpdate) {
            throw new BusinessException("Provide at least one of passengerName, passengerAge or mobile to update");
        }

        if (!isBlank(request.passengerName())) {
            booking.setPassengerName(request.passengerName().trim());
        }
        if (request.passengerAge() != null) {
            booking.setPassengerAge(request.passengerAge());
        }
        if (!isBlank(request.mobile())) {
            booking.setMobile(request.mobile());
        }
        return bookingMapper.toResponse(bookingRepository.save(booking));
    }

    @Transactional
    public BookingResponse changeSeat(String pnr, ChangeSeatRequest request) {
        Booking booking = findConfirmedBooking(pnr);
        Flight flight = lockFlight(booking.getFlight().getFlightId());

        if (!flight.getDepartureTime().isAfter(LocalDateTime.now())) {
            throw new BusinessException("Flight has already departed, seat cannot be changed");
        }

        String requestedSeat = request.seatNumber().toUpperCase(Locale.ROOT);
        if (!allSeats(flight.getTotalSeats()).contains(requestedSeat)) {
            throw new BusinessException("Seat " + requestedSeat + " does not exist on this flight");
        }
        if (requestedSeat.equals(booking.getSeatNumber())) {
            throw new BusinessException("You are already seated at " + requestedSeat);
        }
        if (confirmedSeats(flight.getFlightId()).contains(requestedSeat)) {
            throw new BusinessException("Seat " + requestedSeat + " is already booked");
        }

        booking.setSeatNumber(requestedSeat);
        return bookingMapper.toResponse(bookingRepository.save(booking));
    }

    @Transactional
    public BookingResponse cancel(String pnr) {
        Booking booking = findConfirmedBooking(pnr);
        Flight flight = lockFlight(booking.getFlight().getFlightId());

        LocalDateTime now = LocalDateTime.now();
        if (!flight.getDepartureTime().isAfter(now)) {
            throw new BusinessException("Flight has already departed, booking cannot be cancelled");
        }

        long hoursToDeparture = Duration.between(now, flight.getDepartureTime()).toHours();
        BigDecimal refundPercent = hoursToDeparture >= FULL_REFUND_WINDOW_HOURS
                ? REFUND_PERCENT_EARLY
                : REFUND_PERCENT_LATE;
        BigDecimal refund = booking.getAmountPaid()
                .multiply(refundPercent)
                .divide(HUNDRED, 2, RoundingMode.HALF_UP);

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setRefundAmount(refund);
        booking.setCancelledAt(now);
        flight.setAvailableSeats(flight.getAvailableSeats() + 1);

        return bookingMapper.toResponse(bookingRepository.save(booking));
    }

    // ---------------------------------------------------------------- helpers

    private Flight lockFlight(Long flightId) {
        return flightRepository.findByIdForUpdate(flightId)
                .orElseThrow(() -> new ResourceNotFoundException("Flight not found with id " + flightId));
    }

    private Booking findByPnr(String pnr) {
        return bookingRepository.findByPnr(pnr.trim().toUpperCase(Locale.ROOT))
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found for PNR " + pnr));
    }

    private Booking findConfirmedBooking(String pnr) {
        Booking booking = findByPnr(pnr);
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BusinessException("Booking " + booking.getPnr() + " is already cancelled");
        }
        return booking;
    }

    private Set<String> confirmedSeats(Long flightId) {
        return new HashSet<>(bookingRepository.findSeatNumbers(flightId, BookingStatus.CONFIRMED));
    }

    private List<String> allSeats(int totalSeats) {
        List<String> seats = new ArrayList<>(totalSeats);
        for (int index = 0; index < totalSeats; index++) {
            int row = index / SEAT_LETTERS.length() + 1;
            char letter = SEAT_LETTERS.charAt(index % SEAT_LETTERS.length());
            seats.add(row + String.valueOf(letter));
        }
        return seats;
    }

    private String generateUniquePnr() {
        String pnr;
        do {
            StringBuilder builder = new StringBuilder(PNR_LENGTH);
            for (int i = 0; i < PNR_LENGTH; i++) {
                builder.append(PNR_CHARACTERS.charAt(secureRandom.nextInt(PNR_CHARACTERS.length())));
            }
            pnr = builder.toString();
        } while (bookingRepository.existsByPnr(pnr));
        return pnr;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
