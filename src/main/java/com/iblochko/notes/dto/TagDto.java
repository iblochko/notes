package com.iblochko.notes.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;
import lombok.Data;

@Data
@Schema(description = "Data Transfer Object (DTO) for a tag")
public class TagDto {

    @Schema(
            description = "Unique identifier for the tag",
            example = "1"
    )
    private Long id;

    @Schema(
            description = "Name of the tag",
            example = "Work"
    )
    private String name;

    @Schema(
            description = "Username of the user who created the tag",
            example = "korol_pelmeney"
    )
    private String username;

    @Schema(
            description = "Set of note IDs associated with the tag",
            example = "[1, 2, 6]"
    )
    private Set<Long> noteIds;
}
