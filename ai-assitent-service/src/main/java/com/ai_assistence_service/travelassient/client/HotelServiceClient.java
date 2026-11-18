package com.ai_assistence_service.travelassient.client;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.ai_assistence_service.travelassient.dto.HotelBookingBody;
import com.ai_assistence_service.travelassient.dto.HotelBookingDto;
import com.ai_assistence_service.travelassient.dto.HotelDto;

@Component
public class HotelServiceClient {

    private static final String SERVICE_NAME = "Hotel service";

    private final RestClient restClient;

    public HotelServiceClient(@LoadBalanced RestClient.Builder builder,
                              @Value("${travel.services.hotel-url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    public List<HotelDto> searchHotels(String city, BigDecimal maxPrice) {
        return RemoteCall.execute(SERVICE_NAME, () -> restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/hotels")
                        .queryParam("city", city)
                        .queryParamIfPresent("maxPrice", Optional.ofNullable(maxPrice))
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<List<HotelDto>>() {
                }));
    }

    public HotelBookingDto bookHotel(HotelBookingBody body) {
        return RemoteCall.execute(SERVICE_NAME, () -> restClient.post()
                .uri("/api/hotel-bookings")
                .body(body)
                .retrieve()
                .body(HotelBookingDto.class));
    }

    public HotelBookingDto getHotelBooking(String reference) {
        return RemoteCall.execute(SERVICE_NAME, () -> restClient.get()
                .uri("/api/hotel-bookings/{reference}", reference)
                .retrieve()
                .body(HotelBookingDto.class));
    }

    public HotelBookingDto cancelHotelBooking(String reference) {
        return RemoteCall.execute(SERVICE_NAME, () -> restClient.put()
                .uri("/api/hotel-bookings/{reference}/cancel", reference)
                .retrieve()
                .body(HotelBookingDto.class));
    }
}
