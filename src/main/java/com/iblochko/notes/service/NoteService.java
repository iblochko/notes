package com.iblochko.notes.service;

import com.iblochko.notes.model.Note;
import java.util.List;

public interface NoteService {
    Note saveNote(Note note);

    List<Note> findNoteByTitle(String title);

    Note findNoteById(long id);

    Note updateNote(Note note);

    void deleteNote(long id);
}
