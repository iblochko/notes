package com.iblochko.notes.dto;

import java.util.Set;
import lombok.Data;

@Data
public class TagDto {
    private Long id;
    private String name;
    private String username;
    private Set<Long> noteIds;
}
