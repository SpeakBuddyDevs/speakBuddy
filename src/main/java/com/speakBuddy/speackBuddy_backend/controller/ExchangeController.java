package com.speakBuddy.speackBuddy_backend.controller;

import com.speakBuddy.speackBuddy_backend.dto.CreateExchangeRequestDTO;
import com.speakBuddy.speackBuddy_backend.dto.ExchangeResponseDTO;
import com.speakBuddy.speackBuddy_backend.exception.ResourceNotFoundException;
import com.speakBuddy.speackBuddy_backend.models.User;
import com.speakBuddy.speackBuddy_backend.service.ExchangeService;
import com.speakBuddy.speackBuddy_backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/exchanges")
public class ExchangeController {

    private final ExchangeService exchangeService;
    private final UserService userService;

    public ExchangeController(ExchangeService exchangeService, UserService userService) {
        this.exchangeService = exchangeService;
        this.userService = userService;
    }

    private Long getCurrentUserId(UserDetails userDetails) {
        String email = userDetails.getUsername();
        Optional<User> user = userService.getUserByEmail(email);
        return user.orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado")).getId();
    }

    @PostMapping
    public ResponseEntity<ExchangeResponseDTO> create(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateExchangeRequestDTO dto) {
        Long userId = getCurrentUserId(userDetails);
        ExchangeResponseDTO created = exchangeService.create(userId, dto);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/joined")
    public ResponseEntity<List<ExchangeResponseDTO>> getJoined(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getCurrentUserId(userDetails);
        List<ExchangeResponseDTO> exchanges = exchangeService.getJoinedExchanges(userId);
        return ResponseEntity.ok(exchanges);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExchangeResponseDTO> getById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        Long userId = getCurrentUserId(userDetails);
        ExchangeResponseDTO exchange = exchangeService.getById(id, userId);
        return ResponseEntity.ok(exchange);
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<ExchangeResponseDTO> confirm(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        Long userId = getCurrentUserId(userDetails);
        ExchangeResponseDTO updated = exchangeService.confirm(id, userId);
        return ResponseEntity.ok(updated);
    }
}
