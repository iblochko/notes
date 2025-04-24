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
import com.iblochko.notes.util.CacheUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TagServiceImplTest {

    @Mock
    private TagRepository tagRepository;

    @Mock
    private TagMapper tagMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private CacheUtil cacheUtil;

    @InjectMocks
    private TagServiceImpl tagService;

    private User testUser;
    private Tag testTag;
    private TagDto testTagDto;
    private Note testNote;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testUser");
        testUser.setNotes(new ArrayList<>());
        testUser.setTags(new ArrayList<>());

        testTag = new Tag();
        testTag.setId(1L);
        testTag.setName("testTag");
        testTag.setUser(testUser);
        testTag.setNotes(new ArrayList<>());

        testNote = new Note();
        testNote.setId(1L);
        testNote.setTitle("Test Note");
        testNote.setContent("Test Content");
        testNote.setUser(testUser);
        testNote.setTags(new ArrayList<>());

        Set<Long> noteIds = new HashSet<>();
        noteIds.add(1L);

        testTagDto = new TagDto();
        testTagDto.setId(1L);
        testTagDto.setName("testTag");
        testTagDto.setUsername("testUser");
        testTagDto.setNoteIds(noteIds);
    }

    @Test
    void getAllTags_Success() {
        // Arrange
        List<Tag> tags = List.of(testTag);
        when(tagRepository.findAll()).thenReturn(tags);

        // Act
        List<Tag> result = tagService.getAllTags();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("testTag", result.get(0).getName());
    }

    @Test
    void getTagById_FromCache_Success() {
        // Arrange
        when(cacheUtil.get(anyString(), eq(Tag.class))).thenReturn(testTag);

        // Act
        Tag result = tagService.getTagById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testTag.getId(), result.getId());
        verify(tagRepository, never()).findById(anyLong());
    }

    @Test
    void getTagById_FromRepository_Success() {
        // Arrange
        when(cacheUtil.get(anyString(), eq(Tag.class))).thenReturn(null);
        when(tagRepository.findById(anyLong())).thenReturn(Optional.of(testTag));

        // Act
        Tag result = tagService.getTagById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testTag.getId(), result.getId());
        verify(tagRepository).findById(anyLong());
        verify(cacheUtil).put(anyString(), any(Tag.class));
    }

    @Test
    void getTagById_NotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(cacheUtil.get(anyString(), eq(Tag.class))).thenReturn(null);
        when(tagRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> tagService.getTagById(1L));
    }

    @Test
    void createTag_Success() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
        when(noteRepository.findById(anyLong())).thenReturn(Optional.of(testNote));
        when(tagMapper.toEntity(any(TagDto.class))).thenReturn(testTag);
        when(tagRepository.save(any(Tag.class))).thenReturn(testTag);
        when(tagMapper.toDto(any(Tag.class))).thenReturn(testTagDto);

        // Act
        TagDto result = tagService.createTag(testTagDto);

        // Assert
        assertNotNull(result);
        assertEquals(testTagDto.getName(), result.getName());
        verify(tagRepository).save(any(Tag.class));
        verify(noteRepository).save(any(Note.class));
        verify(cacheUtil).evict(anyString());
    }

    @Test
    void createTag_EmptyName_ThrowsBadRequestException() {
        // Arrange
        testTagDto.setName("");

        // Act & Assert
        assertThrows(BadRequestException.class, () -> tagService.createTag(testTagDto));
        verify(tagRepository, never()).save(any(Tag.class));
    }

    @Test
    void createTag_EmptyUsername_ThrowsBadRequestException() {
        // Arrange
        testTagDto.setUsername("");

        // Act & Assert
        assertThrows(BadRequestException.class, () -> tagService.createTag(testTagDto));
        verify(tagRepository, never()).save(any(Tag.class));
    }

    @Test
    void createTag_UserNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> tagService.createTag(testTagDto));
        verify(tagRepository, never()).save(any(Tag.class));
    }

    @Test
    void createTag_NoteNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        // Используем HashSet вместо Collections.singletonList
        Set<Long> noteIds = new HashSet<>();
        noteIds.add(1L);
        testTagDto.setNoteIds(noteIds);

        // Убедитесь, что testTagDto содержит имя пользователя
        testTagDto.setUsername("testuser");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(noteRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> tagService.createTag(testTagDto));
        verify(tagRepository, never()).save(any(Tag.class));
    }

    @Test
    void createTag_NoteBelongsToAnotherUser_ThrowsResourceNotFoundException() {
        // Arrange
        User anotherUser = new User();
        anotherUser.setUsername("anotherUser");

        Note noteFromAnotherUser = new Note();
        noteFromAnotherUser.setId(1L);
        noteFromAnotherUser.setUser(anotherUser);

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
        when(noteRepository.findById(anyLong())).thenReturn(Optional.of(noteFromAnotherUser));
        when(tagMapper.toEntity(any(TagDto.class))).thenReturn(testTag);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> tagService.createTag(testTagDto));
        verify(tagRepository, never()).save(any(Tag.class));
    }

    @Test
    void createTag_WithoutNotes_Success() {
        // Arrange
        testTagDto.setNoteIds(null);
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
        when(tagMapper.toEntity(any(TagDto.class))).thenReturn(testTag);
        when(tagRepository.save(any(Tag.class))).thenReturn(testTag);
        when(tagMapper.toDto(any(Tag.class))).thenReturn(testTagDto);

        // Act
        TagDto result = tagService.createTag(testTagDto);

        // Assert
        assertNotNull(result);
        verify(tagRepository).save(any(Tag.class));
        verify(noteRepository, never()).save(any(Note.class));
        verify(cacheUtil).evict(anyString());
    }

    @Test
    void updateTag_Success() {
        // Arrange
        when(tagRepository.findById(anyLong())).thenReturn(Optional.of(testTag));
        when(noteRepository.findById(anyLong())).thenReturn(Optional.of(testNote));
        when(tagRepository.save(any(Tag.class))).thenReturn(testTag);
        when(tagMapper.toDto(any(Tag.class))).thenReturn(testTagDto);

        // Act
        TagDto result = tagService.updateTag(1L, testTagDto);

        // Assert
        assertNotNull(result);
        verify(tagRepository).save(any(Tag.class));
        verify(noteRepository).save(any(Note.class));
        verify(cacheUtil).evict(anyString());
    }

    @Test
    void updateTag_TagNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(tagRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> tagService.updateTag(1L, testTagDto));
        verify(tagRepository, never()).save(any(Tag.class));
    }

    @Test
    void updateTag_EmptyName_ThrowsBadRequestException() {
        // Arrange
        testTagDto.setName("");
        when(tagRepository.findById(anyLong())).thenReturn(Optional.of(testTag));

        // Act & Assert
        assertThrows(BadRequestException.class, () -> tagService.updateTag(1L, testTagDto));
        verify(tagRepository, never()).save(any(Tag.class));
    }

    @Test
    void updateTag_NoteNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(tagRepository.findById(anyLong())).thenReturn(Optional.of(testTag));
        when(noteRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> tagService.updateTag(1L, testTagDto));
        verify(tagRepository, never()).save(any(Tag.class));
    }

    @Test
    void updateTag_WithoutNotes_Success() {
        // Arrange
        testTagDto.setNoteIds(null);
        when(tagRepository.findById(anyLong())).thenReturn(Optional.of(testTag));
        when(tagRepository.save(any(Tag.class))).thenReturn(testTag);
        when(tagMapper.toDto(any(Tag.class))).thenReturn(testTagDto);

        // Act
        TagDto result = tagService.updateTag(1L, testTagDto);

        // Assert
        assertNotNull(result);
        verify(tagRepository).save(any(Tag.class));
        verify(noteRepository, never()).save(any(Note.class));
        verify(cacheUtil).evict(anyString());
    }

    @Test
    void deleteTag_Success() {
        // Arrange
        testTag.getNotes().add(testNote);
        testNote.getTags().add(testTag);

        when(tagRepository.findById(anyLong())).thenReturn(Optional.of(testTag));
        doNothing().when(tagRepository).delete(any(Tag.class));

        // Act
        tagService.deleteTag(1L);

        // Assert
        verify(tagRepository).delete(any(Tag.class));
        verify(cacheUtil).evict(anyString());
        // Verify that the tag was removed from the note's tags
        assertFalse(testNote.getTags().contains(testTag));
    }

    @Test
    void deleteTag_TagNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(tagRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> tagService.deleteTag(1L));
        verify(tagRepository, never()).delete(any(Tag.class));
    }
}