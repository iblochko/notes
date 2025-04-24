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
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
        when(tagRepository.findById(anyLong())).thenReturn(Optional.of(testTag));
        when(noteMapper.toEntity(any(NoteDto.class))).thenReturn(testNote);
        when(noteRepository.save(any(Note.class))).thenReturn(testNote);
        when(noteMapper.toDto(any(Note.class))).thenReturn(testNoteDto);

        NoteDto result = noteService.createNote(testNoteDto);

        assertNotNull(result);
        assertEquals(testNoteDto.getTitle(), result.getTitle());
        verify(noteRepository).save(any(Note.class));
        verify(tagRepository).save(any(Tag.class));
        verify(cacheUtil).evict(anyString());
    }

    @Test
    void createNote_EmptyTitle_ThrowsBadRequestException() {
        testNoteDto.setTitle("");

        assertThrows(BadRequestException.class, () -> noteService.createNote(testNoteDto));
        verify(noteRepository, never()).save(any(Note.class));
    }

    @Test
    void createNote_EmptyUsername_ThrowsBadRequestException() {
        testNoteDto.setUsername("");

        assertThrows(BadRequestException.class, () -> noteService.createNote(testNoteDto));
        verify(noteRepository, never()).save(any(Note.class));
    }

    @Test
    void createNote_UserNotFound_ThrowsResourceNotFoundException() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> noteService.createNote(testNoteDto));
        verify(noteRepository, never()).save(any(Note.class));
    }

    @Test
    void createNote_TagNotFound_ThrowsResourceNotFoundException() {
        Set<Long> tagIds = new HashSet<>();
        tagIds.add(1L);
        testNoteDto.setTagIds(tagIds);

        testNoteDto.setUsername("testuser");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(tagRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> noteService.createNote(testNoteDto));
        verify(noteRepository, never()).save(any(Note.class));
    }

    @Test
    void createNote_TagBelongsToAnotherUser_ThrowsResourceNotFoundException() {
        User anotherUser = new User();
        anotherUser.setUsername("anotherUser");

        Tag tagFromAnotherUser = new Tag();
        tagFromAnotherUser.setId(1L);
        tagFromAnotherUser.setUser(anotherUser);

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
        when(tagRepository.findById(anyLong())).thenReturn(Optional.of(tagFromAnotherUser));
        when(noteMapper.toEntity(any(NoteDto.class))).thenReturn(testNote);

        assertThrows(ResourceNotFoundException.class, () -> noteService.createNote(testNoteDto));
        verify(noteRepository, never()).save(any(Note.class));
    }

    @Test
    void createNote_WithoutTags_Success() {
        testNoteDto.setTagIds(null);
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
        when(noteMapper.toEntity(any(NoteDto.class))).thenReturn(testNote);
        when(noteRepository.save(any(Note.class))).thenReturn(testNote);
        when(noteMapper.toDto(any(Note.class))).thenReturn(testNoteDto);

        NoteDto result = noteService.createNote(testNoteDto);

        assertNotNull(result);
        verify(noteRepository).save(any(Note.class));
        verify(tagRepository, never()).save(any(Tag.class));
        verify(cacheUtil).evict(anyString());
    }

    @Test
    void createBulkNotes_WithValidNotes_ShouldReturnSavedNotes() {
        List<NoteDto> notesDto = List.of(testNoteDto, testNoteDto);

        when(noteService.createNote(any(NoteDto.class))).thenReturn(testNoteDto);
        when(noteMapper.toEntity(any(NoteDto.class))).thenReturn(testNote);
        when(noteRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        List<Note> result = noteService.createBulkNotes(notesDto);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(noteService, times(2)).createNote(any(NoteDto.class));
        verify(noteMapper, times(2)).toEntity(any(NoteDto.class));
        verify(noteRepository).saveAll(anyList());

        for (Note note : result) {
            assertNotNull(note.getCreatedAt());
            assertNotNull(note.getUpdatedAt());
        }
    }

    @Test
    void createBulkNotes_WithNullList_ShouldThrowBadRequestException() {
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> noteService.createBulkNotes(null)
        );

        assertEquals("The list of notes cannot be empty", exception.getMessage());
        verify(noteRepository, never()).saveAll(anyList());
    }

    @Test
    void createBulkNotes_WithEmptyList_ShouldThrowBadRequestException() {
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> noteService.createBulkNotes(Collections.emptyList())
        );

        assertEquals("The list of notes cannot be empty", exception.getMessage());
        verify(noteRepository, never()).saveAll(anyList());
    }

    @Test
    void createBulkNotes_WithEmptyTitle_ShouldThrowBadRequestException() {
        NoteDto invalidNoteDto = new NoteDto();
        invalidNoteDto.setTitle("");
        invalidNoteDto.setUsername("testuser");

        List<NoteDto> noteDtos = List.of(testNoteDto, invalidNoteDto);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> noteService.createBulkNotes(noteDtos)
        );

        assertEquals("Note title cannot be empty", exception.getMessage());
        verify(noteRepository, never()).saveAll(anyList());
    }

    @Test
    void createBulkNotes_WithNullTitle_ShouldThrowBadRequestException() {
        NoteDto invalidNoteDto = new NoteDto();
        invalidNoteDto.setTitle(null);
        invalidNoteDto.setUsername("testuser");

        List<NoteDto> noteDtos = List.of(testNoteDto, invalidNoteDto);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> noteService.createBulkNotes(noteDtos)
        );

        assertEquals("Note title cannot be empty", exception.getMessage());
        verify(noteRepository, never()).saveAll(anyList());
    }

    @Test
    void createBulkNotes_WithEmptyUsername_ShouldThrowBadRequestException() {
        NoteDto invalidNoteDto = new NoteDto();
        invalidNoteDto.setTitle("Test Title");
        invalidNoteDto.setUsername("");

        List<NoteDto> noteDtos = List.of(testNoteDto, invalidNoteDto);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> noteService.createBulkNotes(noteDtos)
        );

        assertEquals("Note username cannot be empty", exception.getMessage());
        verify(noteRepository, never()).saveAll(anyList());
    }

    @Test
    void createBulkNotes_WithNullUsername_ShouldThrowBadRequestException() {
        NoteDto invalidNoteDto = new NoteDto();
        invalidNoteDto.setTitle("Test Title");
        invalidNoteDto.setUsername(null);

        List<NoteDto> noteDtos = List.of(testNoteDto, invalidNoteDto);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> noteService.createBulkNotes(noteDtos)
        );

        assertEquals("Note username cannot be empty", exception.getMessage());
        verify(noteRepository, never()).saveAll(anyList());
    }

    @Test
    void findNoteByTitle_Success() {
        when(noteRepository.findByTitleContaining(anyString())).thenReturn(List.of(testNote));

        List<Note> result = noteService.findNoteByTitle("Test");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Note", result.get(0).getTitle());
    }

    @Test
    void findNoteById_FromCache_Success() {
        when(cacheUtil.get(anyString(), eq(Note.class))).thenReturn(testNote);

        Note result = noteService.findNoteById(1L);

        assertNotNull(result);
        assertEquals(testNote.getId(), result.getId());
        verify(noteRepository, never()).findById(anyLong());
    }

    @Test
    void findNoteById_FromRepository_Success() {
        when(cacheUtil.get(anyString(), eq(Note.class))).thenReturn(null);
        when(noteRepository.findById(anyLong())).thenReturn(Optional.of(testNote));

        Note result = noteService.findNoteById(1L);

        assertNotNull(result);
        assertEquals(testNote.getId(), result.getId());
        verify(noteRepository).findById(anyLong());
        verify(cacheUtil).put(anyString(), any(Note.class));
    }

    @Test
    void findNoteById_NotFound_ThrowsResourceNotFoundException() {
        when(cacheUtil.get(anyString(), eq(Note.class))).thenReturn(null);
        when(noteRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> noteService.findNoteById(1L));
    }

    @Test
    void findNoteByTagName_Success() {
        when(tagRepository.findByName(anyString())).thenReturn(Optional.of(testTag));
        when(noteRepository.findByTagName(anyString())).thenReturn(List.of(testNote));

        List<Note> result = noteService.findNoteByTagName("testTag");

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void findNoteByTagName_TagNotFound_ThrowsResourceNotFoundException() {
        when(tagRepository.findByName(anyString())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> noteService.findNoteByTagName("nonExistentTag"));
        verify(noteRepository, never()).findByTagName(anyString());
    }

    @Test
    void findNoteByUsername_Success() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
        when(noteRepository.findByUsername(anyString())).thenReturn(List.of(testNote));

        List<Note> result = noteService.findNoteByUsername("testUser");

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void findNoteByUsername_UserNotFound_ThrowsResourceNotFoundException() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> noteService.findNoteByUsername("nonExistentUser"));
        verify(noteRepository, never()).findByUsername(anyString());
    }

    @Test
    void updateNote_Success() {
        when(noteRepository.findById(anyLong())).thenReturn(Optional.of(testNote));
        when(tagRepository.findById(anyLong())).thenReturn(Optional.of(testTag));
        when(noteRepository.save(any(Note.class))).thenReturn(testNote);
        when(noteMapper.toDto(any(Note.class))).thenReturn(testNoteDto);

        NoteDto result = noteService.updateNote(1L, testNoteDto);

        assertNotNull(result);
        verify(noteRepository).save(any(Note.class));
        verify(tagRepository).save(any(Tag.class));
        verify(cacheUtil).evict(anyString());
    }

    @Test
    void updateNote_NoteNotFound_ThrowsResourceNotFoundException() {
        when(noteRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> noteService.updateNote(1L, testNoteDto));
        verify(noteRepository, never()).save(any(Note.class));
    }

    @Test
    void updateNote_EmptyTitle_ThrowsBadRequestException() {
        testNoteDto.setTitle("");
        when(noteRepository.findById(anyLong())).thenReturn(Optional.of(testNote));

        assertThrows(BadRequestException.class, () -> noteService.updateNote(1L, testNoteDto));
        verify(noteRepository, never()).save(any(Note.class));
    }

    @Test
    void updateNote_TagNotFound_ThrowsResourceNotFoundException() {
        when(noteRepository.findById(anyLong())).thenReturn(Optional.of(testNote));
        when(tagRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> noteService.updateNote(1L, testNoteDto));
        verify(noteRepository, never()).save(any(Note.class));
    }

    @Test
    void updateNote_WithoutTags_Success() {
        testNoteDto.setTagIds(null);
        when(noteRepository.findById(anyLong())).thenReturn(Optional.of(testNote));
        when(noteRepository.save(any(Note.class))).thenReturn(testNote);
        when(noteMapper.toDto(any(Note.class))).thenReturn(testNoteDto);

        NoteDto result = noteService.updateNote(1L, testNoteDto);

        assertNotNull(result);
        verify(noteRepository).save(any(Note.class));
        verify(tagRepository, never()).save(any(Tag.class));
        verify(cacheUtil).evict(anyString());
    }

    @Test
    void deleteNote_Success() {
        testNote.getTags().add(testTag);
        testTag.getNotes().add(testNote);

        when(noteRepository.findById(anyLong())).thenReturn(Optional.of(testNote));
        doNothing().when(noteRepository).delete(any(Note.class));
        when(tagRepository.saveAll(anyCollection())).thenReturn(List.of(testTag));

        noteService.deleteNote(1L);

        verify(noteRepository).delete(any(Note.class));
        verify(tagRepository).saveAll(anyCollection());
        verify(cacheUtil).evict(anyString());
    }

    @Test
    void deleteNote_NoteNotFound_ThrowsResourceNotFoundException() {
        when(noteRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> noteService.deleteNote(1L));
        verify(noteRepository, never()).delete(any(Note.class));
    }
}