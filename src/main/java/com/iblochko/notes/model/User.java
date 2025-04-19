package com.iblochko.notes.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.ToString;

@Data
@Entity
@Table(name = "users")
@ToString
@Schema(description = "Represents a user in the system")
public class User {

    @Id
    @Schema(description = "Unique username of the user", example = "korol_pelmeney")
    private String username;

    @Column(nullable = false, unique = true)
    @Schema(description = "Email address of the user", example = "korol_pelmeney56@fakemail.ru")
    private String email;

    @Column(nullable = false)
    @Schema(description = "Password for the user account", example = "yatutkarol")
    private String password;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    @Schema(description = "List of notes created by the user")
    private List<Note> notes = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonBackReference
    @ToString.Exclude
    @Schema(description = "List of tags created by the user")
    private List<Tag> tags = new ArrayList<>();
}