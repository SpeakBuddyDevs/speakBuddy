package com.speakBuddy.speackBuddy_backend.service;

import com.speakBuddy.speackBuddy_backend.dto.NotificationResponseDTO;
import com.speakBuddy.speackBuddy_backend.models.Notification;
import org.springframework.stereotype.Component;

/**
 * Mapper centralizado para notificaciones.
 */
@Component
public class NotificationMapper {

    public NotificationResponseDTO toDto(Notification n) {
        return NotificationResponseDTO.builder()
                .id(n.getId())
                .type(n.getType())
                .title(n.getTitle())
                .body(n.getBody())
                .chatId(n.getChatId())
                .exchangeId(n.getExchangeId())
                .requesterUserId(n.getRequester() != null ? n.getRequester().getId() : null)
                .read(n.getRead())
                .createdAt(n.getCreatedAt())
                .build();
    }

    public String truncate(String s, int maxLen) {
        if (s == null) return "";
        if (s.length() <= maxLen) return s;
        return s.substring(0, maxLen) + "...";
    }
}
