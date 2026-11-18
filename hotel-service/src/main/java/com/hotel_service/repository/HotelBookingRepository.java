package com.hotel_service.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hotel_service.entity.HotelBooking;
import com.hotel_service.entity.HotelBookingStatus;

public interface HotelBookingRepository extends JpaRepository<HotelBooking, Long> {

    Optional<HotelBooking> findByReference(String reference);

    boolean existsByReference(String reference);

    @Query("""
            select coalesce(sum(b.rooms), 0L) from HotelBooking b
            where b.hotel.id = :hotelId
              and b.status = :status
              and b.checkIn < :checkOut
              and b.checkOut > :checkIn
            """)
    Long countBookedRooms(@Param("hotelId") Long hotelId,
                          @Param("status") HotelBookingStatus status,
                          @Param("checkIn") LocalDate checkIn,
                          @Param("checkOut") LocalDate checkOut);
}
