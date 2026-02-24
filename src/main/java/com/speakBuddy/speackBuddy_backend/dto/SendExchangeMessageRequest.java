package com.speakBuddy.speackBuddy_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SendExchangeMessageRequest {

    @NotBlank(message = "El contenido del mensaje es obligatorio")
    @Size(max = 4000, message = "El mensaje no puede superar los 4000 caracteres")
    private String content;
}
