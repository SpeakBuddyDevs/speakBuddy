package com.speakBuddy.speackBuddy_backend.dto;

import lombok.Getter;
import lombok.Setter;

// Para mostrar el idioma que el usuario está aprendiendo junto con su nivel (no sirve para crear)
@Getter
@Setter
public class LearningLanguageDTO {

    private LanguageDTO language;
    private String levelName;
}
