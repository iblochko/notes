package com.iblochko.notes.controller;

import com.iblochko.notes.service.LogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDate;

@RestController
@RequestMapping("/logs")
public class LogController {
    private final LogService logService;

    @Autowired
    public LogController(LogService logService) {
        this.logService = logService;
    }

    @GetMapping
    @Operation(summary = "Get logs for a specific date", description = "Returns log entries for the specified date")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved logs"),
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
            return ResponseEntity.internalServerError().body("Error retrieving logs: " + e.getMessage());
        }
    }
}
