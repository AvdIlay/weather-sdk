package com.weather.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.Properties;

public record WeatherApiConfig(
        String apiKey,
        String baseUrl,
        String units,
        String lang
) {
    private static final Logger log = LoggerFactory.getLogger(WeatherApiConfig.class);

    public static WeatherApiConfig load() {
        Properties props = new Properties();

        try (var input = WeatherApiConfig.class.getClassLoader()
                .getResourceAsStream("weather.properties")) {
            if (input != null) props.load(input);
        } catch (IOException e) {
            log.warn("Could not load weather.properties: {}", e.getMessage());
        }

        String key = System.getenv("WEATHER_API_KEY");
        if (key == null) key = props.getProperty("weather.api.key");

        String url = System.getenv("WEATHER_API_URL");
        if (url == null) url = props.getProperty("weather.api.url");

        String units = System.getenv("WEATHER_API_UNITS");
        if (units == null) units = props.getProperty("weather.api.units");

        String lang = System.getenv("WEATHER_API_LANG");
        if (lang == null) lang = props.getProperty("weather.api.lang");

        return new WeatherApiConfig(key, url, units, lang);
    }
}
