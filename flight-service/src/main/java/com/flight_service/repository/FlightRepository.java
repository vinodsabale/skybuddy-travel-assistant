package com.flight_service.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.flight_service.entity.Flight;

import jakarta.persistence.LockModeType;

public interface FlightRepository extends JpaRepository<Flight, Long> {

    @Query("""
            select f from Flight f
            where lower(f.origin) = lower(:origin)
              and lower(f.destination) = lower(:destination)
              and f.departureTime >= :fromTime
              and f.departureTime < :toTime
              and f.availableSeats > 0
            order by f.departureTime
            """)
    List<Flight> search(@Param("origin") String origin,
                        @Param("destination") String destination,
                        @Param("fromTime") LocalDateTime fromTime,
                        @Param("toTime") LocalDateTime toTime);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select f from Flight f where f.flightId = :id")
    Optional<Flight> findByIdForUpdate(@Param("id") Long id);

    long countByDepartureTimeAfter(LocalDateTime time);

    boolean existsByDepartureTimeGreaterThanEqualAndDepartureTimeLessThan(LocalDateTime from, LocalDateTime to);
}
