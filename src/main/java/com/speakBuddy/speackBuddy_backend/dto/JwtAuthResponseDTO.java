package com.speakBuddy.speackBuddy_backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JwtAuthResponseDTO {

    private String access_token;

    private String token_type = "Bearer";

    public JwtAuthResponseDTO(String access_token){
        this.access_token = access_token;
    }
}
