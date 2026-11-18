package com.ai_assistence_service.travelassient.dto;

import java.util.List;

public record WeatherReport(String city, String country, Current current, List<DailyForecast> forecast) {

    public record Current(double temperatureCelsius, int humidityPercent, double windSpeedKmh, String condition) {
    }

    public record DailyForecast(String date, String condition, double minTempCelsius,
                                double maxTempCelsius, Integer rainChancePercent) {
    }
}
