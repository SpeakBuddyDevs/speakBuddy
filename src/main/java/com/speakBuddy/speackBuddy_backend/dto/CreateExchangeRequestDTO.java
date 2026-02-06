package com.speakBuddy.speackBuddy_backend.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CreateExchangeRequestDTO {

    @NotNull(message = "La fecha y hora son obligatorias")
    private LocalDateTime scheduledAt;

    @NotNull(message = "La duración es obligatoria")
    @Min(value = 15, message = "La duración mínima es 15 minutos")
    @Max(value = 480, message = "La duración máxima es 8 horas")
    private Integer durationMinutes;

    /**
     * IDs de usuarios que participan (además del creador).
     * Puede estar vacío para intercambios 1:1 donde el creador añadirá al otro después.
     */
    private List<Long> participantUserIds;

    @Size(max = 200, message = "El título no puede superar los 200 caracteres")
    private String title;

    /** true si el intercambio aparece en el catálogo público */
    private Boolean isPublic;

    /** Máximo de participantes (requerido si isPublic=true) */
    @Min(value = 2, message = "El mínimo de participantes es 2")
    @Max(value = 100, message = "El máximo de participantes es 100")
    private Integer maxParticipants;

    @Size(max = 2000, message = "La descripción no puede superar los 2000 caracteres")
    private String description;

    /** Código ISO del idioma nativo que el creador ofrece (ej: ES, EN). Requerido si isPublic=true */
    @Size(max = 10)
    private String nativeLanguageCode;

    /** Código ISO del idioma que el creador quiere practicar. Requerido si isPublic=true */
    @Size(max = 10)
    private String targetLanguageCode;

    /** Nivel requerido: Principiante, Intermedio, Avanzado. Requerido si isPublic=true */
    @Size(max = 50)
    private String requiredLevel;
}
