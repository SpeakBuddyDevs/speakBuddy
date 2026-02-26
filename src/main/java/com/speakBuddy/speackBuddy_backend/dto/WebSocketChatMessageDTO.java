package com.speakBuddy.speackBuddy_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO para mensajes de chat enviados por WebSocket.
 * Usa recipientId para alinearse con el frontend Flutter (que trabaja con userId).
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WebSocketChatMessageDTO {

    private String content;
    private Long recipientId; // userId del destinatario (chat 1:1)
}
