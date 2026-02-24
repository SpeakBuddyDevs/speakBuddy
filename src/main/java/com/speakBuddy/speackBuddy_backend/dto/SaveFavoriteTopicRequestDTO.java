package com.speakBuddy.speackBuddy_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SaveFavoriteTopicRequestDTO {

    @NotNull(message = "La categoría es obligatoria")
    private String category;

    @NotBlank(message = "El nivel es obligatorio")
    private String level;

    @NotBlank(message = "El texto principal es obligatorio")
    private String mainText;

    private String positionA;
    private String positionB;

    private List<String> suggestedVocabulary;

    @NotBlank(message = "El idioma es obligatorio")
    private String language;

    @NotNull(message = "La fecha de generación es obligatoria")
    private LocalDateTime generatedAt;
}
