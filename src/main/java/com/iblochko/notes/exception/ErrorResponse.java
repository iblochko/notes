package com.iblochko.notes.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Represents an error response from the API")
public class ErrorResponse {

    @Schema(description = "Timestamp when the error occurred", example = "2025-04-17T10:30:00")
    private LocalDateTime timestamp;

    @Schema(description = "HTTP status code associated with the error", example = "404")
    private int status;

    @Schema(description = "Short description of the error", example = "Not Found")
    private String error;

    @Schema(description = "Detailed error message",
            example = "The requested resource was not found")
    private String message;

    @Schema(description = "Request path that caused the error", example = "/api/v1/notes/1")
    private String path;

    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(int status, String error, String message, String path) {
        this();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }
}
