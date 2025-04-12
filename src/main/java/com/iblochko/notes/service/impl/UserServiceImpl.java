package com.iblochko.notes.service.impl;

import com.iblochko.notes.dto.UserDto;
import com.iblochko.notes.exception.BadRequestException;
import com.iblochko.notes.exception.ResourceNotFoundException;
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
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Primary
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final NoteRepository noteRepository;
    private final TagRepository tagRepository;
    private final UserMapper userMapper;
    private final CacheUtil cacheUtil;

    private void checkData(UserDto userDto) {
        if (userDto.getUsername() == null || userDto.getUsername().isEmpty()) {
            throw new BadRequestException("Username cannot be empty");
        }
        if (userDto.getEmail() == null || userDto.getEmail().isEmpty()) {
            throw new BadRequestException("Email cannot be empty");
        }
        if (userDto.getPassword() == null || userDto.getPassword().isEmpty()) {
            throw new BadRequestException("Password cannot be empty");
        }
    }

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
                -> new ResourceNotFoundException("User with name " + username + " not found"));
        if (user != null) {
            cacheUtil.put(cacheKey, user);
            return user;
        }
        return null;
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        checkData(userDto);
        User user = userMapper.toEntity(userDto);
        User savedUser = userRepository.save(user);
        cacheUtil.evict("user_" + savedUser.getUsername());
        return userMapper.toDto(savedUser);
    }

    @Override
    public UserDto updateUser(String username, UserDto userDto) {
        User existingUser = userRepository.findByUsername(username).orElseThrow(()
                -> new ResourceNotFoundException("User with name " + username + " not found"));

        checkData(userDto);

        userMapper.updateEntity(userDto, existingUser);
        User updatedUser = userRepository.save(existingUser);
        cacheUtil.evict("user_" + username);
        return userMapper.toDto(updatedUser);
    }

    @Override
    public void deleteUser(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(()
                -> new ResourceNotFoundException("User with name " + username + " not found"));


        noteRepository.deleteAll(user.getNotes());
        tagRepository.deleteAll(user.getTags());

        cacheUtil.evict("user_" + username);

        userRepository.delete(user);
    }
}
