package com.iblochko.notes.service.impl;

import com.iblochko.notes.dto.UserDto;
import com.iblochko.notes.exception.BadRequestException;
import com.iblochko.notes.exception.ResourceNotFoundException;
import com.iblochko.notes.mapper.UserMapper;
import com.iblochko.notes.model.User;
import com.iblochko.notes.model.Note;
import com.iblochko.notes.model.Tag;
import com.iblochko.notes.repository.NoteRepository;
import com.iblochko.notes.repository.TagRepository;
import com.iblochko.notes.repository.UserRepository;
import com.iblochko.notes.util.CacheUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private CacheUtil cacheUtil;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UserDto testUserDto;
    private List<User> userList;
    private List<Note> noteList;
    private List<Tag> tagList;

    @BeforeEach
    void setUp() {

        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");


        noteList = Collections.singletonList(new Note());
        tagList = Collections.singletonList(new Tag());
        testUser.setNotes(noteList);
        testUser.setTags(tagList);


        testUserDto = new UserDto();
        testUserDto.setUsername("testuser");
        testUserDto.setEmail("test@example.com");
        testUserDto.setPassword("password");


        User user1 = new User();
        user1.setUsername("user1");
        User user2 = new User();
        user2.setUsername("user2");
        userList = Arrays.asList(user1, user2);
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {

        when(userRepository.findAll()).thenReturn(userList);


        List<User> result = userService.getAllUsers();


        assertEquals(2, result.size());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void getUserByUsername_WithCachedUser_ShouldReturnCachedUser() {

        String username = "testuser";
        when(cacheUtil.get("user_" + username, User.class)).thenReturn(testUser);


        User result = userService.getUserByUsername(username);


        assertEquals(testUser, result);
        verify(cacheUtil, times(1)).get("user_" + username, User.class);
        verify(userRepository, never()).findByUsername(anyString());
    }

    @Test
    void getUserByUsername_WithoutCachedUser_ShouldFetchFromRepository() {

        String username = "testuser";
        when(cacheUtil.get("user_" + username, User.class)).thenReturn(null);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));


        User result = userService.getUserByUsername(username);


        assertEquals(testUser, result);
        verify(cacheUtil, times(1)).get("user_" + username, User.class);
        verify(userRepository, times(1)).findByUsername(username);
        verify(cacheUtil, times(1)).put("user_" + username, testUser);
    }

    @Test
    void getUserByUsername_UserNotFound_ShouldThrowException() {

        String username = "nonexistent";
        when(cacheUtil.get("user_" + username, User.class)).thenReturn(null);
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());


        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> userService.getUserByUsername(username));

        assertEquals("User with name nonexistent not found", exception.getMessage());
        verify(cacheUtil, times(1)).get("user_" + username, User.class);
        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    void createUser_ValidData_ShouldCreateUser() {

        when(userMapper.toEntity(testUserDto)).thenReturn(testUser);
        when(userRepository.save(testUser)).thenReturn(testUser);
        when(userMapper.toDto(testUser)).thenReturn(testUserDto);


        UserDto result = userService.createUser(testUserDto);


        assertEquals(testUserDto, result);
        verify(userMapper, times(1)).toEntity(testUserDto);
        verify(userRepository, times(1)).save(testUser);
        verify(cacheUtil, times(1)).evict("user_" + testUser.getUsername());
        verify(userMapper, times(1)).toDto(testUser);
    }

    @Test
    void createUser_EmptyUsername_ShouldThrowException() {

        testUserDto.setUsername("");


        BadRequestException exception = assertThrows(BadRequestException.class, () -> userService.createUser(testUserDto));

        assertEquals("Username cannot be empty", exception.getMessage());
    }

    @Test
    void createUser_NullUsername_ShouldThrowException() {

        testUserDto.setUsername(null);


        BadRequestException exception = assertThrows(BadRequestException.class, () -> userService.createUser(testUserDto));

        assertEquals("Username cannot be empty", exception.getMessage());
    }

    @Test
    void createUser_EmptyEmail_ShouldThrowException() {

        testUserDto.setEmail("");


        BadRequestException exception = assertThrows(BadRequestException.class, () -> userService.createUser(testUserDto));

        assertEquals("Email cannot be empty", exception.getMessage());
    }

    @Test
    void createUser_NullEmail_ShouldThrowException() {

        testUserDto.setEmail(null);


        BadRequestException exception = assertThrows(BadRequestException.class, () -> userService.createUser(testUserDto));

        assertEquals("Email cannot be empty", exception.getMessage());
    }

    @Test
    void createUser_EmptyPassword_ShouldThrowException() {

        testUserDto.setPassword("");


        BadRequestException exception = assertThrows(BadRequestException.class, () -> userService.createUser(testUserDto));

        assertEquals("Password cannot be empty", exception.getMessage());
    }

    @Test
    void createUser_NullPassword_ShouldThrowException() {

        testUserDto.setPassword(null);


        BadRequestException exception = assertThrows(BadRequestException.class, () -> userService.createUser(testUserDto));

        assertEquals("Password cannot be empty", exception.getMessage());
    }

    @Test
    void updateUser_ValidData_ShouldUpdateUser() {

        String username = "testuser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(userRepository.save(testUser)).thenReturn(testUser);
        when(userMapper.toDto(testUser)).thenReturn(testUserDto);


        UserDto result = userService.updateUser(username, testUserDto);


        assertEquals(testUserDto, result);
        verify(userRepository, times(1)).findByUsername(username);
        verify(userMapper, times(1)).updateEntity(testUserDto, testUser);
        verify(userRepository, times(1)).save(testUser);
        verify(cacheUtil, times(1)).evict("user_" + username);
        verify(userMapper, times(1)).toDto(testUser);
    }

    @Test
    void updateUser_UserNotFound_ShouldThrowException() {

        String username = "nonexistent";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());


        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> userService.updateUser(username, testUserDto));

        assertEquals("User with name nonexistent not found", exception.getMessage());
        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    void updateUser_InvalidData_ShouldThrowException() {

        String username = "testuser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        testUserDto.setUsername("");


        BadRequestException exception = assertThrows(BadRequestException.class, () -> userService.updateUser(username, testUserDto));

        assertEquals("Username cannot be empty", exception.getMessage());
        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    void deleteUser_ExistingUser_ShouldDeleteUser() {

        String username = "testuser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));


        userService.deleteUser(username);


        verify(userRepository, times(1)).findByUsername(username);
        verify(noteRepository, times(1)).deleteAll(noteList);
        verify(tagRepository, times(1)).deleteAll(tagList);
        verify(cacheUtil, times(1)).evict("user_" + username);
        verify(userRepository, times(1)).delete(testUser);
    }

    @Test
    void deleteUser_UserNotFound_ShouldThrowException() {

        String username = "nonexistent";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());


        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(username));

        assertEquals("User with name nonexistent not found", exception.getMessage());
        verify(userRepository, times(1)).findByUsername(username);
        verify(noteRepository, never()).deleteAll(any());
        verify(tagRepository, never()).deleteAll(any());
        verify(cacheUtil, never()).evict(anyString());
        verify(userRepository, never()).delete(any());
    }
}