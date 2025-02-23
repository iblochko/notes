package com.iblochko.notes.service.impl;

import com.iblochko.notes.model.Note;
import com.iblochko.notes.repository.InMemoryNote;
import com.iblochko.notes.service.NoteService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class InMemoryNoteService implements NoteService {
    private final InMemoryNote repository;

    @Override
    public Note saveNote(Note note) {
        return repository.saveNote(note);
    }

    @Override
    public List<Note> findNoteByTitle(String title) {
        return repository.findNoteByTitle(title);
    }

    @Override
    public Note findNoteById(long id) {
        return repository.findNoteById(id);
    }

    @Override
    public Note updateNote(Note note) {
        return repository.updateNote(note);
    }

    @Override
    public void deleteNote(long id) {
        repository.deleteNote(id);
    }
}
