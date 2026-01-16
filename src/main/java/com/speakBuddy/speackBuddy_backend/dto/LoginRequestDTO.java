package com.speakBuddy.speackBuddy_backend.dto;


import lombok.Getter;
import lombok.Setter;

/**
 * Esta clase recoge lo que el usuario
 * introduce para iniciar sesion
 */

@Getter
@Setter
public class LoginRequestDTO {

    private String email;
    private String password;
}
