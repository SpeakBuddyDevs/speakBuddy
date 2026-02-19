package com.speakBuddy.speackBuddy_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para mensajes del chat 1:1 usuario-usuario.
 * Formato esperado por el frontend Flutter.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DirectChatMessageResponseDTO {

    private Long id;
    private String content;
    private Long senderId;
    private String senderName;
    private LocalDateTime timestamp;
}
