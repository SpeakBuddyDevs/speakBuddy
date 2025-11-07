package com.speakBuddy.speackBuddy_backend.models;

import com.speakBuddy.speackBuddy_backend.security.Role;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String profilePicture;
    private String name;
    private String surname;
    private String username;
    private String email;
    private String password;
    private String nativeLanguage;
    private String languageToLearn;
    private Role role;
}
