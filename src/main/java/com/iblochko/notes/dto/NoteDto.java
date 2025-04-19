package com.iblochko.notes.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.Data;


@Data
@Schema(description = "Data Transfer Object (DTO) for a note")
public class NoteDto {

    @Schema(
            description = "Unique identifier for the note",
            example = "1"
    )
    private Long id;

    @Schema(
            description = "Title of the note",
            example = "Shopping List"
    )
    private String title;

    @Schema(
            description = "Content of the note",
            example = "Milk, bread, honey"
    )
    private String content;

    @Schema(
            description = "Creation date and time of the note in ISO 8601 format",
            example = "2025-04-17T10:30:00"
    )
    private LocalDateTime createdAt;

    @Schema(
            description = "Last updated date and time of the note in ISO 8601 format",
            example = "2025-04-17T10:30:00"
    )
    private LocalDateTime updatedAt;

    @Schema(
            description = "Username of the user who owns the note",
            example = "korol_pelmeney"
    )
    private String username;

    @Schema(
            description = "Set of tag IDs associated with the note",
            example = "[1, 2, 6]"
    )
    private Set<Long> tagIds;
}
