package com.weather.config;

import com.weather.api.WorkMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public record WeatherSdkConfig(
        int cacheSize,
        int ttlMinutes,
        WorkMode workMode,
        List<String> cities

) {
    private static final Logger log = LoggerFactory.getLogger(WeatherSdkConfig.class);

    public static WeatherSdkConfig defaults() {
        int cacheSize = Integer.parseInt(System.getenv().getOrDefault("WEATHER_CACHE_SIZE", "5"));
        int ttlMinutes = Integer.parseInt(System.getenv().getOrDefault("WEATHER_TTL_MINUTES", "3"));
        String modeEnv = System.getenv().getOrDefault("WEATHER_MODE", "ON_DEMAND");
        String cityEnv = System.getenv().getOrDefault("WEATHER_CITY", "London");

        WorkMode mode;
        try {
            mode = WorkMode.valueOf(modeEnv.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Unknown WEATHER_MODE '{}', fallback to ON_DEMAND", modeEnv);
            mode = WorkMode.ON_DEMAND;
        }
        List<String> cities = Arrays.stream(cityEnv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .collect(Collectors.toList());

        log.info("Configured cities: {}", String.join(", ", cities));

        return new WeatherSdkConfig(cacheSize, ttlMinutes, mode, cities);
    }
}
