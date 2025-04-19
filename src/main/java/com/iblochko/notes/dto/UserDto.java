package com.iblochko.notes.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;
import lombok.Data;

@Data
@Schema(description = "Data Transfer Object (DTO) for a user")
public class UserDto {

    @Schema(
            description = "Unique username of the user",
            example = "korol_pelmeney"
    )
    private String username;

    @Schema(
            description = "Email address associated with the user",
            example = "korol_pelmeney56@fakemail.ru"
    )
    private String email;

    @Schema(
            description = "Password for the user account",
            example = "yatutkarol"
    )
    private String password;

    @Schema(
            description = "Set of note IDs created by the user",
            example = "[1, 2, 6]"
    )
    private Set<Long> noteIds;

    @Schema(
            description = "Set of tag IDs associated with the user",
            example = "[1, 2, 6]"
    )
    private Set<Long> tagIds;
}
