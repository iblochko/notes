package com.iblochko.notes.service;

import com.iblochko.notes.dto.TagDto;
import com.iblochko.notes.model.Tag;
import java.util.List;

public interface TagService {
    List<Tag> getAllTags();

    Tag getTagById(Long id);

    TagDto createTag(TagDto tagDto);

    TagDto updateTag(Long id, TagDto tagDto);

    void deleteTag(Long id);
}