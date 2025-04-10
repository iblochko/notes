package com.iblochko.notes.service.impl;

import com.iblochko.notes.service.LogService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LogServiceImpl implements LogService {

    @Value("${logging.file.name}")
    private String logFilePath;

    @Override
    public String getLogsForDate(LocalDate date) throws IOException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String dateString = date.format(formatter);

        Path path = Paths.get(logFilePath);
        if (!Files.exists(path)) {
            throw new IOException("Log file does not exist");
        }

        List<String> matchingLines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(logFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(dateString)) {
                    matchingLines.add(line);
                }
            }
        }
        if (matchingLines.isEmpty()) {
            return "No logs found for date: " + dateString;
        }
        return matchingLines.stream().collect(Collectors.joining("\n"));
    }
}
