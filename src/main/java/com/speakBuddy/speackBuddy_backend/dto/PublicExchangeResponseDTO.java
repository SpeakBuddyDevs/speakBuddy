package com.speakBuddy.speackBuddy_backend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para el listado de intercambios públicos (GET /api/exchanges/public).
 * Incluye información del creador y si el usuario actual cumple los requisitos.
 */
@Data
@Builder
public class PublicExchangeResponseDTO {

    private Long id;
    private String title;
    private String description;

    /** ID del usuario creador del intercambio */
    private Long creatorId;
    private String creatorName;
    private String creatorAvatarUrl;
    /** Si el creador tiene suscripción PRO */
    private Boolean creatorIsPro;

    private String requiredLevel;
    /** Nivel numérico mínimo (1-10) para el idioma objetivo */
    private Integer minLevel;

    private LocalDateTime scheduledAt;
    private Integer durationMinutes;
    /** Número actual de participantes */
    private Integer currentParticipants;
    private Integer maxParticipants;

    /** Nombre del idioma nativo que el creador ofrece (ej: "Español") */
    private String nativeLanguage;
    /** Nombre del idioma que el creador quiere practicar (ej: "Inglés") */
    private String targetLanguage;

    private List<String> topics;

    /** Si el usuario actual cumple los requisitos para unirse */
    private Boolean isEligible;
    /** Requisitos no cumplidos cuando !isEligible */
    private List<String> unmetRequirements;

    /** Si el usuario actual ya es participante del intercambio */
    private Boolean isJoined;

    private Boolean isPublic;
    private String shareLink;
}
