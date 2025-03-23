package com.iblochko.notes.mapper;

import com.iblochko.notes.dto.NoteDto;
import com.iblochko.notes.model.Note;
import com.iblochko.notes.model.Tag;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class NoteMapper {
    public NoteDto toDto(Note note) {
        if (note == null) {
            return null;
        }

        NoteDto noteDto = new NoteDto();
        noteDto.setId(note.getId());
        noteDto.setTitle(note.getTitle());
        noteDto.setContent(note.getContent());
        noteDto.setCreatedAt(note.getCreatedAt());
        noteDto.setUpdatedAt(note.getUpdatedAt());
        if (note.getUser() != null) {
            noteDto.setUsername(note.getUser().getUsername());
        }

        if (note.getTags() != null && !note.getTags().isEmpty()) {
            Set<Long> tagIds = note.getTags().stream()
                    .map(Tag::getId)
                    .collect(Collectors.toSet());
            noteDto.setTagIds(tagIds);
        }

        return noteDto;
    }

    public Note toEntity(NoteDto noteDto) {
        if (noteDto == null) {
            return null;
        }

        Note note = new Note();
        note.setId(noteDto.getId());
        note.setTitle(noteDto.getTitle());
        note.setContent(noteDto.getContent());
        note.setCreatedAt(noteDto.getCreatedAt());
        note.setUpdatedAt(noteDto.getUpdatedAt());

        return note;
    }

    public void updateEntity(NoteDto noteDto, Note note) {
        if (note == null || noteDto == null) {
            return;
        }

        if (noteDto.getId() != null) {
            note.setId(noteDto.getId());
        }
        if (noteDto.getTitle() != null) {
            note.setTitle(noteDto.getTitle());
        }
        if (noteDto.getContent() != null) {
            note.setContent(noteDto.getContent());
        }
    }
}
