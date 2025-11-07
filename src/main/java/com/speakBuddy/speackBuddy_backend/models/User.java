package com.speakBuddy.speackBuddy_backend.models;

import com.speakBuddy.speackBuddy_backend.security.Role;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
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

    @Column(nullable = false, unique = true, length = 254)
    private String email; // username para spring security

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String surname;

    @Column(nullable = false)
    private String username; // nombre de usuario público que verán los demás

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @ManyToOne(optional = false)
    @JoinColumn(name = "native_language_id", nullable = false)
    private Language nativeLanguage;

    @OneToMany(
        mappedBy = "user",
        cascade = CascadeType.ALL, // Si se borra/guarda el usuario, se borran/guardan sus relaciones
        orphanRemoval = true // Si se quita un idioma de esta lista, se borra de la BD
    )
    private Set<UserLanguagesLearning> languagesToLearn = new HashSet<>();

    private String profilePicture;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

}
