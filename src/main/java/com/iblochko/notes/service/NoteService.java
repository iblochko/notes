package com.iblochko.notes.service;

import com.iblochko.notes.dto.NoteDto;
import com.iblochko.notes.model.Note;
import java.util.List;

public interface NoteService {
    NoteDto createNote(NoteDto noteDto);

    List<Note> findNoteByTitle(String title);

    Note findNoteById(Long id);

    List<Note> findNoteByTagName(String tagName);

    List<Note> findNoteByUsername(String username);

    NoteDto updateNote(Long id, NoteDto noteDto);

    void deleteNote(Long id);
}
