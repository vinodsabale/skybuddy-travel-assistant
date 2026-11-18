package com.flight_service.config;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.flight_service.entity.Flight;
import com.flight_service.repository.FlightRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class FlightDataInitializer implements CommandLineRunner {

    private static final int DAYS_TO_SEED = 10;
    private static final int TOTAL_SEATS = 60;

    private final FlightRepository flightRepository;

    private record Route(String flightNumber, String airline, String origin, String destination,
                         LocalTime departure, int durationMinutes, String price) {
    }

    private static final List<Route> ROUTES = List.of(
            new Route("6E-231", "IndiGo", "Mumbai", "Goa", LocalTime.of(7, 30), 75, "3499"),
            new Route("AI-663", "Air India", "Mumbai", "Goa", LocalTime.of(13, 15), 80, "4299"),
            new Route("SG-541", "SpiceJet", "Mumbai", "Goa", LocalTime.of(19, 40), 75, "3199"),
            new Route("6E-232", "IndiGo", "Goa", "Mumbai", LocalTime.of(9, 15), 75, "3399"),
            new Route("AI-664", "Air India", "Goa", "Mumbai", LocalTime.of(17, 50), 80, "4199"),
            new Route("6E-2041", "IndiGo", "Mumbai", "Delhi", LocalTime.of(6, 0), 135, "5499"),
            new Route("AI-101", "Air India", "Mumbai", "Delhi", LocalTime.of(11, 30), 130, "6999"),
            new Route("QP-1101", "Akasa Air", "Mumbai", "Delhi", LocalTime.of(20, 15), 135, "4999"),
            new Route("6E-2042", "IndiGo", "Delhi", "Mumbai", LocalTime.of(8, 0), 135, "5399"),
            new Route("AI-102", "Air India", "Delhi", "Mumbai", LocalTime.of(15, 20), 130, "6799"),
            new Route("6E-6101", "IndiGo", "Pune", "Delhi", LocalTime.of(9, 30), 140, "5299"),
            new Route("6E-6102", "IndiGo", "Delhi", "Pune", LocalTime.of(14, 10), 140, "5199"),
            new Route("6E-5011", "IndiGo", "Mumbai", "Bengaluru", LocalTime.of(8, 20), 105, "3999"),
            new Route("6E-5012", "IndiGo", "Bengaluru", "Mumbai", LocalTime.of(12, 0), 105, "3899"),
            new Route("6E-7301", "IndiGo", "Pune", "Goa", LocalTime.of(10, 10), 70, "2999"),
            new Route("6E-7302", "IndiGo", "Goa", "Pune", LocalTime.of(15, 30), 70, "2899"));

    @Override
    public void run(String... args) {
        LocalDate today = LocalDate.now();
        List<Flight> flights = new ArrayList<>();

        for (int day = 0; day < DAYS_TO_SEED; day++) {
            LocalDate date = today.plusDays(day);

            // seed only the days that have no flights yet (safe to restart many times)
            if (flightRepository.existsByDepartureTimeGreaterThanEqualAndDepartureTimeLessThan(
                    date.atStartOfDay(), date.plusDays(1).atStartOfDay())) {
                continue;
            }

            for (Route route : ROUTES) {
                LocalDateTime departure = date.atTime(route.departure());
                flights.add(Flight.builder()
                        .flightNumber(route.flightNumber())
                        .airline(route.airline())
                        .origin(route.origin())
                        .destination(route.destination())
                        .departureTime(departure)
                        .arrivalTime(departure.plusMinutes(route.durationMinutes()))
                        .price(new BigDecimal(route.price()))
                        .totalSeats(TOTAL_SEATS)
                        .availableSeats(TOTAL_SEATS)
                        .build());
            }
        }

        if (flights.isEmpty()) {
            return;
        }
        flightRepository.saveAll(flights);
        log.info("Seeded {} flights", flights.size());
    }
}
