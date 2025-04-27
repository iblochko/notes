package com.iblochko.notes.controller;

import com.iblochko.notes.service.VisitorCounterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stats")
@Tag(name = "Visitor Counter", description = "API for tracking and retrieving URL visit statistics")
public class VisitorCounterController {
    private final VisitorCounterService visitorCounterService;

    @Autowired
    public VisitorCounterController(VisitorCounterService visitorCounterService) {
        this.visitorCounterService = visitorCounterService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a visit for a URL",
            description =
                    "Registers a visit for the specified URL and returns the updated visit count")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Visit successfully registered",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = Long.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Long> registerVisit(
            @Parameter(description = "URL to register the visit for", required = true)
            @RequestParam String url) {
        long count = visitorCounterService.registerVisit(url);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/url")
    @Operation(summary = "Get visit count for a specific URL",
            description = "Returns the number of visits for the specified URL")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved visit count",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = Long.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Long> getUrlVisits(
            @Parameter(description = "URL to get visit count for", required = true)
            @RequestParam String url) {
        long count = visitorCounterService.getVisitCount(url);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/total")
    @Operation(summary = "Get total visit count across all URLs",
            description = "Returns the total number of visits for all registered URLs")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved total visit count",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = Long.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Long> getTotalVisits() {
        long count = visitorCounterService.getTotalVisitCount();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/all")
    @Operation(summary = "Get visit statistics for all URLs",
            description = "Returns a map of all registered URLs and their respective visit counts")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved visit statistics",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(description = "Map of URL to visit count"))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Long>> getAllStats() {
        Map<String, Long> stats = visitorCounterService.getAllStats();
        return ResponseEntity.ok(stats);
    }
}