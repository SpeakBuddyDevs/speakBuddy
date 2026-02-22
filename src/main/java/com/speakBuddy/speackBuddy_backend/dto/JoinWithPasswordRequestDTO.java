package com.speakBuddy.speackBuddy_backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class JoinWithPasswordRequestDTO {
    @NotBlank(message = "La contraseña es obligatoria")
    private String password;
}
