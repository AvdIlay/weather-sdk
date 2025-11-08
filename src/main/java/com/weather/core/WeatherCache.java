package com.weather.core;

import com.weather.api.WeatherData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.LinkedHashMap;
import java.util.Map;

public class WeatherCache {
    private static final Logger log = LoggerFactory.getLogger(WeatherCache.class);

    private final int maxSize;
    private final long ttlMillis;
    private final Map<String, CacheEntry> cache;

    public WeatherCache(int maxSize, int ttlMinutes) {
        this.maxSize = maxSize;
        this.ttlMillis = ttlMinutes * 60L * 1000L;

        this.cache = new LinkedHashMap<String, CacheEntry>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, CacheEntry> eldest) {
                boolean remove = size() > maxSize;
                if (remove) {
                    log.info("Evict Least Recently Used : {} (maxSize={})", eldest.getKey(), maxSize);
                }
                return remove;
            }
        };
    }

    public synchronized WeatherData get(String city) {
        CacheEntry entry = cache.get(city);
        if (entry == null) {
            log.debug("Miss: {}", city);
            return null;
        }

        if (entry.isExpired(ttlMillis)) {
            log.debug("Expired: {}", city);
            cache.remove(city);
            return null;
        }

        log.trace("Hit: {}", city);
        return entry.getData();
    }

    public synchronized void put(String city, WeatherData data) {
        cache.put(city, new CacheEntry(data));
        log.debug("Put: {}", city);
    }

    public synchronized void clear() {
        cache.clear();
        log.info("Cleared manually");
    }

    public synchronized Map<String, WeatherData> snapshot() {
        Map<String, WeatherData> out = new LinkedHashMap<>();
        for (var e : cache.entrySet()) {
            if (!e.getValue().isExpired(ttlMillis)) {
                out.put(e.getKey(), e.getValue().getData());
            }
        }
        log.debug("Snapshot size={}", out.size());
        return out;
    }
}
