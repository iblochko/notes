package com.iblochko.notes.mapper;

import com.iblochko.notes.dto.UserDto;
import com.iblochko.notes.model.Note;
import com.iblochko.notes.model.Tag;
import com.iblochko.notes.model.User;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserDto toDto(User user) {
        if (user == null) {
            return null;
        }

        UserDto userDto = new UserDto();
        userDto.setUsername(user.getUsername());
        userDto.setEmail(user.getEmail());
        userDto.setPassword(user.getPassword());

        if (user.getNotes() != null) {
            Set<Long> noteIds = user.getNotes().stream()
                    .map(Note::getId)
                    .collect(Collectors.toSet());
            userDto.setNoteIds(noteIds);
        }

        if (user.getTags() != null) {
            Set<Long> tagIds = user.getTags().stream()
                    .map(Tag::getId)
                    .collect(Collectors.toSet());
            userDto.setTagIds(tagIds);
        }

        return userDto;
    }

    public User toEntity(UserDto userDto) {
        if (userDto == null) {
            return null;
        }

        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setPassword(userDto.getPassword());

        return user;
    }

    public void updateEntity(UserDto userDto, User user) {
        if (userDto == null || user == null) {
            return;
        }
        if (userDto.getUsername() != null) {
            user.setUsername(userDto.getUsername());
        }
        if (userDto.getEmail() != null) {
            user.setEmail(userDto.getEmail());
        }
        if (userDto.getPassword() != null) {
            user.setPassword(userDto.getPassword());
        }
    }
}
