package com.speakBuddy.speackBuddy_backend.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Esta clase recibe el mensaje, lo procesa
 * y lo envia al destinatario
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageDTO {

    private String content;
    private String sender;
    private String recipient; // El email o username de quien recibe (para chat 1 a 1)
    private MessageType type; // Tipo de mensaje (CHAT, UNIRSE, SALIR)
    private LocalDateTime timestamp;

    public enum MessageType {
        CHAT,
        JOIN,
        LEAVE
    }
}
