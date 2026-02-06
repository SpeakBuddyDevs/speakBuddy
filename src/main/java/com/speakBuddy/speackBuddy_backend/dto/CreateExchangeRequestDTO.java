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
}
