package com.speakBuddy.speackBuddy_backend.controller;

import com.speakBuddy.speackBuddy_backend.dto.RegisterRequestDTO;
import com.speakBuddy.speackBuddy_backend.dto.UserResponseDTO;
import com.speakBuddy.speackBuddy_backend.models.User;
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

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
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
}
