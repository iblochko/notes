package com.iblochko.notes.dto;

import java.time.LocalDateTime;
import java.util.Set;
import lombok.Data;


@Data
public class NoteDto {
    private Long id;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String username;
    private Set<Long> tagIds;
}
