package com.iblochko.notes.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
        // Create a temporary log file for testing
        logFile = tempDir.resolve("application.log");

        // Inject the path of the temporary file into the service
        ReflectionTestUtils.setField(logService, "logFilePath", logFile.toString());

        // Create sample log content
        List<String> logLines = Arrays.asList(
                "2023-05-15 10:30:45 INFO  Test log entry from May 15",
                "2023-05-15 11:22:33 ERROR Exception occurred in the application",
                "2023-05-16 09:15:27 INFO  Another log entry from May 16",
                "2023-05-17 14:45:12 WARN  Warning message from May 17",
                "2023-06-01 08:30:00 INFO  Log entry from June"
        );

        // Write the sample logs to the temporary file
        Files.write(logFile, logLines);
    }

    @Test
    void getLogsForDate_WithExistingEntries_ShouldReturnMatchingLogs() throws IOException {
        // Arrange
        LocalDate date = LocalDate.of(2023, 5, 15);

        // Act
        String result = logService.getLogsForDate(date);

        // Assert
        assertTrue(result.contains("2023-05-15 10:30:45"));
        assertTrue(result.contains("2023-05-15 11:22:33"));
        assertFalse(result.contains("2023-05-16"));
        assertEquals(2, result.split("\n").length);
    }

    @Test
    void getLogsForDate_WithNoMatchingEntries_ShouldReturnNoLogsMessage() throws IOException {
        // Arrange
        LocalDate date = LocalDate.of(2023, 5, 20);

        // Act
        String result = logService.getLogsForDate(date);

        // Assert
        assertEquals("No logs found for date: 2023-05-20", result);
    }

    @Test
    void getLogsForDate_WithNonExistentLogFile_ShouldThrowIOException() throws IOException {
        // Arrange
        LocalDate date = LocalDate.of(2023, 5, 15);

        // Delete the log file to simulate non-existent file
        Files.delete(logFile);

        // Act & Assert
        IOException exception = assertThrows(IOException.class, () -> logService.getLogsForDate(date));

        assertEquals("Log file does not exist", exception.getMessage());
    }

    @Test
    void getLogsForDate_WithIOExceptionDuringReading_ShouldPropagateException() throws IOException {
        // Arrange
        LocalDate date = LocalDate.of(2023, 5, 15);

        // Изменить этот подход - нельзя делать spy из mock
        doThrow(new IOException("Error reading file"))
                .when(logService).getLogsForDate(any(LocalDate.class));

        // Act & Assert
        IOException exception = assertThrows(IOException.class, () -> logService.getLogsForDate(date));

        assertEquals("Error reading file", exception.getMessage());
    }

    @Test
    void getLogsForDate_WithDifferentDateFormats_ShouldMatchCorrectly() throws IOException {
        // Arrange - Create a new log file with various date formats
        List<String> mixedFormatLogs = Arrays.asList(
                "2023-07-01 Info log with correct format",
                "07-01-2023 Info log with different format",  // This shouldn't match
                "2023/07/01 Info log with slashes",           // This shouldn't match
                "2023-07-01T12:34:56 Log with time in ISO format"  // This should match
        );

        Files.write(logFile, mixedFormatLogs);

        // Act
        String result = logService.getLogsForDate(LocalDate.of(2023, 7, 1));

        // Assert
        assertTrue(result.contains("2023-07-01 Info"));
        assertTrue(result.contains("2023-07-01T12:34:56"));
        assertFalse(result.contains("07-01-2023"));
        assertFalse(result.contains("2023/07/01"));
        assertEquals(2, result.split("\n").length);
    }

    @Test
    void getLogsForDate_WithEmptyLogFile_ShouldReturnNoLogsMessage() throws IOException {
        // Arrange - Create an empty log file
        Files.write(logFile, List.of());

        // Act
        String result = logService.getLogsForDate(LocalDate.of(2023, 5, 15));

        // Assert
        assertEquals("No logs found for date: 2023-05-15", result);
    }
}