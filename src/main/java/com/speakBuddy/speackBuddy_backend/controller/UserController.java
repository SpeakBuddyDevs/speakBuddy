package com.speakBuddy.speackBuddy_backend.controller;

import com.speakBuddy.speackBuddy_backend.dto.AddLearningLanguageDTO;
import com.speakBuddy.speackBuddy_backend.dto.ProfileResponseDTO;
import com.speakBuddy.speackBuddy_backend.dto.ProfileUpdateDTO;
import com.speakBuddy.speackBuddy_backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Endpoint 1: Obtener información del usuario
    @GetMapping("/{id}/profile")
    public ResponseEntity<ProfileResponseDTO> getProfileById(@PathVariable Long id) {
        ProfileResponseDTO profile = userService.getProfile(id);

        return ResponseEntity.ok(profile);
    }

    // Endpoint 2: Actualizar información del usuario
    @PutMapping("/{id}/profile")
    public ResponseEntity<ProfileResponseDTO> updateProfileById(
            @PathVariable Long id,
            @RequestBody ProfileUpdateDTO updateDTO
    ) {
        ProfileResponseDTO updatedProfile = userService.updateProfile(id, updateDTO);

        return ResponseEntity.ok(updatedProfile);
    }

    @PostMapping("/{id}/languages/learn")
    public ResponseEntity<ProfileResponseDTO> addLanguageToLearn(
            @PathVariable Long id,
            @RequestBody AddLearningLanguageDTO addDTO
    ) {
        ProfileResponseDTO updatedProfile = userService.addLearningLanguage(id, addDTO);

        return ResponseEntity.ok(updatedProfile);
    }
}
