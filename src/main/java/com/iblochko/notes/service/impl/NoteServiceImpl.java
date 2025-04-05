package com.iblochko.notes.service.impl;

import com.iblochko.notes.dto.NoteDto;
import com.iblochko.notes.mapper.NoteMapper;
import com.iblochko.notes.model.Note;
import com.iblochko.notes.model.Tag;
import com.iblochko.notes.model.User;
import com.iblochko.notes.repository.NoteRepository;
import com.iblochko.notes.repository.TagRepository;
import com.iblochko.notes.repository.UserRepository;
import com.iblochko.notes.service.NoteService;
import com.iblochko.notes.util.CacheUtil;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;


@Service
@AllArgsConstructor
@Primary
public class NoteServiceImpl implements NoteService {
    private final NoteRepository noteRepository;
    private final NoteMapper noteMapper;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final CacheUtil cacheUtil;
    private final String noteNotFoundMessage = "Note not found";
    private final String userNotFoundMessage = "User not found";

    @Override
    public NoteDto createNote(NoteDto noteDto) {
        User user = userRepository.findByUsername(noteDto.getUsername()).orElseThrow(()
                -> new ResponseStatusException(HttpStatus.NOT_FOUND, userNotFoundMessage));
        List<Tag> tags;
        Note savedNote;
        Note note = noteMapper.toEntity(noteDto);
        note.setUser(user);

        if (noteDto.getTagIds() != null) {
            tags = tagRepository.findAllById(noteDto.getTagIds());
            note.getTags().addAll(tags);
            savedNote = noteRepository.save(note);
            for (Tag tag : tags) {
                tag.getNotes().add(savedNote);
                tagRepository.save(tag);
            }
        } else {
            savedNote = noteRepository.save(note);
        }

        user.getNotes().add(savedNote);

        cacheUtil.evict("note_" + savedNote.getId());

        return noteMapper.toDto(savedNote);
    }

    @Override
    public List<Note> findNoteByTitle(String title) {
        return noteRepository.findByTitleContaining(title).stream()
                .toList();
    }

    @Override
    public Note findNoteById(Long id) {
        String cacheKey = "note_" + id;

        Note cachedNote = cacheUtil.get(cacheKey, Note.class);
        if (cachedNote != null) {
            return cachedNote;
        }

        Note note = noteRepository.findById(id).orElseThrow(()
                -> new ResponseStatusException(HttpStatus.NOT_FOUND, noteNotFoundMessage));
        if (note != null) {
            cacheUtil.put(cacheKey, note);
            return note;
        }
        return null;
    }

    @Override
    public List<Note> findNoteByTagName(String tagName) {
        return noteRepository.findByTagName(tagName).stream()
                .toList();
    }

    @Override
    public List<Note> findNoteByUsername(String username) {
        return noteRepository.findByUsername(username).stream()
                .toList();
    }

    @Override
    public NoteDto updateNote(Long id, NoteDto noteDto) {
        Note existingNote = noteRepository.findById(id).orElseThrow(()
                -> new ResponseStatusException(HttpStatus.NOT_FOUND, noteNotFoundMessage));
        List<Tag> tags;
        Note updatedNote;
        noteMapper.updateEntity(noteDto, existingNote);
        if (noteDto.getTagIds() != null) {
            tags = tagRepository.findAllById(noteDto.getTagIds());
            existingNote.getTags().clear();
            existingNote.getTags().addAll(tags);
            updatedNote = noteRepository.save(existingNote);
            for (Tag tag : tags) {
                tag.getNotes().add(updatedNote);
                tagRepository.save(tag);
            }
        } else {
            updatedNote = noteRepository.save(existingNote);
        }

        cacheUtil.evict("note_" + updatedNote.getId());

        return noteMapper.toDto(updatedNote);
    }

    @Override
    @Transactional
    public void deleteNote(Long id) {
        Note note = noteRepository.findById(id).orElseThrow(()
                -> new ResponseStatusException(HttpStatus.NOT_FOUND, noteNotFoundMessage));

        List<Tag> tags = note.getTags();

        for (Tag tag : tags) {
            tag.getNotes().remove(note);
        }

        tagRepository.saveAll(tags);
        noteRepository.delete(note);

        cacheUtil.evict("note_" + id);
    }


}
