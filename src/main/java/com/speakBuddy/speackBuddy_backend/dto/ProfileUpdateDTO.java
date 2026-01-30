package com.speakBuddy.speackBuddy_backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileUpdateDTO {

    // Campos que el usuario PUEDE cambiar
    private String name;
    private String surname;
    private String profilePictureUrl;
    private String description;
}
