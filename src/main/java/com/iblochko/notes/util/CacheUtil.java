package com.iblochko.notes.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CacheUtil {
    private final Map<String, Object> cache = new ConcurrentHashMap<>();
    private final AtomicLong cacheHits = new AtomicLong(0);
    private final AtomicLong cacheMisses = new AtomicLong(0);

    public <T> T get(String key, Class<T> type) {
        Object value = cache.get(key);
        if (value != null) {
            cacheHits.incrementAndGet();
            log.info("Cache hit for key: {}", key);
            return type.cast(value);
        }
        cacheMisses.incrementAndGet();
        log.info("Cache miss for key: {}", key);
        return null;
    }

    public void put(String key, Object value) {
        int maxCacheSize = 100;
        if (cache.size() >= maxCacheSize) {
            log.info("Cache is full, clearing...");
            this.clear();
            cacheHits.set(0);
            cacheMisses.set(0);
        }
        cache.put(key, value);
        log.info("Added to cache: {}", key);
    }

    public void evict(String key) {
        cache.remove(key);
        log.info("Evicted from cache: {}", key);
    }

    public void clear() {
        cache.clear();
        log.info("Cache cleared");
    }

}