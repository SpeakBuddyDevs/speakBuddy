package com.speakBuddy.speackBuddy_backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddLearningLanguageDTO {

    private Long languageId; // El ID del idioma que quiero aprender
    private Long levelId; // El ID del nivel al que lo aprendo
}
