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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class TagServiceImpl implements TagService {
    private final TagRepository tagRepository;
    private final TagMapper tagMapper;
    private final UserRepository userRepository;
    private final NoteRepository noteRepository;
    private final CacheUtil cacheUtil;


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
        if (tagDto.getUsername() == null || tagDto.getUsername().trim().isEmpty()) {
            throw new BadRequestException("Username cannot be empty");
        }
        User user = userRepository.findByUsername(tagDto.getUsername()).orElseThrow(()
                -> new ResourceNotFoundException("User with name "
                + tagDto.getUsername() + " not found"));
        List<Note> notes = new ArrayList<>();
        Tag savedTag;
        Tag tag = tagMapper.toEntity(tagDto);
        tag.setUser(user);

        if (tagDto.getNoteIds() != null) {
            Set<Long> noteIds = tagDto.getNoteIds();
            for (Long noteId : noteIds) {
                notes.add(noteRepository.findById(noteId).orElseThrow(()
                        -> new ResourceNotFoundException("Note with id " + noteId + " not found")
                ));
            }

            for (Note note : notes) {
                if (note.getUser() == tag.getUser()) {
                    continue;
                }
                throw new ResourceNotFoundException("Note with id " + note.getId() + " not found");
            }
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

        List<Note> notes = new ArrayList<>();
        Tag updatedTag;
        tagMapper.updateEntity(tagDto, existingTag);
        if (tagDto.getNoteIds() != null) {
            for  (Long noteId : tagDto.getNoteIds()) {
                notes.add(noteRepository.findById(noteId).orElseThrow(()
                        -> new ResourceNotFoundException("Note with id " + noteId + " not found")));
            }
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