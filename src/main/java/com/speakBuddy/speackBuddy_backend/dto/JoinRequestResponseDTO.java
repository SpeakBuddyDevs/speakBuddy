package com.speakBuddy.speackBuddy_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JoinRequestResponseDTO {

    private Long id;
    private Long userId;
    private String username;
    private LocalDateTime createdAt;
    /** Requisitos no cumplidos por el solicitante (opcional) */
    private List<String> unmetRequirements;
}
