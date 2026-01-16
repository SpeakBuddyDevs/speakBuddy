package com.speakBuddy.speackBuddy_backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class ProfileResponseDTO {

    // Campos de User que se quieren exponer en el perfil
    private Long id;
    private String email;
    private String username;
    private String name;
    private String surname;
    private String profilePictureURL;

    // Campos de gamificacion
    private Integer level;
    private Long experiencePoints;

    private Long xpToNextLevel;
    private Double progressPercentage;

    // Campos relacionales (DTOs anidados)
    private LanguageDTO nativeLanguage;
    private Set<LearningLanguageDTO> languagesToLearn;
}
