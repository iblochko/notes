package com.iblochko.notes.repository;

import com.iblochko.notes.model.Note;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.springframework.stereotype.Repository;


@Repository
public class InMemoryNote {
    private final List<Note> notes = new ArrayList<>();

    public Note saveNote(Note note) {
        notes.add(note);
        return note;
    }

    public List<Note> findNoteByTitle(String title) {
        if (title == null || title.isEmpty()) {
            return notes;
        }
        return notes.stream()
                .filter(note -> note.getTitle().toLowerCase().contains(title.toLowerCase()))
                .toList();
    }

    public Note findNoteById(long id) {
        return notes.stream()
                .filter(element -> element.getId() == id)
                .findFirst()
                .orElse(null);
    }

    public Note updateNote(Note note) {
        var noteIndex = IntStream.range(0, notes.size())
                .filter(index -> notes.get(index).getTitle().equals(note.getTitle()))
                .findFirst()
                .orElse(-1);
        if (noteIndex > -1) {
            notes.set(noteIndex, note);
            return note;
        }
        return null;
    }

    public void deleteNote(long id) {
        var note = findNoteById(id);
        if (note != null) {
            notes.remove(note);
        }
    }
}
