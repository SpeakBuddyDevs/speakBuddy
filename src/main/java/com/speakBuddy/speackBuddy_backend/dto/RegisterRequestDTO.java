package com.speakBuddy.speackBuddy_backend.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequestDTO {

    private String email;
    private String password;
    private String username;
    private String surname;
    private Long nativeLanguageId;

}
