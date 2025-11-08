package com.weather.core;

import com.weather.api.WeatherSdkException;
import com.weather.infra.WeatherApi;
import com.weather.api.WeatherData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import java.util.concurrent.*;

public class PollingScheduler implements AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(PollingScheduler.class);

    private final ScheduledExecutorService executor;
    private final WeatherCache cache;
    private final WeatherApi api;

    public PollingScheduler(WeatherCache cache, WeatherApi api) {
        this.cache = cache;
        this.api = api;
        this.executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "weather-polling");
            t.setDaemon(true);
            return t;
        });
    }

    public void start(long intervalMinutes) {
        log.info("Scheduler started (interval={} min)", intervalMinutes);
        executor.scheduleAtFixedRate(this::refreshAll, 0, intervalMinutes, TimeUnit.MINUTES);
    }

    private void refreshAll() {
        log.debug("Refresh triggered");
        try {
            Map<String, WeatherData> snapshot = cache.snapshot();
            if (snapshot.isEmpty()) {
                log.debug("No cities in cache, skipping refresh");
                return;
            }

            for (String city : snapshot.keySet()) {
                try {
                    log.debug("Refreshing '{}'", city);
                    WeatherData updated = api.getWeather(city);
                    cache.put(city, updated);
                } catch (WeatherSdkException e) {
                    log.warn("Failed to refresh '{}': {}", city, e.getMessage());
                }
            }

            log.info("Completed refresh for {} cities", snapshot.size());
        } catch (Exception e) {
            log.error("Error while refreshing cache", e);
        }
    }

    @Override
    public void close() {
        log.info("[POLLING] Scheduler shutting down...");

        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                log.warn("[POLLING] Scheduler didn't terminate in time — forcing shutdown");
                executor.shutdownNow();
            } else {
                log.info("[POLLING] Scheduler stopped gracefully");
            }
        } catch (InterruptedException e) {
            log.error("[POLLING] Interrupted during shutdown — forcing shutdown");
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
