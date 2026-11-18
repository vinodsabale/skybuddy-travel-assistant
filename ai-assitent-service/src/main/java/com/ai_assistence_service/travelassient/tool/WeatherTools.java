package com.ai_assistence_service.travelassient.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import com.ai_assistence_service.travelassient.client.WeatherClient;
import com.ai_assistence_service.travelassient.dto.WeatherReport;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WeatherTools {

    private final WeatherClient weatherClient;

    @Tool(description = "Get the current weather and the 3 day forecast (temperature in Celsius, rain chance, "
            + "wind speed) of a city.")
    public WeatherReport getWeather(
            @ToolParam(description = "City name, for example Goa") String city) {
        return weatherClient.getWeather(city);
    }
}
