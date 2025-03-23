package com.iblochko.notes.dto;

import java.util.Set;
import lombok.Data;

@Data
public class UserDto {
    private String username;
    private String email;
    private String password;
    private Set<Long> noteIds;
    private Set<Long> tagIds;
}
