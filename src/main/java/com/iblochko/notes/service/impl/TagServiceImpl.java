package com.iblochko.notes.service.impl;

import com.iblochko.notes.dto.TagDto;
import com.iblochko.notes.mapper.TagMapper;
import com.iblochko.notes.model.Note;
import com.iblochko.notes.model.Tag;
import com.iblochko.notes.model.User;
import com.iblochko.notes.repository.NoteRepository;
import com.iblochko.notes.repository.TagRepository;
import com.iblochko.notes.repository.UserRepository;
import com.iblochko.notes.service.TagService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@AllArgsConstructor
public class TagServiceImpl implements TagService {
    private final TagRepository tagRepository;
    private final TagMapper tagMapper;
    private final UserRepository userRepository;
    private final NoteRepository noteRepository;
    private final String tagNotFoundMessage = "Tag not found";
    private final String userNotFoundMessage = "User not found";


    @Override
    public List<Tag> getAllTags() {
        return tagRepository.findAll().stream()
                .toList();
    }

    @Override
    public Tag getTagById(Long id) {
        return tagRepository.findById(id).orElseThrow(()
                -> new ResponseStatusException(HttpStatus.NOT_FOUND, tagNotFoundMessage));
    }

    @Override
    public TagDto createTag(TagDto tagDto) {
        User user = userRepository.findByUsername(tagDto.getUsername()).orElseThrow(()
                -> new ResponseStatusException(HttpStatus.NOT_FOUND, userNotFoundMessage));
        List<Note> notes;
        Tag savedTag;
        Tag tag = tagMapper.toEntity(tagDto);
        tag.setUser(user);

        if (tagDto.getNoteIds() != null) {
            notes = noteRepository.findAllById(tagDto.getNoteIds());
            tag.getNotes().addAll(notes);
            savedTag = tagRepository.save(tag);
            for (Note note : notes) {
                note.getTags().add(savedTag);
                noteRepository.save(note);
            }
        } else {
            savedTag = tagRepository.save(tag);
        }

        user.getTags().add(savedTag);

        return tagMapper.toDto(savedTag);
    }

    @Override
    public TagDto updateTag(Long id, TagDto tagDto) {
        Tag existingTag = tagRepository.findById(id).orElseThrow(()
                -> new ResponseStatusException(HttpStatus.NOT_FOUND, tagNotFoundMessage));
        List<Note> notes;
        Tag updatedTag;
        tagMapper.updateEntity(tagDto, existingTag);
        if (tagDto.getNoteIds() != null) {
            notes = noteRepository.findAllById(tagDto.getNoteIds());
            existingTag.getNotes().clear();
            existingTag.getNotes().addAll(notes);
            updatedTag = tagRepository.save(existingTag);
            for (Note note : notes) {
                note.getTags().add(updatedTag);
                noteRepository.save(note);
            }
        } else {
            updatedTag = tagRepository.save(existingTag);
        }

        return tagMapper.toDto(updatedTag);
    }

    @Override
    public void deleteTag(Long id) {
        Tag tag = tagRepository.findById(id).orElseThrow(()
                -> new ResponseStatusException(HttpStatus.NOT_FOUND, tagNotFoundMessage));

        List<Note> notes = tag.getNotes();

        for (Note note : notes) {
            note.getTags().remove(tag);
        }
        tagRepository.delete(tag);
    }
}