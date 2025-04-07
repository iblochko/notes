package com.iblochko.notes.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CacheUtil {
    private final Map<String, Object> cache = new ConcurrentHashMap<>();
    private final int maxCacheSize;

    CacheUtil() {
        maxCacheSize = 100;
    }

    public <T> T get(String key, Class<T> type) {
        Object value = cache.get(key);
        if (value != null) {
            log.info("Cache hit for key: {}", key);
            return type.cast(value);
        }
        log.info("Cache miss for key: {}", key);
        return null;
    }

    public void put(String key, Object value) {
        if (cache.size() >= maxCacheSize) {
            log.info("Cache is full, clearing...");
            this.clear();
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