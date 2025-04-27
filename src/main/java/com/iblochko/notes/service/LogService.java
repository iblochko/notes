package com.iblochko.notes.service;

import com.iblochko.notes.service.impl.LogServiceImpl;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

public interface LogService {
    String getLogsForDate(LocalDate date) throws IOException;

    String createLogTask(String content);

    LogServiceImpl.LogTask getTaskStatus(String taskId);

    byte[] getLogFile(String taskId) throws IOException;

    @Getter
    @Setter
    class LogTask {
        public enum Status {
            PENDING,
            PROCESSING,
            COMPLETED,
            FAILED
        }

        private final String id;
        private final String content;
        private LogServiceImpl.LogTask.Status status;
        private String filePath;
        private String errorMessage;
        private final LocalDateTime createdAt;

        public LogTask(String id, String content) {
            this.id = id;
            this.content = content;
            this.status = LogServiceImpl.LogTask.Status.PENDING;
            this.createdAt = LocalDateTime.now();
        }
    }
}
