package com.iblochko.notes.repository;

import com.iblochko.notes.model.Note;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findByTitleContaining(String title);

    @Query("SELECT s FROM Note s JOIN s.tags c WHERE c.name = :tagName")
    List<Note> findByTagName(@Param("tagName") String tagName);

    @Query(value = """
        SELECT n.*
        FROM notes n
        INNER JOIN users u ON n.username = u.username
        WHERE u.username = :username
        """,
            nativeQuery = true)
    List<Note> findByUsername(@Param("username") String username);
}
