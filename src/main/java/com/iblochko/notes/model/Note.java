package com.iblochko.notes.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Data
@Entity
@Table(name = "notes")
@ToString
@Schema(description = "Represents a note in the system")
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier for the note", example = "1")
    private Long id;

    @Column(nullable = false, unique = true)
    @Schema(description = "Title of the note", example = "Shopping List")
    private String title;

    @Schema(description = "Content of the note", example = "Milk, bread, honey")
    private String content;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    @Schema(description = "Creation date and time of the note in ISO 8601 format",
            example = "2025-04-17T10:30:00")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    @Schema(description = "Last updated date and time of the note in ISO 8601 format",
            example = "2025-04-17T10:30:00")
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "username")
    @JsonBackReference
    @ToString.Exclude
    @Schema(description = "User associated with the note")
    private User user;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinTable(
            name = "note_tags",
            joinColumns = @JoinColumn(name = "note_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @JsonManagedReference
    @Schema(description = "List of tags associated with the note")
    private List<Tag> tags = new ArrayList<>();
}