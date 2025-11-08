package com.weather.core;

import com.weather.api.*;
import com.weather.config.WeatherApiConfig;
import com.weather.config.WeatherSdkConfig;
import com.weather.infra.WeatherApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class DefaultWeatherClient implements WeatherClient {
    private static final Logger log = LoggerFactory.getLogger(DefaultWeatherClient.class);

    private final Map<String, Object> locks = new ConcurrentHashMap<>();
    private final WeatherApiConfig apiConfig;
    private final WeatherApi api;
    private final WeatherCache cache;
    private final WorkMode mode;
    private final PollingScheduler scheduler;
    private final AtomicBoolean closed = new AtomicBoolean(false);

    public DefaultWeatherClient(WeatherApiConfig apiConfig, WeatherApi api, WeatherSdkConfig sdkConfig) {
        this.apiConfig = apiConfig;
        this.api = api;
        this.cache = new WeatherCache(sdkConfig.cacheSize(), sdkConfig.ttlMinutes());
        this.mode = sdkConfig.workMode();
        this.scheduler = (mode == WorkMode.POLLING)
                ? new PollingScheduler(cache, api)
                : null;

        if (scheduler != null) {
            scheduler.start(sdkConfig.ttlMinutes());
            log.info("Started in POLLING mode (TTL={} min)", sdkConfig.ttlMinutes());
        } else {
            log.info("Started in ON_DEMAND mode");
        }
    }

    @Override
    public WeatherData getWeather(String city) throws WeatherSdkException {
        if (closed.get()) {
            throw new WeatherSdkException("WeatherClient is already closed");
        }

        WeatherData cached = cache.get(city);
        if (cached != null) {
            log.debug("Cache hit for city '{}'", city);
            return cached;
        }

        Object lock = locks.computeIfAbsent(city, k -> new Object());
        synchronized (lock) {
            try {
                cached = cache.get(city);
                if (cached != null) {
                    log.debug("Cache hit after wait for '{}'", city);
                    return cached;
                }

                log.info("Fetching fresh data for city '{}'", city);
                WeatherData fresh = api.getWeather(city);
                cache.put(city, fresh);
                log.debug("Stored '{}' in cache", city);
                return fresh;
            } finally {
                locks.remove(city);
            }
        }
    }

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            if (scheduler != null) {
                log.info("Shutting down scheduler...");
                scheduler.close();
            }
            cache.clear();
            WeatherClientRegistry.remove(apiConfig.apiKey());
            log.info("Closed safely (apiKey={})", apiConfig.apiKey());
        } else {
            log.warn("Close() called more than once — ignored");
        }
    }
}
