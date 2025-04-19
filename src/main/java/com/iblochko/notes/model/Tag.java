package com.iblochko.notes.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.ToString;


@Data
@Entity
@Table(name = "tags")
@ToString
@Schema(description = "Represents a tag in the system")
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier for the tag", example = "1")
    private Long id;

    @Column(nullable = false, unique = true)
    @Schema(description = "Name of the tag", example = "Work")
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonBackReference
    @ToString.Exclude
    @Schema(description = "User associated with the tag")
    private User user;

    @ManyToMany(mappedBy = "tags")
    @JsonBackReference
    @ToString.Exclude
    @Schema(description = "List of notes associated with the tag")
    private List<Note> notes = new ArrayList<>();
}