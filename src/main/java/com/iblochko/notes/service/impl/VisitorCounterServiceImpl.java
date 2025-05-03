package com.iblochko.notes.service.impl;

import com.iblochko.notes.service.VisitorCounterService;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;


@Service
public class VisitorCounterServiceImpl implements VisitorCounterService {
    private final Map<String, Long> urlCounters = new ConcurrentHashMap<>();

    private long totalCounter = 0;

    public synchronized long registerVisit(String url) {
        totalCounter++;

        Long count = urlCounters.getOrDefault(url, 0L);
        urlCounters.put(url, count + 1L);

        return count;
    }

    public synchronized long getVisitCount(String url) {
        Long counter = urlCounters.get(url);
        return counter != null ? counter : 0;
    }

    public synchronized long getTotalVisitCount() {
        return totalCounter;
    }

    public synchronized Map<String, Long> getAllStats() {
        return new ConcurrentHashMap<>(urlCounters);
    }
}
