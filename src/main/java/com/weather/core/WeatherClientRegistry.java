package com.weather.core;

import com.weather.api.WeatherClient;
import com.weather.api.WeatherSdkException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class WeatherClientRegistry {

    private static final Logger log = LoggerFactory.getLogger(WeatherClientRegistry.class);
    private static final Map<String, WeatherClient> CLIENTS = new ConcurrentHashMap<>();

    private WeatherClientRegistry() {}

    public static WeatherClient register(String apiKey, WeatherClient client) throws WeatherSdkException {
        WeatherClient existing = CLIENTS.putIfAbsent(apiKey, client);
        if (existing != null) {
            log.error("Attempt to register duplicate client with API key '{}'", apiKey);
            throw new WeatherSdkException("Client with API key already exists: " + apiKey);
        }
        log.info("Registered new WeatherClient (apiKey={})", apiKey);
        return client;
    }

    public static void remove(String apiKey) {
        if (CLIENTS.remove(apiKey) != null) {
            log.info("Removed WeatherClient (apiKey={})", apiKey);
        } else {
            log.debug("No client found for removal (apiKey={})", apiKey);
        }
    }

    public static WeatherClient get(String apiKey) {
        WeatherClient client = CLIENTS.get(apiKey);
        if (client == null) {
            log.debug("Client not found (apiKey={})", apiKey);
        } else {
            log.trace("Client fetched (apiKey={})", apiKey);
        }
        return client;
    }

    public static void clear() {
        int size = CLIENTS.size();
        CLIENTS.clear();
        log.warn("Cleared all {} registered clients", size);
    }
}
