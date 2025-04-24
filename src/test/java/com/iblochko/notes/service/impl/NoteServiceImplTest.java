package com.iblochko.notes.service.impl;

import com.iblochko.notes.dto.NoteDto;
import com.iblochko.notes.exception.BadRequestException;
import com.iblochko.notes.exception.ResourceNotFoundException;
import com.iblochko.notes.mapper.NoteMapper;
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

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoteServiceImplTest {

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private NoteMapper noteMapper;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CacheUtil cacheUtil;

    @InjectMocks
    private NoteServiceImpl noteService;

    private User testUser;
    private Note testNote;
    private NoteDto testNoteDto;
    private Tag testTag;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testUser");
        testUser.setNotes(new ArrayList<>());

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
        testNote.setCreatedAt(LocalDateTime.now());
        testNote.setUpdatedAt(LocalDateTime.now());

        Set<Long> tagIds = new HashSet<>();
        tagIds.add(1L);

        testNoteDto = new NoteDto();
        testNoteDto.setId(1L);
        testNoteDto.setTitle("Test Note");
        testNoteDto.setContent("Test Content");
        testNoteDto.setUsername("testUser");
        testNoteDto.setTagIds(tagIds);
    }

    @Test
    void createNote_Success() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
        when(tagRepository.findById(anyLong())).thenReturn(Optional.of(testTag));
        when(noteMapper.toEntity(any(NoteDto.class))).thenReturn(testNote);
        when(noteRepository.save(any(Note.class))).thenReturn(testNote);
        when(noteMapper.toDto(any(Note.class))).thenReturn(testNoteDto);

        // Act
        NoteDto result = noteService.createNote(testNoteDto);

        // Assert
        assertNotNull(result);
        assertEquals(testNoteDto.getTitle(), result.getTitle());
        verify(noteRepository).save(any(Note.class));
        verify(tagRepository).save(any(Tag.class));
        verify(cacheUtil).evict(anyString());
    }

    @Test
    void createNote_EmptyTitle_ThrowsBadRequestException() {
        // Arrange
        testNoteDto.setTitle("");

        // Act & Assert
        assertThrows(BadRequestException.class, () -> noteService.createNote(testNoteDto));
        verify(noteRepository, never()).save(any(Note.class));
    }

    @Test
    void createNote_EmptyUsername_ThrowsBadRequestException() {
        // Arrange
        testNoteDto.setUsername("");

        // Act & Assert
        assertThrows(BadRequestException.class, () -> noteService.createNote(testNoteDto));
        verify(noteRepository, never()).save(any(Note.class));
    }

    @Test
    void createNote_UserNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> noteService.createNote(testNoteDto));
        verify(noteRepository, never()).save(any(Note.class));
    }

    @Test
    void createNote_TagNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        // Используем HashSet вместо Collections.singletonList
        Set<Long> tagIds = new HashSet<>();
        tagIds.add(1L);
        testNoteDto.setTagIds(tagIds);

        // Убедитесь, что testNoteDto содержит имя пользователя
        testNoteDto.setUsername("testuser");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(tagRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> noteService.createNote(testNoteDto));
        verify(noteRepository, never()).save(any(Note.class));
    }

    @Test
    void createNote_TagBelongsToAnotherUser_ThrowsResourceNotFoundException() {
        // Arrange
        User anotherUser = new User();
        anotherUser.setUsername("anotherUser");

        Tag tagFromAnotherUser = new Tag();
        tagFromAnotherUser.setId(1L);
        tagFromAnotherUser.setUser(anotherUser);

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
        when(tagRepository.findById(anyLong())).thenReturn(Optional.of(tagFromAnotherUser));
        when(noteMapper.toEntity(any(NoteDto.class))).thenReturn(testNote);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> noteService.createNote(testNoteDto));
        verify(noteRepository, never()).save(any(Note.class));
    }

    @Test
    void createNote_WithoutTags_Success() {
        // Arrange
        testNoteDto.setTagIds(null);
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
        when(noteMapper.toEntity(any(NoteDto.class))).thenReturn(testNote);
        when(noteRepository.save(any(Note.class))).thenReturn(testNote);
        when(noteMapper.toDto(any(Note.class))).thenReturn(testNoteDto);

        // Act
        NoteDto result = noteService.createNote(testNoteDto);

        // Assert
        assertNotNull(result);
        verify(noteRepository).save(any(Note.class));
        verify(tagRepository, never()).save(any(Tag.class));
        verify(cacheUtil).evict(anyString());
    }

    @Test
    void createBulkNotes_Success() {
        // Arrange
        List<Note> notes = List.of(testNote);
        when(noteRepository.saveAll(anyList())).thenReturn(notes);

        // Act
        List<Note> result = noteService.createBulkNotes(notes);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(noteRepository).saveAll(anyList());
    }

    @Test
    void createBulkNotes_EmptyList_ThrowsBadRequestException() {
        // Act & Assert
        assertThrows(BadRequestException.class, () -> noteService.createBulkNotes(Collections.emptyList()));
        verify(noteRepository, never()).saveAll(anyList());
    }

    @Test
    void createBulkNotes_NullList_ThrowsBadRequestException() {
        // Act & Assert
        assertThrows(BadRequestException.class, () -> noteService.createBulkNotes(null));
        verify(noteRepository, never()).saveAll(anyList());
    }

    @Test
    void createBulkNotes_EmptyTitle_ThrowsBadRequestException() {
        // Arrange
        testNote.setTitle("");
        List<Note> notes = List.of(testNote);

        // Act & Assert
        assertThrows(BadRequestException.class, () -> noteService.createBulkNotes(notes));
        verify(noteRepository, never()).saveAll(anyList());
    }

    @Test
    void findNoteByTitle_Success() {
        // Arrange
        when(noteRepository.findByTitleContaining(anyString())).thenReturn(List.of(testNote));

        // Act
        List<Note> result = noteService.findNoteByTitle("Test");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Note", result.get(0).getTitle());
    }

    @Test
    void findNoteById_FromCache_Success() {
        // Arrange
        when(cacheUtil.get(anyString(), eq(Note.class))).thenReturn(testNote);

        // Act
        Note result = noteService.findNoteById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testNote.getId(), result.getId());
        verify(noteRepository, never()).findById(anyLong());
    }

    @Test
    void findNoteById_FromRepository_Success() {
        // Arrange
        when(cacheUtil.get(anyString(), eq(Note.class))).thenReturn(null);
        when(noteRepository.findById(anyLong())).thenReturn(Optional.of(testNote));

        // Act
        Note result = noteService.findNoteById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testNote.getId(), result.getId());
        verify(noteRepository).findById(anyLong());
        verify(cacheUtil).put(anyString(), any(Note.class));
    }

    @Test
    void findNoteById_NotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(cacheUtil.get(anyString(), eq(Note.class))).thenReturn(null);
        when(noteRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> noteService.findNoteById(1L));
    }

    @Test
    void findNoteByTagName_Success() {
        // Arrange
        when(tagRepository.findByName(anyString())).thenReturn(Optional.of(testTag));
        when(noteRepository.findByTagName(anyString())).thenReturn(List.of(testNote));

        // Act
        List<Note> result = noteService.findNoteByTagName("testTag");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void findNoteByTagName_TagNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(tagRepository.findByName(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> noteService.findNoteByTagName("nonExistentTag"));
        verify(noteRepository, never()).findByTagName(anyString());
    }

    @Test
    void findNoteByUsername_Success() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
        when(noteRepository.findByUsername(anyString())).thenReturn(List.of(testNote));

        // Act
        List<Note> result = noteService.findNoteByUsername("testUser");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void findNoteByUsername_UserNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> noteService.findNoteByUsername("nonExistentUser"));
        verify(noteRepository, never()).findByUsername(anyString());
    }

    @Test
    void updateNote_Success() {
        // Arrange
        when(noteRepository.findById(anyLong())).thenReturn(Optional.of(testNote));
        when(tagRepository.findById(anyLong())).thenReturn(Optional.of(testTag));
        when(noteRepository.save(any(Note.class))).thenReturn(testNote);
        when(noteMapper.toDto(any(Note.class))).thenReturn(testNoteDto);

        // Act
        NoteDto result = noteService.updateNote(1L, testNoteDto);

        // Assert
        assertNotNull(result);
        verify(noteRepository).save(any(Note.class));
        verify(tagRepository).save(any(Tag.class));
        verify(cacheUtil).evict(anyString());
    }

    @Test
    void updateNote_NoteNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(noteRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> noteService.updateNote(1L, testNoteDto));
        verify(noteRepository, never()).save(any(Note.class));
    }

    @Test
    void updateNote_EmptyTitle_ThrowsBadRequestException() {
        // Arrange
        testNoteDto.setTitle("");
        when(noteRepository.findById(anyLong())).thenReturn(Optional.of(testNote));

        // Act & Assert
        assertThrows(BadRequestException.class, () -> noteService.updateNote(1L, testNoteDto));
        verify(noteRepository, never()).save(any(Note.class));
    }

    @Test
    void updateNote_TagNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(noteRepository.findById(anyLong())).thenReturn(Optional.of(testNote));
        when(tagRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> noteService.updateNote(1L, testNoteDto));
        verify(noteRepository, never()).save(any(Note.class));
    }

    @Test
    void updateNote_WithoutTags_Success() {
        // Arrange
        testNoteDto.setTagIds(null);
        when(noteRepository.findById(anyLong())).thenReturn(Optional.of(testNote));
        when(noteRepository.save(any(Note.class))).thenReturn(testNote);
        when(noteMapper.toDto(any(Note.class))).thenReturn(testNoteDto);

        // Act
        NoteDto result = noteService.updateNote(1L, testNoteDto);

        // Assert
        assertNotNull(result);
        verify(noteRepository).save(any(Note.class));
        verify(tagRepository, never()).save(any(Tag.class));
        verify(cacheUtil).evict(anyString());
    }

    @Test
    void deleteNote_Success() {
        // Arrange
        testNote.getTags().add(testTag);
        testTag.getNotes().add(testNote);

        when(noteRepository.findById(anyLong())).thenReturn(Optional.of(testNote));
        doNothing().when(noteRepository).delete(any(Note.class));
        when(tagRepository.saveAll(anyCollection())).thenReturn(List.of(testTag));

        // Act
        noteService.deleteNote(1L);

        // Assert
        verify(noteRepository).delete(any(Note.class));
        verify(tagRepository).saveAll(anyCollection());
        verify(cacheUtil).evict(anyString());
    }

    @Test
    void deleteNote_NoteNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(noteRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> noteService.deleteNote(1L));
        verify(noteRepository, never()).delete(any(Note.class));
    }
}