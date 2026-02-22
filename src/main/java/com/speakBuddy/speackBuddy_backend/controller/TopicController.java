package com.speakBuddy.speackBuddy_backend.controller;

import com.speakBuddy.speackBuddy_backend.dto.GenerateTopicRequestDTO;
import com.speakBuddy.speackBuddy_backend.dto.GeneratedTopicResponseDTO;
import com.speakBuddy.speackBuddy_backend.dto.SaveFavoriteTopicRequestDTO;
import com.speakBuddy.speackBuddy_backend.exception.ResourceNotFoundException;
import com.speakBuddy.speackBuddy_backend.models.TopicCategory;
import com.speakBuddy.speackBuddy_backend.models.User;
import com.speakBuddy.speackBuddy_backend.service.TopicService;
import com.speakBuddy.speackBuddy_backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/topics")
public class TopicController {

    private final TopicService topicService;
    private final UserService userService;

    public TopicController(TopicService topicService, UserService userService) {
        this.topicService = topicService;
        this.userService = userService;
    }

    private User getCurrentUser(UserDetails userDetails) {
        String email = userDetails.getUsername();
        return userService.getUserByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    @PostMapping("/generate")
    public ResponseEntity<GeneratedTopicResponseDTO> generate(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody GenerateTopicRequestDTO request) {

        TopicCategory category;
        try {
            category = TopicCategory.valueOf(request.getCategory().toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }

        GeneratedTopicResponseDTO topic = topicService.generate(
                category,
                request.getLevel(),
                request.getLanguageCode()
        );

        return ResponseEntity.ok(topic);
    }

    @GetMapping("/favorites")
    public ResponseEntity<List<GeneratedTopicResponseDTO>> getFavorites(
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = getCurrentUser(userDetails);
        List<GeneratedTopicResponseDTO> favorites = topicService.getFavorites(user);
        return ResponseEntity.ok(favorites);
    }

    @PostMapping("/favorites")
    public ResponseEntity<GeneratedTopicResponseDTO> addToFavorites(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody SaveFavoriteTopicRequestDTO request) {

        User user = getCurrentUser(userDetails);
        GeneratedTopicResponseDTO saved = topicService.addToFavorites(user, request);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/favorites/{id}")
    public ResponseEntity<Void> removeFromFavorites(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        User user = getCurrentUser(userDetails);
        topicService.removeFromFavorites(user, id);
        return ResponseEntity.ok().build();
    }
}
