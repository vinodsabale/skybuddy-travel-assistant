package com.flight_service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.flight_service.entity.Booking;
import com.flight_service.entity.BookingStatus;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByPnr(String pnr);

    boolean existsByPnr(String pnr);

    List<Booking> findByMobileOrderByBookedAtDesc(String mobile);

    @Query("select b.seatNumber from Booking b where b.flight.flightId = :flightId and b.status = :status")
    List<String> findSeatNumbers(@Param("flightId") Long flightId, @Param("status") BookingStatus status);
}
