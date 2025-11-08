package com.weather;

import com.weather.api.*;
import com.weather.config.WeatherApiConfig;
import com.weather.config.WeatherSdkConfig;
import com.weather.util.JsonUtils;

public class Main {
    public static void main(String[] args) throws Exception {
        WeatherApiConfig apiConfig = WeatherApiConfig.load();
        WeatherSdkConfig sdkConfig = WeatherSdkConfig.defaults();
        WorkMode mode = sdkConfig.workMode();

        WeatherClient client = new WeatherClientBuilder()
                .apiKey(apiConfig.apiKey())
                .workMode(mode)
                .ttlMinutes(sdkConfig.ttlMinutes())
                .cacheSize(sdkConfig.cacheSize())
                .build();

        for (String city : sdkConfig.cities()) {
            System.out.printf("Requesting weather for city: %s%n", city);

            WeatherData data = client.getWeather(city);
            System.out.println(JsonUtils.toJson(data));

            WeatherData again = client.getWeather(city);
            System.out.println("Repeated call (should be cached):");
            System.out.println(JsonUtils.toJson(again));
        }

        if (mode == WorkMode.POLLING) {
            System.out.printf("Polling every %d minute(s). Waiting for two cycles to observe refresh...%n",
                    sdkConfig.ttlMinutes());
            Thread.sleep(sdkConfig.ttlMinutes() * 2 * 60L * 1000L);
        } else {
            System.out.println("ON_DEMAND mode – polling disabled, exiting immediately.");
        }

        client.close();
    }
}
