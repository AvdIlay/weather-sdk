package com.weather.core;

import com.weather.api.WeatherData;

public class CacheEntry {
    private final WeatherData data;
    private final long timestamp;

    public CacheEntry(WeatherData data) {
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    public WeatherData getData() {
        return data;
    }

    public boolean isExpired(long ttlMillis) {
        return System.currentTimeMillis() - timestamp > ttlMillis;
    }
}
