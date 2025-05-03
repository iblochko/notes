package com.iblochko.notes.service.impl;

import com.iblochko.notes.service.LogService;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@SuppressWarnings("ResultOfMethodCallIgnored")
@Service
public class LogServiceImpl implements LogService {

    private final ExecutorService executorService = Executors.newFixedThreadPool(5);
    private final ConcurrentHashMap<String, LogTask> tasks = new ConcurrentHashMap<>();
    private final String logDirectory = "logs";

    @Value("${logging.file.name}")
    private String logFilePath;

    public LogServiceImpl() {
        File logDir = new File(logDirectory);
        if (!logDir.exists()) {
            logDir.mkdirs();
        }
    }

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
        return String.join("\n", matchingLines);
    }

    @Override
    public String createLogTask(String content, LocalDate date) {
        String taskId = UUID.randomUUID().toString();
        LogTask task = new LogTask(taskId, content, date);
        tasks.put(taskId, task);

        executorService.submit(() -> processLogTask(task));

        return taskId;
    }

    @Override
    public LogTask getTaskStatus(String taskId) {
        return tasks.get(taskId);
    }

    @Override
    public byte[] getLogFile(String taskId) throws IOException {
        LogTask task = tasks.get(taskId);
        if (task == null || task.getStatus() != LogTask.Status.COMPLETED) {
            return null;
        }

        return Files.readAllBytes(Paths.get(task.getFilePath()));
    }

    private void processLogTask(LogTask task) {
        try {
            task.setStatus(LogTask.Status.PROCESSING);

            try {
                Thread.sleep(30000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                task.setStatus(LogTask.Status.FAILED);
                task.setErrorMessage("Task processing was interrupted");
                return;
            }

            String fileName = logDirectory + File.separator + "log_" + task.getId() + ".txt";
            try (FileWriter writer = new FileWriter(fileName)) {
                writer.write(this.getLogsForDate(task.getDate()));
            }
            task.setFilePath(fileName);
            task.setStatus(LogTask.Status.COMPLETED);
        } catch (Exception e) {
            task.setStatus(LogTask.Status.FAILED);
            task.setErrorMessage(e.getMessage());
        }
    }

}
