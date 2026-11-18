package com.hotel_service.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hotel_service.entity.Hotel;

import jakarta.persistence.LockModeType;

public interface HotelRepository extends JpaRepository<Hotel, Long> {

    List<Hotel> findByCityIgnoreCaseOrderByRatingDesc(String city);

    List<Hotel> findByCityIgnoreCaseAndPricePerNightLessThanEqualOrderByRatingDesc(String city, BigDecimal maxPrice);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select h from Hotel h where h.id = :id")
    Optional<Hotel> findByIdForUpdate(@Param("id") Long id);
}
