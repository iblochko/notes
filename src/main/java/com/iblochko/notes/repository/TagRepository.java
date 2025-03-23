package com.iblochko.notes.repository;

import com.iblochko.notes.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long> {
}