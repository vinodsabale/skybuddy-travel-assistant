package com.hotel_service.config;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.hotel_service.entity.Hotel;
import com.hotel_service.repository.HotelRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class HotelDataInitializer implements CommandLineRunner {

    private final HotelRepository hotelRepository;

    @Override
    public void run(String... args) {
        if (hotelRepository.count() > 0) {
            return;
        }

        List<Hotel> hotels = List.of(
                hotel("Taj Fort Aguada Resort", "Goa", "Sinquerim, Candolim", "12500", 4.8, "Beach access, Pool, Spa, Free breakfast, WiFi", 40),
                hotel("Calangute Beach Residency", "Goa", "Calangute Beach Road", "4200", 4.1, "Pool, Free breakfast, WiFi", 30),
                hotel("Baga Budget Inn", "Goa", "Baga, Bardez", "2200", 3.8, "WiFi, AC rooms", 25),
                hotel("Candolim Palms Boutique", "Goa", "Candolim", "6800", 4.4, "Pool, Restaurant, WiFi, Airport pickup", 20),
                hotel("The Gateway Seaview", "Mumbai", "Marine Drive", "9800", 4.6, "Sea view, Gym, Restaurant, WiFi", 50),
                hotel("Andheri Comfort Suites", "Mumbai", "Andheri East", "3900", 4.0, "Free breakfast, WiFi, Airport shuttle", 35),
                hotel("Juhu Beachfront Hotel", "Mumbai", "Juhu Tara Road", "7600", 4.3, "Beach view, Pool, Spa, WiFi", 28),
                hotel("Connaught Grand", "Delhi", "Connaught Place", "8800", 4.5, "Gym, Restaurant, WiFi, Metro access", 45),
                hotel("Aerocity Business Hotel", "Delhi", "Aerocity", "6200", 4.2, "Airport shuttle, Gym, WiFi", 40),
                hotel("Paharganj Backpackers Stay", "Delhi", "Paharganj", "1800", 3.6, "WiFi, Common kitchen", 20),
                hotel("Koregaon Park Residency", "Pune", "Koregaon Park", "5200", 4.3, "Pool, Restaurant, WiFi", 30),
                hotel("Shivajinagar Stay Inn", "Pune", "Shivajinagar", "2600", 3.9, "WiFi, Free breakfast", 25),
                hotel("MG Road Executive", "Bengaluru", "MG Road", "7200", 4.5, "Gym, Restaurant, WiFi, Rooftop bar", 38),
                hotel("Indiranagar Urban Stay", "Bengaluru", "Indiranagar", "4100", 4.1, "WiFi, Free breakfast", 28));

        hotelRepository.saveAll(hotels);
        log.info("Seeded {} hotels", hotels.size());
    }

    private Hotel hotel(String name, String city, String address, String price,
                        double rating, String amenities, int totalRooms) {
        return Hotel.builder()
                .name(name)
                .city(city)
                .address(address)
                .pricePerNight(new BigDecimal(price))
                .rating(rating)
                .amenities(amenities)
                .totalRooms(totalRooms)
                .build();
    }
}
