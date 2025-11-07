package com.speakBuddy.speackBuddy_backend.models;

import com.speakBuddy.speackBuddy_backend.security.Role;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String profilePicture;
    private String name;
    private String surname;
    private String username;
    private String email;
    private String password;

    @ManyToOne
    private Language nativeLanguage;

    @OneToMany(
        mappedBy = "user",
        cascade = CascadeType.ALL, // Si se borra/guarda el usuario, se borran/guardan sus relaciones
        orphanRemoval = true // Si se quita un idioma de esta lista, se borra de la BD
    )
    private Set<UserLanguagesLearning> languagesToLearn = new HashSet<>();

    private Role role;
}
