package com.iblochko.notes.controller;

import com.iblochko.notes.dto.NoteDto;
import com.iblochko.notes.model.Note;
import com.iblochko.notes.service.NoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
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
@Tag(name = "Notes", description = "API for managing notes")
public class NotesController {

    private final NoteService noteService;

    public NotesController(NoteService noteService) {
        this.noteService = noteService;
    }


    @GetMapping
    @Operation(summary = "Get notes by title containing",
            description =
                    "Returns a list of all notes that contains title")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved notes"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<Note>>
        findNoteByTitle(@Parameter(description = "Note title or part of it", required = true)
                        @RequestParam(required = false) String title) {
        List<Note> notes = noteService.findNoteByTitle(title);
        return new ResponseEntity<>(notes, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get note by id",
            description =
                    "Returns a note with entered id")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved note"),
        @ApiResponse(responseCode = "404", description = "Note not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Note> findNoteById(@Parameter(description = "Note id", required = true)
                                             @PathVariable Long id) {
        Note note = noteService.findNoteById(id);
        return new ResponseEntity<>(note, HttpStatus.OK);
    }

    @GetMapping("/tagName")
    @Operation(summary = "Get notes by tag containing",
            description =
                    "Returns a list of all notes that contains tag")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved notes"),
        @ApiResponse(responseCode = "404", description = "Tag not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<Note>>
        findNoteByTagName(@Parameter(description = "Name of tag", required = true)
                          @RequestParam(required = false) String tagName) {
        List<Note> notes = noteService.findNoteByTagName(tagName);
        return new ResponseEntity<>(notes, HttpStatus.OK);
    }

    @GetMapping("/username")
    @Operation(summary = "Get notes by user containing",
            description =
                    "Returns a list of all notes that contains user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved notes"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<Note>>
        findNoteByUsername(@Parameter(description = "Username", required = true)
                           @RequestParam(required = false) String username) {
        List<Note> notes = noteService.findNoteByUsername(username);
        return new ResponseEntity<>(notes, HttpStatus.OK);
    }

    @PostMapping
    @Operation(summary = "Post new note",
            description =
                    "Create new note and save it to database")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Successfully created note"),
        @ApiResponse(responseCode = "400", description = "Bad request"),
        @ApiResponse(responseCode = "404", description = "Resource not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<NoteDto> createNote(@RequestBody NoteDto noteDto) {
        NoteDto createdNote = noteService.createNote(noteDto);

        return new ResponseEntity<>(createdNote, HttpStatus.CREATED);
    }

    @PostMapping("/bulk")
    @Operation(summary = "Создать несколько заметок",
            description = "Создает несколько заметок одним запросом")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Заметки успешно созданы"),
        @ApiResponse(responseCode = "400", description = "Некорректные данные в запросе")
    })
    public ResponseEntity<List<Note>> createNotes(@RequestBody List<NoteDto> notes) {
        List<Note> createdNotes = noteService.createBulkNotes(notes);
        return new ResponseEntity<>(createdNotes, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Put note",
            description =
                    "Update note and save changes to database")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully updated note"),
        @ApiResponse(responseCode = "400", description = "Bad request"),
        @ApiResponse(responseCode = "404", description = "Note not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<NoteDto> updateNote(@Parameter(description = "Note id", required = true)
                                              @PathVariable Long id, @RequestBody NoteDto noteDto) {
        NoteDto updatedNote = noteService.updateNote(id, noteDto);
        return new ResponseEntity<>(updatedNote, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete note",
            description =
                    "Delete note from database")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Successfully deleted note"),
        @ApiResponse(responseCode = "404", description = "Note not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> deleteNote(@PathVariable long id) {
        noteService.deleteNote(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
