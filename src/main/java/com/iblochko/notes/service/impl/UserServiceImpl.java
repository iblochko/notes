package com.iblochko.notes.service.impl;

import com.iblochko.notes.dto.UserDto;
import com.iblochko.notes.mapper.UserMapper;
import com.iblochko.notes.model.User;
import com.iblochko.notes.repository.NoteRepository;
import com.iblochko.notes.repository.TagRepository;
import com.iblochko.notes.repository.UserRepository;
import com.iblochko.notes.service.UserService;
import com.iblochko.notes.util.CacheUtil;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@AllArgsConstructor
@Primary
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final NoteRepository noteRepository;
    private final TagRepository tagRepository;
    private final UserMapper userMapper;
    private final CacheUtil cacheUtil;
    private final String userNotFoundMessage = "User not found";

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll().stream()
                .toList();
    }

    @Override
    public User getUserByUsername(String username) {
        String cacheKey = "user_" + username;

        User cachedUser = cacheUtil.get(cacheKey, User.class);
        if (cachedUser != null) {
            return cachedUser;
        }

        User user = userRepository.findByUsername(username).orElseThrow(()
                -> new ResponseStatusException(HttpStatus.NOT_FOUND, userNotFoundMessage));
        if (user != null) {
            cacheUtil.put(cacheKey, user);
            return user;
        }
        return null;
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        User user = userMapper.toEntity(userDto);
        User savedUser = userRepository.save(user);
        cacheUtil.evict("user_" + savedUser.getUsername());
        return userMapper.toDto(savedUser);
    }

    @Override
    public UserDto updateUser(String username, UserDto userDto) {
        User existingUser = userRepository.findByUsername(username).orElseThrow(()
                -> new ResponseStatusException(HttpStatus.NOT_FOUND, userNotFoundMessage));

        userMapper.updateEntity(userDto, existingUser);
        User updatedUser = userRepository.save(existingUser);
        cacheUtil.evict("user_" + username);
        return userMapper.toDto(updatedUser);
    }

    @Override
    public void deleteUser(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(()
                -> new ResponseStatusException(HttpStatus.NOT_FOUND, userNotFoundMessage));


        noteRepository.deleteAll(user.getNotes());
        tagRepository.deleteAll(user.getTags());

        cacheUtil.evict("user_" + username);

        userRepository.delete(user);
    }
}
