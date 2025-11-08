package com.speakBuddy.speackBuddy_backend.dto;


import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ErrorResponseDTO {
    private int statusCode;
    private LocalDateTime timestamp;
    private String message;
    private String path;

    public ErrorResponseDTO(int statusCode, String message, String path) {
        this.statusCode = statusCode;
        this.message = message;
        this.path = path;
        this.timestamp = LocalDateTime.now();
    }
}
