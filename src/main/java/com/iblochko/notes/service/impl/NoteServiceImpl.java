package com.iblochko.notes.service.impl;

import com.iblochko.notes.dto.NoteDto;
import com.iblochko.notes.exception.BadRequestException;
import com.iblochko.notes.exception.ResourceNotFoundException;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;


@Service
@AllArgsConstructor
@Primary
public class NoteServiceImpl implements NoteService {
    private final NoteRepository noteRepository;
    private final NoteMapper noteMapper;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final CacheUtil cacheUtil;

    @Override
    public NoteDto createNote(NoteDto noteDto) {
        if (noteDto.getTitle() == null || noteDto.getTitle().trim().isEmpty()) {
            throw new BadRequestException("Note title cannot be empty");
        }
        if (noteDto.getUsername() == null || noteDto.getUsername().trim().isEmpty()) {
            throw new BadRequestException("Username cannot be empty");
        }
        User user = userRepository.findByUsername(noteDto.getUsername()).orElseThrow(()
                -> new ResourceNotFoundException("User with name "
                + noteDto.getUsername() + " not found"));
        List<Tag> tags = new ArrayList<>();
        Note savedNote;
        Note note = noteMapper.toEntity(noteDto);
        note.setUser(user);

        if (noteDto.getTagIds() != null) {
            Set<Long> tagIds = noteDto.getTagIds();
            for (Long tagId : tagIds) {
                tags.add(tagRepository.findById(tagId).orElseThrow(() ->
                        new ResourceNotFoundException("Tag with id " + tagId + " not found")));
            }
            for (Tag tag : tags) {
                if (tag.getUser() == note.getUser()) {
                    continue;
                }
                throw new ResourceNotFoundException("Tag with id " + tag.getId() + " not found");
            }
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
    public List<Note> createBulkNotes(List<Note> notes) {
        if (notes == null || notes.isEmpty()) {
            throw new BadRequestException("Список заметок не может быть пустым");
        }

        // Валидация заметок с использованием Stream API
        boolean hasInvalidNotes = notes.stream()
                .anyMatch(note -> note.getTitle() == null || note.getTitle().trim().isEmpty());

        if (hasInvalidNotes) {
            throw new BadRequestException("Все заметки должны иметь заголовок");
        }

        // Установка времени создания/обновления для всех заметок
        LocalDateTime now = LocalDateTime.now();
        List<Note> preparedNotes = notes.stream()
                .peek(note -> {
                    note.setCreatedAt(now);
                    note.setUpdatedAt(now);
                })
                .collect(Collectors.toList());

        // Сохранение всех заметок
        return noteRepository.saveAll(preparedNotes);
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

        Note note = noteRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Note with id " + id + " not found"));
        if (note != null) {
            cacheUtil.put(cacheKey, note);
            return note;
        }
        return null;
    }

    @Override
    public List<Note> findNoteByTagName(String tagName) {
        tagRepository.findByName(tagName).orElseThrow(() ->
                new ResourceNotFoundException("Tag with name " + tagName + " not found"));
        return noteRepository.findByTagName(tagName).stream()
                .toList();
    }

    @Override
    public List<Note> findNoteByUsername(String username) {
        userRepository.findByUsername(username).orElseThrow(() ->
                new ResourceNotFoundException("User with name " + username + " not found"));
        return noteRepository.findByUsername(username).stream()
                .toList();
    }

    @Override
    public NoteDto updateNote(Long id, NoteDto noteDto) {
        Note existingNote = noteRepository.findById(id).orElseThrow(()
                -> new ResourceNotFoundException("Note with id " + id + " not found"));

        if (noteDto.getTitle() == null || noteDto.getTitle().trim().isEmpty()) {
            throw new BadRequestException("Note title cannot be empty");
        }

        List<Tag> tags = new ArrayList<>();
        Note updatedNote;
        noteMapper.updateEntity(noteDto, existingNote);
        if (noteDto.getTagIds() != null) {
            for (Long tagId : noteDto.getTagIds()) {
                tags.add(tagRepository.findById(tagId).orElseThrow(() ->
                        new ResourceNotFoundException("Tag with id " + tagId + " not found")));
            }
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
                -> new ResourceNotFoundException("Note with id " + id + " not found"));

        List<Tag> tags = note.getTags();

        for (Tag tag : tags) {
            tag.getNotes().remove(note);
        }

        tagRepository.saveAll(tags);
        noteRepository.delete(note);

        cacheUtil.evict("note_" + id);
    }


}
