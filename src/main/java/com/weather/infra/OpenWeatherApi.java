package com.weather.infra;

import com.weather.config.WeatherApiConfig;
import com.weather.api.WeatherData;
import com.weather.api.WeatherSdkException;
import com.weather.util.HttpUtils;
import com.weather.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class OpenWeatherApi implements WeatherApi {

    private static final Logger log = LoggerFactory.getLogger(OpenWeatherApi.class);
    private final WeatherApiConfig config;

    public OpenWeatherApi(WeatherApiConfig config) {
        this.config = config;
    }

    @Override
    public WeatherData getWeather(String city) throws WeatherSdkException {
        if (config.apiKey() == null || config.apiKey().isBlank() || config.apiKey().equals("replace_me")) {
            log.error("Missing or invalid API key. Check weather.properties or WEATHER_API_KEY env var.");
            throw new WeatherSdkException("Missing or invalid API key. Check weather.properties or environment variable WEATHER_API_KEY.");
        }

        try {
            String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8);
            String url = String.format(
                    "%s?q=%s&appid=%s&units=%s&lang=%s",
                    config.baseUrl(),
                    encodedCity,
                    config.apiKey(),
                    config.units(),
                    config.lang()
            );

            log.info("Requesting weather for '{}'", city);
            log.debug("Full URL: {}", url);

            String response = HttpUtils.get(url);

            log.info("Successfully received response for '{}'", city);
            log.trace("Raw JSON response ({} chars)", response.length());

            return JsonUtils.parseWeather(response);
        } catch (WeatherSdkException e) {
            throw e;
        } catch (Exception e) {
            log.error("Network error while calling OpenWeather API for '{}': {}", city, e.getMessage());
            throw new WeatherSdkException("Network error while calling OpenWeather API", e);
        }
    }
}
