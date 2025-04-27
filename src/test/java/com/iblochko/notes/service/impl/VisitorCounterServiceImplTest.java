package com.iblochko.notes.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class VisitorCounterServiceTest {

    private VisitorCounterServiceImpl visitorCounterService;

    @BeforeEach
    void setUp() {
        visitorCounterService = new VisitorCounterServiceImpl();
    }

    @Test
    void registerVisit_ShouldIncrementCounter() {
        String url = "/test-url";

        long firstVisit = visitorCounterService.registerVisit(url);
        long secondVisit = visitorCounterService.registerVisit(url);

        assertEquals(1, firstVisit);
        assertEquals(2, secondVisit);
    }

    @Test
    void getVisitCount_ShouldReturnCorrectCount() {
        String url = "/test-url";
        visitorCounterService.registerVisit(url);
        visitorCounterService.registerVisit(url);
        visitorCounterService.registerVisit(url);

        long count = visitorCounterService.getVisitCount(url);

        assertEquals(3, count);
    }

    @Test
    void getVisitCount_ShouldReturnZero_WhenUrlNotVisited() {
        String url = "/non-existent-url";

        long count = visitorCounterService.getVisitCount(url);

        assertEquals(0, count);
    }

    @Test
    void getTotalVisitCount_ShouldReturnTotalVisits() {
        visitorCounterService.registerVisit("/url1");
        visitorCounterService.registerVisit("/url2");
        visitorCounterService.registerVisit("/url1");

        long totalCount = visitorCounterService.getTotalVisitCount();

        assertEquals(3, totalCount);
    }

    @Test
    void getAllStats_ShouldReturnAllUrlCounts() {
        visitorCounterService.registerVisit("/url1");
        visitorCounterService.registerVisit("/url2");
        visitorCounterService.registerVisit("/url1");

        Map<String, Long> stats = visitorCounterService.getAllStats();

        assertEquals(2, stats.size());
        assertEquals(2L, stats.get("/url1"));
        assertEquals(1L, stats.get("/url2"));
    }

    @Test
    void concurrentRegistration_ShouldMaintainCorrectCounts() throws InterruptedException {
        final String url = "/concurrent-test";
        final int threadCount = 100;
        final int visitsPerThread = 100;
        final ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch endLatch = new CountDownLatch(threadCount);
        final AtomicInteger totalExpectedVisits = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    startLatch.await();

                    for (int j = 0; j < visitsPerThread; j++) {
                        visitorCounterService.registerVisit(url);
                        totalExpectedVisits.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();

        endLatch.await();
        executorService.shutdown();

        long actualVisits = visitorCounterService.getVisitCount(url);
        long totalVisits = visitorCounterService.getTotalVisitCount();

        assertEquals(threadCount * visitsPerThread, actualVisits);
        assertEquals(totalExpectedVisits.get(), totalVisits);
    }

    @Test
    void multipleUrlsConcurrently_ShouldTrackCorrectly() throws InterruptedException {
        final int threadCount = 50;
        final int visitsPerThread = 20;
        final ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch endLatch = new CountDownLatch(threadCount);
        final String[] urls = {"/url1", "/url2", "/url3", "/url4", "/url5"};
        final AtomicInteger totalExpectedVisits = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final int threadIndex = i;
            executorService.submit(() -> {
                try {
                    startLatch.await();

                    String url = urls[threadIndex % urls.length];

                    for (int j = 0; j < visitsPerThread; j++) {
                        visitorCounterService.registerVisit(url);
                        totalExpectedVisits.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();

        endLatch.await();
        executorService.shutdown();

        long totalVisits = visitorCounterService.getTotalVisitCount();
        assertEquals(totalExpectedVisits.get(), totalVisits);

        Map<String, Long> stats = visitorCounterService.getAllStats();

        for (String url : urls) {
            assertTrue(stats.containsKey(url));
            assertTrue(stats.get(url) > 0);

        }

        long sumOfUrlVisits = stats.values().stream().mapToLong(Long::longValue).sum();
        assertEquals(totalVisits, sumOfUrlVisits);
    }
}

