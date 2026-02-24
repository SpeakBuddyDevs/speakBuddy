package com.speakBuddy.speackBuddy_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponseDTO {

    private Long id;
    private String type;
    private String title;
    private String body;
    private String chatId;
    private Long exchangeId;
    /** ID del usuario solicitante cuando type=EXCHANGE_JOIN_REQUEST */
    private Long requesterUserId;
    private Boolean read;
    private LocalDateTime createdAt;
}
