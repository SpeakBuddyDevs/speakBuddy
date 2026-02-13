package com.speakBuddy.speackBuddy_backend.dto;

import lombok.Data;

@Data
public class ExchangeParticipantDTO {
    private Long userId;
    private String username;
    private boolean confirmed;
    private String role; // creator, participant
}
