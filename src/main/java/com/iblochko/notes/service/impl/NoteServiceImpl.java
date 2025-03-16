package com.iblochko.notes.service.impl;

import com.iblochko.notes.model.Note;
import com.iblochko.notes.repository.NoteRepository;
import com.iblochko.notes.service.NoteService;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;


@Service
@AllArgsConstructor
@Primary
public class NoteServiceImpl implements NoteService {
    private final NoteRepository repository;

    @Override
    public Note saveNote(Note note) {
        return repository.save(note);
    }

    @Override
    public List<Note> findNoteByTitle(String title) {
        return repository.findByTitleContaining(title);
    }

    @Override
    public Note findNoteById(long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public Note updateNote(Note note) {
        return repository.save(note);
    }

    @Override
    @Transactional
    public void deleteNote(long id) {
        repository.deleteById(id);
    }
}
