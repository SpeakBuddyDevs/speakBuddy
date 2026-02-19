package com.speakBuddy.speackBuddy_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatIdResponseDTO {

    /**
     * Identificador único del chat entre dos usuarios.
     * Formato: chat_{minUserId}_{maxUserId}
     */
    private String chatId;
}
