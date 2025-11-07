package com.speakBuddy.speackBuddy_backend.models;

import com.speakBuddy.speackBuddy_backend.security.Role;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
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
