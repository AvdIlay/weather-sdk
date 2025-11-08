package com.weather.api;

import com.weather.config.WeatherApiConfig;
import com.weather.config.WeatherSdkConfig;
import com.weather.core.DefaultWeatherClient;
import com.weather.core.WeatherClientRegistry;
import com.weather.infra.OpenWeatherApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WeatherClientBuilder {
    private static final Logger log = LoggerFactory.getLogger(WeatherClientBuilder.class);

    private String apiKey;
    private WorkMode workMode;
    private Integer cacheSize;
    private Integer ttlMinutes;

    public WeatherClientBuilder apiKey(String key) {
        this.apiKey = key;
        return this;
    }

    public WeatherClientBuilder workMode(WorkMode mode) {
        this.workMode = mode;
        return this;
    }

    public WeatherClientBuilder cacheSize(int size) {
        this.cacheSize = size;
        return this;
    }

    public WeatherClientBuilder ttlMinutes(int ttl) {
        this.ttlMinutes = ttl;
        return this;
    }

    public WeatherClient build() throws WeatherSdkException {
        log.info("Initializing WeatherClient");

        WeatherApiConfig apiConfig = WeatherApiConfig.load();
        log.debug("Loaded WeatherApiConfig: baseUrl={}, lang={}, units={}",
                apiConfig.baseUrl(), apiConfig.lang(), apiConfig.units());

        if (apiKey != null && !apiKey.isBlank()) {
            log.info("Overriding API key via builder");
            apiConfig = new WeatherApiConfig(
                    apiKey,
                    apiConfig.baseUrl(),
                    apiConfig.units(),
                    apiConfig.lang()
            );
        }

        if (apiConfig.apiKey() == null || apiConfig.apiKey().isBlank() || apiConfig.apiKey().equals("replace_me")) {
            log.error("Missing or invalid API key. Please configure WEATHER_API_KEY env var or weather.api.key in properties.");
            throw new WeatherSdkException(
                    "Missing or invalid API key. Please set WEATHER_API_KEY environment variable or specify weather.api.key in weather.properties."
            );
        }

        WeatherSdkConfig sdkConfig = WeatherSdkConfig.defaults();
        log.debug("Loaded default SDK config: ttl={}min, cacheSize={}, mode={}",
                sdkConfig.ttlMinutes(), sdkConfig.cacheSize(), sdkConfig.workMode());

        if (cacheSize != null || ttlMinutes != null || workMode != null) {
            sdkConfig = new WeatherSdkConfig(
                    cacheSize != null ? cacheSize : sdkConfig.cacheSize(),
                    ttlMinutes != null ? ttlMinutes : sdkConfig.ttlMinutes(),
                    workMode != null ? workMode : sdkConfig.workMode(),
                    sdkConfig.cities()
            );
            log.info("Custom SDK config applied: ttl={}min, cacheSize={}, mode={}, city={}",
                    sdkConfig.ttlMinutes(), sdkConfig.cacheSize(), sdkConfig.workMode(), sdkConfig.cities());
        }

        OpenWeatherApi api = new OpenWeatherApi(apiConfig);
        DefaultWeatherClient client = new DefaultWeatherClient(apiConfig, api, sdkConfig);

        WeatherClientRegistry.register(apiConfig.apiKey(), client);
        log.info("WeatherClient successfully registered (apiKey={})", apiConfig.apiKey());

        return client;
    }

}
