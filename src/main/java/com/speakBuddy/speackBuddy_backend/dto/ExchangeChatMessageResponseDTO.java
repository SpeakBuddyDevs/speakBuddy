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
public class ExchangeChatMessageResponseDTO {

    private Long id;
    private String content;
    private Long senderId;
    private String senderName;
    private LocalDateTime timestamp;
}
