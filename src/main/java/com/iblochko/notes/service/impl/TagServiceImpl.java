package com.iblochko.notes.service.impl;

import com.iblochko.notes.dto.TagDto;
import com.iblochko.notes.exception.BadRequestException;
import com.iblochko.notes.exception.ResourceNotFoundException;
import com.iblochko.notes.mapper.TagMapper;
import com.iblochko.notes.model.Note;
import com.iblochko.notes.model.Tag;
import com.iblochko.notes.model.User;
import com.iblochko.notes.repository.NoteRepository;
import com.iblochko.notes.repository.TagRepository;
import com.iblochko.notes.repository.UserRepository;
import com.iblochko.notes.service.TagService;
import com.iblochko.notes.util.CacheUtil;
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
    private final CacheUtil cacheUtil;
    private final String tagNotFoundMessage = "Tag not found";
    private final String userNotFoundMessage = "User not found";


    @Override
    public List<Tag> getAllTags() {
        return tagRepository.findAll().stream()
                .toList();
    }

    @Override
    public Tag getTagById(Long id) {
        String cacheKey = "tag_" + id;

        Tag cachedTag = cacheUtil.get(cacheKey, Tag.class);
        if (cachedTag != null) {
            return cachedTag;
        }

        Tag tag = tagRepository.findById(id).orElseThrow(()
                -> new ResourceNotFoundException("Tag with id " + id + " not found"));
        if (tag != null) {
            cacheUtil.put(cacheKey, tag);
            return tag;
        }
        return null;
    }

    @Override
    public TagDto createTag(TagDto tagDto) {
        if (tagDto.getName() == null || tagDto.getName().trim().isEmpty()) {
            throw new BadRequestException("Tag name cannot be empty");
        }
        User user = userRepository.findByUsername(tagDto.getUsername()).orElseThrow(()
                -> new ResourceNotFoundException("User with name "
                + tagDto.getUsername() + " not found"));
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

        cacheUtil.evict("tag_" + savedTag.getId());

        return tagMapper.toDto(savedTag);
    }

    @Override
    public TagDto updateTag(Long id, TagDto tagDto) {
        Tag existingTag = tagRepository.findById(id).orElseThrow(()
                -> new ResourceNotFoundException("Tag with id " + id + " not found"));

        if (tagDto.getName() == null || tagDto.getName().trim().isEmpty()) {
            throw new BadRequestException("Tag name cannot be empty");
        }

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

        cacheUtil.evict("tag_" + id);

        return tagMapper.toDto(updatedTag);
    }

    @Override
    public void deleteTag(Long id) {
        Tag tag = tagRepository.findById(id).orElseThrow(()
                -> new ResourceNotFoundException("Tag with id " + id + " not found"));

        List<Note> notes = tag.getNotes();

        for (Note note : notes) {
            note.getTags().remove(tag);
        }

        cacheUtil.evict("tag_" + id);

        tagRepository.delete(tag);
    }
}