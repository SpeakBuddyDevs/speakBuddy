package com.speakBuddy.speackBuddy_backend.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
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
    @GeneratedValue
    private Long id;
    private Integer id;
    private String profilePicture;
    private String name;
    private String email;
    private String password;
    private String nativeLanguage;
    private String languageToLearn;
    private boolean isPremium;
}
