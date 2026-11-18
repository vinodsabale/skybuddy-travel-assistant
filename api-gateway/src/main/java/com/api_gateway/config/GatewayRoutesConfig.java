package com.api_gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutesConfig {

    @Bean
    public RouteLocator travelRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("flight-service", r -> r
                        .path("/api/flights/**", "/api/bookings/**")
                        .uri("lb://flight-service"))
                .route("hotel-service", r -> r
                        .path("/api/hotels/**", "/api/hotel-bookings/**")
                        .uri("lb://hotel-service"))
                .route("ai-assistant-service", r -> r
                        .path("/api/ai/**", "/", "/index.html")
                        .uri("lb://ai-assistant-service"))
                .build();
    }
}
