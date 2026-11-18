package com.ai_assistence_service.travelassient.client;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.ai_assistence_service.travelassient.dto.BookFlightBody;
import com.ai_assistence_service.travelassient.dto.BookingDto;
import com.ai_assistence_service.travelassient.dto.ChangeSeatBody;
import com.ai_assistence_service.travelassient.dto.FlightDto;
import com.ai_assistence_service.travelassient.dto.UpdatePassengerBody;

@Component
public class FlightServiceClient {

    private static final String SERVICE_NAME = "Flight service";

    private final RestClient restClient;

    public FlightServiceClient(@LoadBalanced RestClient.Builder builder,
                               @Value("${travel.services.flight-url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    public List<FlightDto> searchFlights(String origin, String destination, String date) {
        return RemoteCall.execute(SERVICE_NAME, () -> restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/flights")
                        .queryParam("origin", origin)
                        .queryParam("destination", destination)
                        .queryParamIfPresent("date", Optional.ofNullable(date).filter(value -> !value.isBlank()))
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<List<FlightDto>>() {
                }));
    }

    public BookingDto bookFlight(BookFlightBody body) {
        return RemoteCall.execute(SERVICE_NAME, () -> restClient.post()
                .uri("/api/bookings")
                .body(body)
                .retrieve()
                .body(BookingDto.class));
    }

    public BookingDto getBooking(String pnr) {
        return RemoteCall.execute(SERVICE_NAME, () -> restClient.get()
                .uri("/api/bookings/{pnr}", pnr)
                .retrieve()
                .body(BookingDto.class));
    }

    public List<BookingDto> getBookingsByMobile(String mobile) {
        return RemoteCall.execute(SERVICE_NAME, () -> restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/bookings").queryParam("mobile", mobile).build())
                .retrieve()
                .body(new ParameterizedTypeReference<List<BookingDto>>() {
                }));
    }

    public BookingDto updatePassenger(String pnr, UpdatePassengerBody body) {
        return RemoteCall.execute(SERVICE_NAME, () -> restClient.put()
                .uri("/api/bookings/{pnr}/passenger", pnr)
                .body(body)
                .retrieve()
                .body(BookingDto.class));
    }

    public BookingDto changeSeat(String pnr, ChangeSeatBody body) {
        return RemoteCall.execute(SERVICE_NAME, () -> restClient.put()
                .uri("/api/bookings/{pnr}/seat", pnr)
                .body(body)
                .retrieve()
                .body(BookingDto.class));
    }

    public BookingDto cancelBooking(String pnr) {
        return RemoteCall.execute(SERVICE_NAME, () -> restClient.put()
                .uri("/api/bookings/{pnr}/cancel", pnr)
                .retrieve()
                .body(BookingDto.class));
    }
}
