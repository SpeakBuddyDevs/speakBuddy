package com.speakBuddy.speackBuddy_backend.dto;

import lombok.*;

// Para mostrar el idioma que el usuario está aprendiendo junto con su nivel (no sirve para crear)
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LearningLanguageDTO {

    private LanguageDTO language;
    private String levelName;
    private boolean active;
}
