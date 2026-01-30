package com.speakBuddy.speackBuddy_backend.controller;

import com.speakBuddy.speackBuddy_backend.dto.*;
import com.speakBuddy.speackBuddy_backend.exception.ResourceNotFoundException;
import com.speakBuddy.speackBuddy_backend.models.User;
import com.speakBuddy.speackBuddy_backend.models.UserLanguagesLearning;
import com.speakBuddy.speackBuddy_backend.service.ReviewService;
import com.speakBuddy.speackBuddy_backend.service.StorageService;
import com.speakBuddy.speackBuddy_backend.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final ReviewService reviewService;
    private final StorageService storageService;


    @Autowired
    public UserController(UserService userService, ReviewService reviewService, StorageService storageService) {
        this.userService = userService;
        this.reviewService = reviewService;
        this.storageService = storageService;
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

    // Endpoint 3: Añadir un nuevo idioma que el usuario quiere aprender
    @PostMapping("/{id}/languages/learn")
    public ResponseEntity<ProfileResponseDTO> addLanguageToLearn(
            @PathVariable Long id,
            @RequestBody AddLearningLanguageDTO addDTO
    ) {
        ProfileResponseDTO updatedProfile = userService.addLearningLanguage(id, addDTO);

        return ResponseEntity.ok(updatedProfile);
    }

    // Endpoint 4: Eliminar un idioma que el usuario está aprendiendo
    @DeleteMapping("/{id}/languages/learn/{languageId}")
    public ResponseEntity<ProfileResponseDTO> deleteLearningLanguage(
            @PathVariable Long id,
            @PathVariable Long languageId
    ) {
        ProfileResponseDTO updatedProfile = userService.deleteLearningLanguage(id, languageId);

        return ResponseEntity.ok(updatedProfile);
    }

    // --- Endpoint 5: Actualizar el Idioma Nativo ---
    @PutMapping("/{id}/languages/native")
    public ResponseEntity<ProfileResponseDTO> updateNativeLanguage(
            @PathVariable Long id,
            @RequestBody UpdateNativeLanguageDTO dto
    ) {
        ProfileResponseDTO updatedProfile = userService.updateNativeLanguage(id, dto);

        return ResponseEntity.ok(updatedProfile);
    }

    // --- Endpoint 6: Actualizar Nivel de Idioma de Aprendizaje ---
    @PutMapping("/{id}/languages/learn/{learningId}")
    public ResponseEntity<ProfileResponseDTO> updateLearningLevel(
            @PathVariable Long id,        // El ID del Usuario
            @PathVariable Long learningId, // El ID de la *relación* a cambiar
            @RequestBody UpdateLearningLevelDTO dto
    ) {
        ProfileResponseDTO updatedProfile = userService.updateLearningLevel(id, learningId, dto);

        return ResponseEntity.ok(updatedProfile);
    }

    /**
     * Endpoint para eliminar la cuenta del usuario autenticado
     * Lo que hace es coger el email del contexto de seguridad(Token JWT)
     * @param userDetails
     * @return elimina el usuario
     */
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMyAccount(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        userService.deleteUserByEmail(email);
        return ResponseEntity.noContent().build();
    }

    // --- HU 2.1: Buscador ---
    // GET /api/users/search?nativeLang=es&learningLang=en&page=0&size=10
    @GetMapping("/search")
    public ResponseEntity<Page<UserSummaryDTO>> searchUsers(
            @RequestParam(required = false) String nativeLang,
            @RequestParam(required = false) String learningLang,
            Pageable pageable
    ) {
        Page<UserSummaryDTO> results = userService.searchUsers(nativeLang, learningLang, pageable);
        return ResponseEntity.ok(results);
    }

    // --- HU 2.4: Crear Valoración ---
    @PostMapping("/{id}/reviews")
    public ResponseEntity<Void> addReview(
            @PathVariable Long id, // El ID del usuario al que valoramos (reviewee)
            @RequestBody ReviewRequestDTO reviewDTO,
            @AuthenticationPrincipal UserDetails userDetails // El usuario logueado (reviewer)
    ) {

        Optional<User> reviewer = userService.getUserByEmail(userDetails.getUsername()); // *Necesitas crear este método helper en UserService si no lo tienes público*

        reviewer.ifPresent(user -> reviewService.createOrUpdateReview(user.getId(), id, reviewDTO));

        return ResponseEntity.ok().build();
    }

    // --- HU Extra: Obtener Mi Perfil ---
    @GetMapping("/me")
    public ResponseEntity<UserProfileDTO> getMyProfile(@AuthenticationPrincipal UserDetails userDetails) {

        User user = userService.getUserByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        UserProfileDTO response = userService.mapToUserProfileDTO(user);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{userId}/languages/{code}/active")
    public ResponseEntity<Void> setLanguageActive(
            @PathVariable Long userId,
            @PathVariable String code
    ) {
        userService.setLearningLanguageActive(userId, code);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{userId}/languages/{code}/inactive")
    public ResponseEntity<Void> setLanguageInactive(
            @PathVariable Long userId,
            @PathVariable String code
    ) {
        userService.setLearningLanguageInactive(userId, code);

        return ResponseEntity.ok().build();
    }

    // --- Eliminar idioma de aprendizaje por código ---
    @DeleteMapping("/{userId}/languages/learn/by-code/{languageCode}")
    public ResponseEntity<Void> deleteLearningLanguageByCode(
            @PathVariable Long userId,
            @PathVariable String languageCode
    ) {
        userService.deleteLearningLanguageByCode(userId, languageCode);
        return ResponseEntity.ok().build();
    }

    // --- Actualizar nivel de idioma por código ---
    @PutMapping("/{userId}/languages/learn/by-code/{languageCode}")
    public ResponseEntity<Void> updateLearningLevelByCode(
            @PathVariable Long userId,
            @PathVariable String languageCode,
            @RequestBody UpdateLearningLevelDTO dto
    ) {
        userService.updateLearningLevelByCode(userId, languageCode, dto.getNewLevelId());
        return ResponseEntity.ok().build();
    }

    // --- Subir foto de perfil ---
    @PostMapping("/{id}/profile/picture")
    public ResponseEntity<Map<String, String>> uploadProfilePicture(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file
    ) {
        try {
            // Subir archivo al storage
            String imageUrl = storageService.upload(file, "avatars");

            // Actualizar URL en el perfil del usuario
            userService.updateProfilePicture(id, imageUrl);

            return ResponseEntity.ok(Map.of("url", imageUrl));
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error al subir la imagen: " + e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
