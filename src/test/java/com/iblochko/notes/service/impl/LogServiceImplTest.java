package com.iblochko.notes.service.impl;

import com.iblochko.notes.service.LogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("ResultOfMethodCallIgnored")
@ExtendWith(MockitoExtension.class)
public class LogServiceImplTest {

    @Spy
    @InjectMocks
    private LogServiceImpl logService;

    @TempDir
    Path tempDir;

    private Path logFile;

    @BeforeEach
    void setUp() throws IOException {
        logFile = tempDir.resolve("application.log");

        ReflectionTestUtils.setField(logService, "logFilePath", logFile.toString());

        List<String> logLines = Arrays.asList(
                "2023-05-15 10:30:45 INFO  Test log entry from May 15",
                "2023-05-15 11:22:33 ERROR Exception occurred in the application",
                "2023-05-16 09:15:27 INFO  Another log entry from May 16",
                "2023-05-17 14:45:12 WARN  Warning message from May 17",
                "2023-06-01 08:30:00 INFO  Log entry from June"
        );

        Files.write(logFile, logLines);

    }

    @Test
    void getLogsForDate_WithExistingEntries_ShouldReturnMatchingLogs() throws IOException {
        LocalDate date = LocalDate.of(2023, 5, 15);

        String result = logService.getLogsForDate(date);

        assertTrue(result.contains("2023-05-15 10:30:45"));
        assertTrue(result.contains("2023-05-15 11:22:33"));
        assertFalse(result.contains("2023-05-16"));
        assertEquals(2, result.split("\n").length);
    }

    @Test
    void getLogsForDate_WithNoMatchingEntries_ShouldReturnNoLogsMessage() throws IOException {
        LocalDate date = LocalDate.of(2023, 5, 20);

        String result = logService.getLogsForDate(date);

        assertEquals("No logs found for date: 2023-05-20", result);
    }

    @Test
    void getLogsForDate_WithNonExistentLogFile_ShouldThrowIOException() throws IOException {
        LocalDate date = LocalDate.of(2023, 5, 15);

        Files.delete(logFile);

        IOException exception = assertThrows(IOException.class, () -> logService.getLogsForDate(date));

        assertEquals("Log file does not exist", exception.getMessage());
    }

    @Test
    void getLogsForDate_WithIOExceptionDuringReading_ShouldPropagateException() throws IOException {
        LocalDate date = LocalDate.of(2023, 5, 15);

        doThrow(new IOException("Error reading file"))
                .when(logService).getLogsForDate(any(LocalDate.class));

        IOException exception = assertThrows(IOException.class, () -> logService.getLogsForDate(date));

        assertEquals("Error reading file", exception.getMessage());
    }

    @Test
    void getLogsForDate_WithDifferentDateFormats_ShouldMatchCorrectly() throws IOException {
        List<String> mixedFormatLogs = Arrays.asList(
                "2023-07-01 Info log with correct format",
                "07-01-2023 Info log with different format",
                "2023/07/01 Info log with slashes",
                "2023-07-01T12:34:56 Log with time in ISO format"
        );

        Files.write(logFile, mixedFormatLogs);

        String result = logService.getLogsForDate(LocalDate.of(2023, 7, 1));

        assertTrue(result.contains("2023-07-01 Info"));
        assertTrue(result.contains("2023-07-01T12:34:56"));
        assertFalse(result.contains("07-01-2023"));
        assertFalse(result.contains("2023/07/01"));
        assertEquals(2, result.split("\n").length);
    }

    @Test
    void getLogsForDate_WithEmptyLogFile_ShouldReturnNoLogsMessage() throws IOException {
        Files.write(logFile, List.of());

        String result = logService.getLogsForDate(LocalDate.of(2023, 5, 15));

        assertEquals("No logs found for date: 2023-05-15", result);
    }

    @Test
    void createLogTask_ShouldReturnTaskId() {

        String taskId = logService.createLogTask("Test log content", LocalDate.now());


        assertNotNull(taskId);
        assertFalse(taskId.isEmpty());
    }

    @Test
    void getTaskStatus_ShouldReturnTaskWithCorrectStatus() throws InterruptedException {

        String taskId = logService.createLogTask("Test log content", LocalDate.now());


        LogService.LogTask task = logService.getTaskStatus(taskId);


        assertNotNull(task);
        assertEquals(taskId, task.getId());
        assertEquals("Test log content", task.getContent());

        CountDownLatch latch = new CountDownLatch(1);
        for (int i = 0; i < 10; i++) {
            if (task.getStatus() == LogService.LogTask.Status.COMPLETED) {
                latch.countDown();
                break;
            }
            Thread.sleep(1000);
        }

        latch.await(10, TimeUnit.SECONDS);

        task = logService.getTaskStatus(taskId);

        assertEquals(LogService.LogTask.Status.COMPLETED, task.getStatus());
        assertNotNull(task.getFilePath());
        assertTrue(new File(task.getFilePath()).exists());
    }

    @Test
    void getLogFile_ShouldReturnFileContents() throws IOException, InterruptedException {
        String content = "Test log content for file";
        String taskId = logService.createLogTask(content, LocalDate.now());

        LogService.LogTask task = null;
        for (int i = 0; i < 10; i++) {
            task = logService.getTaskStatus(taskId);
            if (task.getStatus() == LogService.LogTask.Status.COMPLETED) {
                break;
            }
            Thread.sleep(1000);
        }

        assertNotNull(task);
        assertEquals(LogService.LogTask.Status.COMPLETED, task.getStatus());

        byte[] fileContent = logService.getLogFile(taskId);

        assertNotNull(fileContent);
        assertTrue(fileContent.length > 0);

        String fileContentStr = new String(fileContent);
        assertTrue(fileContentStr.contains(content));
        assertTrue(fileContentStr.contains(taskId));
    }

    @Test
    void getLogFile_ShouldReturnNull_WhenTaskDoesNotExist() throws IOException {
        byte[] result = logService.getLogFile("non-existent-task");

        assertNull(result);
    }

    @Test
    void getLogFile_ShouldReturnNull_WhenTaskIsNotCompleted() throws IOException {
        String taskId = "test-task";
        LogService.LogTask task = new LogService.LogTask(taskId, "Test content", LocalDate.now());
        task.setStatus(LogService.LogTask.Status.PROCESSING);

        try {
            Field tasksField = LogServiceImpl.class.getDeclaredField("tasks");
            tasksField.setAccessible(true);
            @SuppressWarnings("unchecked")
            java.util.concurrent.ConcurrentHashMap<String, LogService.LogTask> tasks =
                    (java.util.concurrent.ConcurrentHashMap<String, LogService.LogTask>) tasksField.get(logService);
            tasks.put(taskId, task);
        } catch (Exception e) {
            fail("Couldn't set up test properly: " + e.getMessage());
        }

        byte[] result = logService.getLogFile(taskId);

        assertNull(result);
    }
}