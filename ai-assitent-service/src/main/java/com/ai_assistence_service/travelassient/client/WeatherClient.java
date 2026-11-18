package com.ai_assistence_service.travelassient.client;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.ai_assistence_service.travelassient.dto.WeatherReport;
import com.ai_assistence_service.travelassient.exception.TravelServiceException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Free Open-Meteo APIs (no API key needed): geocoding to find the city, forecast for the weather.
 */
@Component
public class WeatherClient {

    private static final String SERVICE_NAME = "Weather service";
    private static final int FORECAST_DAYS = 3;

    private final RestClient geocodingClient = RestClient.create("https://geocoding-api.open-meteo.com");
    private final RestClient forecastClient = RestClient.create("https://api.open-meteo.com");

    public WeatherReport getWeather(String city) {
        GeoResponse geo = RemoteCall.execute(SERVICE_NAME, () -> geocodingClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/search")
                        .queryParam("name", city)
                        .queryParam("count", 1)
                        .queryParam("language", "en")
                        .build())
                .retrieve()
                .body(GeoResponse.class));

        if (geo == null || geo.results() == null || geo.results().isEmpty()) {
            throw new TravelServiceException("Could not find a city named " + city);
        }
        GeoResult place = geo.results().get(0);

        ForecastResponse forecast = RemoteCall.execute(SERVICE_NAME, () -> forecastClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/forecast")
                        .queryParam("latitude", place.latitude())
                        .queryParam("longitude", place.longitude())
                        .queryParam("current", "temperature_2m,relative_humidity_2m,wind_speed_10m,weather_code")
                        .queryParam("daily", "weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max")
                        .queryParam("timezone", "auto")
                        .queryParam("forecast_days", FORECAST_DAYS)
                        .build())
                .retrieve()
                .body(ForecastResponse.class));

        if (forecast == null || forecast.current() == null || forecast.daily() == null) {
            throw new TravelServiceException("Weather data is not available for " + city);
        }

        Current current = forecast.current();
        WeatherReport.Current currentReport = new WeatherReport.Current(
                current.temperature(),
                current.humidity(),
                current.windSpeed(),
                describe(current.weatherCode()));

        Daily daily = forecast.daily();
        List<WeatherReport.DailyForecast> days = new ArrayList<>();
        for (int i = 0; i < daily.time().size(); i++) {
            days.add(new WeatherReport.DailyForecast(
                    daily.time().get(i),
                    describe(daily.weatherCode().get(i)),
                    daily.minTemp().get(i),
                    daily.maxTemp().get(i),
                    daily.rainChance() == null ? null : daily.rainChance().get(i)));
        }

        return new WeatherReport(place.name(), place.country(), currentReport, days);
    }

    private String describe(int code) {
        return switch (code) {
            case 0 -> "Clear sky";
            case 1 -> "Mainly clear";
            case 2 -> "Partly cloudy";
            case 3 -> "Overcast";
            case 45, 48 -> "Fog";
            case 51, 53, 55 -> "Drizzle";
            case 56, 57 -> "Freezing drizzle";
            case 61, 63, 65 -> "Rain";
            case 66, 67 -> "Freezing rain";
            case 71, 73, 75, 77 -> "Snow";
            case 80, 81, 82 -> "Rain showers";
            case 85, 86 -> "Snow showers";
            case 95, 96, 99 -> "Thunderstorm";
            default -> "Unknown";
        };
    }

    // ------------------------------------------------ Open-Meteo response models

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GeoResponse(List<GeoResult> results) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GeoResult(String name, String country, double latitude, double longitude) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ForecastResponse(Current current, Daily daily) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Current(
            @JsonProperty("temperature_2m") double temperature,
            @JsonProperty("relative_humidity_2m") int humidity,
            @JsonProperty("wind_speed_10m") double windSpeed,
            @JsonProperty("weather_code") int weatherCode) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Daily(
            List<String> time,
            @JsonProperty("weather_code") List<Integer> weatherCode,
            @JsonProperty("temperature_2m_max") List<Double> maxTemp,
            @JsonProperty("temperature_2m_min") List<Double> minTemp,
            @JsonProperty("precipitation_probability_max") List<Integer> rainChance) {
    }
}
