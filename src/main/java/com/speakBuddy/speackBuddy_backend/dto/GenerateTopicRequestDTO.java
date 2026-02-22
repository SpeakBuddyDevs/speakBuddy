package com.speakBuddy.speackBuddy_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GenerateTopicRequestDTO {

    @NotNull(message = "La categoría es obligatoria")
    private String category;

    @NotBlank(message = "El nivel es obligatorio")
    private String level;

    @NotBlank(message = "El código de idioma es obligatorio")
    private String languageCode;
}
