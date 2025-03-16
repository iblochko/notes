package com.iblochko.notes.controller;

import com.iblochko.notes.model.Note;
import com.iblochko.notes.service.NoteService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@SuppressWarnings("checkstyle:MissingJavadocType")
@RestController
@RequestMapping("/api/v1/notes")
@AllArgsConstructor
public class NotesController {

    private final NoteService noteService;

    @GetMapping
    public List<Note> findNoteByTitle(@RequestParam(required = false) String title) {
        return noteService.findNoteByTitle(title);
    }

    @GetMapping("/{id}")
    public Note findNoteById(@PathVariable long id) {
        return noteService.findNoteById(id);
    }

    @PostMapping("new_note")
    public Note createNote(@RequestBody Note note) {
        return noteService.saveNote(note);
    }

    @PutMapping("/update_note")
    public Note updateNote(@RequestBody Note note) {
        return noteService.updateNote(note);
    }

    @DeleteMapping("delete_note/{id}")
    public void deleteNote(@PathVariable long id) {
        noteService.deleteNote(id);
    }
}
