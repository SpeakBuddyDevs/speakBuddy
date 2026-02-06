package com.speakBuddy.speackBuddy_backend.dto;

import com.speakBuddy.speackBuddy_backend.models.ExchangeStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ExchangeResponseDTO {
    private Long id;
    private LocalDateTime scheduledAt;
    private Integer durationMinutes;
    private ExchangeStatus status;
    private String type;
    private String title;
    private LocalDateTime createdAt;
    private List<ExchangeParticipantDTO> participants;

    /**
     * true si el usuario actual puede confirmar (status=ENDED_PENDING_CONFIRMATION y no ha confirmado aún)
     */
    private boolean canConfirm;

    /**
     * true si todos han confirmado y el intercambio está completado
     */
    private boolean allConfirmed;
}
