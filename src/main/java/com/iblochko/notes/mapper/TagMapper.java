package com.iblochko.notes.mapper;

import com.iblochko.notes.dto.TagDto;
import com.iblochko.notes.model.Note;
import com.iblochko.notes.model.Tag;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class TagMapper {
    public TagDto toDto(Tag tag) {
        if (tag == null) {
            return null;
        }

        TagDto tagDto = new TagDto();
        tagDto.setId(tag.getId());
        tagDto.setName(tag.getName());
        if (tag.getUser() != null) {
            tagDto.setUsername(tag.getUser().getUsername());
        }

        if (tag.getNotes() != null) {
            Set<Long> noteIds = tag.getNotes().stream()
                    .map(Note::getId)
                    .collect(Collectors.toSet());
            tagDto.setNoteIds(noteIds);
        }

        return tagDto;
    }

    public Tag toEntity(TagDto tagDto) {
        if (tagDto == null) {
            return null;
        }

        Tag tag = new Tag();
        tag.setId(tagDto.getId());
        tag.setName(tagDto.getName());

        return tag;
    }

    public void updateEntity(TagDto tagDto, Tag tag) {
        if (tagDto == null || tag == null) {
            return;
        }

        if (tagDto.getId() != null) {
            tag.setId(tagDto.getId());
        }
        if (tagDto.getName() != null) {
            tag.setName(tagDto.getName());
        }
    }
}
