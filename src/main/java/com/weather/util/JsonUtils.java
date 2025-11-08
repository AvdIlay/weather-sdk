package com.weather.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.weather.api.WeatherData;

public final class JsonUtils {

    private static final ObjectMapper mapper = new ObjectMapper();

    private JsonUtils() {}

    public static WeatherData parseWeather(String json) throws Exception {
        JsonNode node = mapper.readTree(json);

        var weather = new WeatherData.Weather(
                node.get("weather").get(0).get("main").asText(),
                node.get("weather").get(0).get("description").asText()
        );

        var temperature = new WeatherData.Temperature(
                node.get("main").get("temp").asDouble(),
                node.get("main").get("feels_like").asDouble()
        );

        var wind = new WeatherData.Wind(
                node.get("wind").get("speed").asDouble()
        );

        var sys = new WeatherData.Sys(
                node.get("sys").get("sunrise").asLong(),
                node.get("sys").get("sunset").asLong()
        );

        return new WeatherData(
                weather,
                temperature,
                node.get("visibility").asInt(),
                wind,
                node.get("dt").asLong(),
                sys,
                node.get("timezone").asInt(),
                node.get("name").asText()
        );
    }

    public static String toJson(Object obj) throws Exception {
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
    }
}
