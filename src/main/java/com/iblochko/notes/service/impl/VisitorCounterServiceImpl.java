package com.iblochko.notes.service.impl;

import com.iblochko.notes.service.VisitorCounterService;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;

@Service
public class VisitorCounterServiceImpl implements VisitorCounterService {
    private final Map<String, AtomicLong> urlCounters = new ConcurrentHashMap<>();

    private final AtomicLong totalCounter = new AtomicLong(0);

    public long registerVisit(String url) {
        totalCounter.incrementAndGet();

        AtomicLong urlCounter = urlCounters.computeIfAbsent(url, k -> new AtomicLong(0));

        return urlCounter.getAndIncrement();
    }

    public long getVisitCount(String url) {
        AtomicLong counter = urlCounters.get(url);
        return counter != null ? counter.get() : 0;
    }

    public long getTotalVisitCount() {
        return totalCounter.get();
    }

    public Map<String, Long> getAllStats() {
        Map<String, Long> stats = new ConcurrentHashMap<>();
        urlCounters.forEach((url, counter) -> stats.put(url, counter.get()));
        return stats;
    }
}
