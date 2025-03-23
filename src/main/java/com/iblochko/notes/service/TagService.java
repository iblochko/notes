package com.iblochko.notes.service;

import com.iblochko.notes.dto.TagDto;
import java.util.List;

public interface TagService {
    List<TagDto> getAllTags();

    TagDto getTagById(Long id);

    TagDto createTag(TagDto tagDto);

    TagDto updateTag(Long id, TagDto tagDto);

    void deleteTag(Long id);
}