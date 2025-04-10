package com.iblochko.notes.controller;

import com.iblochko.notes.dto.NoteDto;
import com.iblochko.notes.exception.ResourceNotFoundException;
import com.iblochko.notes.model.Note;
import com.iblochko.notes.service.NoteService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/notes")
@AllArgsConstructor
public class NotesController {

    private final NoteService noteService;

    @GetMapping
    public ResponseEntity<List<Note>>
        findNoteByTitle(@RequestParam(required = false) String title) {
        List<Note> notes = noteService.findNoteByTitle(title);
        return new ResponseEntity<>(notes, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Note> findNoteById(@PathVariable Long id) {
        Note note = noteService.findNoteById(id);
        return new ResponseEntity<>(note, HttpStatus.OK);
    }

    @GetMapping("/tagName")
    public ResponseEntity<List<Note>>
        findNoteByTagName(@RequestParam(required = false) String tagName) {
        List<Note> notes = noteService.findNoteByTagName(tagName);
        return new ResponseEntity<>(notes, HttpStatus.OK);
    }

    @GetMapping("/username")
    public ResponseEntity<List<Note>>
        findNoteByUsername(@RequestParam(required = false) String username) {
        List<Note> notes = noteService.findNoteByUsername(username);
        return new ResponseEntity<>(notes, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<NoteDto> createNote(@RequestBody NoteDto noteDto) {
        NoteDto createdNote = noteService.createNote(noteDto);

        return new ResponseEntity<>(createdNote, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<NoteDto> updateNote(@PathVariable Long id, @RequestBody NoteDto noteDto) {
        NoteDto updatedNote = noteService.updateNote(id, noteDto);
        return new ResponseEntity<>(updatedNote, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNote(@PathVariable long id) {
        noteService.deleteNote(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
