package com.iblochko.notes.service;

import com.iblochko.notes.dto.NoteDto;
import java.util.List;

public interface NoteService {
    NoteDto createNote(NoteDto noteDto);

    List<NoteDto> findNoteByTitle(String title);

    NoteDto findNoteById(Long id);

    NoteDto updateNote(Long id, NoteDto noteDto);

    void deleteNote(Long id);
}
