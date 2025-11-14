package com.speakBuddy.speackBuddy_backend.controller;

import com.speakBuddy.speackBuddy_backend.dto.JwtAuthResponseDTO;
import com.speakBuddy.speackBuddy_backend.dto.LoginRequestDTO;
import com.speakBuddy.speackBuddy_backend.dto.RegisterRequestDTO;
import com.speakBuddy.speackBuddy_backend.dto.UserResponseDTO;
import com.speakBuddy.speackBuddy_backend.models.User;
import com.speakBuddy.speackBuddy_backend.service.AuthService;
import com.speakBuddy.speackBuddy_backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthService authService;

    @Autowired
    public AuthController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> registerUser(@RequestBody RegisterRequestDTO request) {

        User newUser = userService.registerUser(request);

        UserResponseDTO response = new UserResponseDTO();
        response.setId(newUser.getId());
        response.setEmail(newUser.getEmail());
        response.setUsername(newUser.getUsername());
        response.setRole(newUser.getRole().name());
        response.setNativeLanguageId(newUser.getNativeLanguage().getId());

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }


    @PostMapping("/login")
    public ResponseEntity<JwtAuthResponseDTO> autenticateUser(@RequestBody LoginRequestDTO loginRequest) {

        String token = authService.login(loginRequest);

        JwtAuthResponseDTO response = new JwtAuthResponseDTO(token);

        return ResponseEntity.ok(response);
    }
}
