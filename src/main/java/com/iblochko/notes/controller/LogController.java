package com.iblochko.notes.controller;

import com.iblochko.notes.service.LogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/logs")
@Tag(name = "Log Management", description = "API for retrieving and managing application logs")
public class LogController {
    private final LogService logService;

    @Autowired
    public LogController(LogService logService) {
        this.logService = logService;
    }

    @GetMapping
    @Operation(summary = "Get logs for a specific date",
            description = "Returns log entries for the specified date")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved logs",
                content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"))),
        @ApiResponse(responseCode = "400", description = "Invalid date format"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<String> getLogsForDate(
            @Parameter(description = "Date in yyyy-MM-dd format", required = true)
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            String logs = logService.getLogsForDate(date);
            return ResponseEntity.ok(logs);
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body("Error retrieving logs: " + e.getMessage());
        }
    }


    @PostMapping("/create")
    @Operation(summary = "Create log processing task",
            description =
                    "Creates a new log processing task with the "
                           + "provided content and returns a task ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "202", description = "Log task successfully created",
                content = @Content(mediaType = "text/plain",
                        schema = @Schema(type = "string", description = "Task ID"))),
        @ApiResponse(responseCode = "400", description = "Invalid request body"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<String> createLog(
            @Parameter(description = "Log content to process", required = true)
            @RequestBody String content,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        String taskId = logService.createLogTask(content, date);
        return ResponseEntity.accepted().body(taskId);
    }

    @GetMapping("/{taskId}/status")
    @Operation(summary = "Get log task status",
            description = "Returns the current status of a log processing task")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved task status",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = LogService.LogTask.class))),
        @ApiResponse(responseCode = "404", description = "Task not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<LogService.LogTask> getLogStatus(
            @Parameter(description = "Log task ID", required = true)
            @PathVariable String taskId) {
        LogService.LogTask task = logService.getTaskStatus(taskId);
        if (task == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(task);
    }

    @GetMapping("/{taskId}/download")
    @Operation(summary = "Download log file",
            description = "Downloads the log file for a completed processing task")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully downloaded log file",
                content = @Content(mediaType = "text/plain")),
        @ApiResponse(responseCode = "102", description = "Processing - task not yet completed"),
        @ApiResponse(responseCode = "404", description = "Task not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ByteArrayResource> downloadLog(
            @Parameter(description = "Log task ID", required = true)
            @PathVariable String taskId) {
        try {
            LogService.LogTask task = logService.getTaskStatus(taskId);
            if (task == null) {
                return ResponseEntity.notFound().build();
            }

            if (task.getStatus() != LogService.LogTask.Status.COMPLETED) {
                return ResponseEntity.notFound().build();
            }

            byte[] data = logService.getLogFile(taskId);
            ByteArrayResource resource = new ByteArrayResource(data);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=log_" + taskId + ".txt")
                    .contentType(MediaType.TEXT_PLAIN)
                    .contentLength(data.length)
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}