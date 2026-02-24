package com.speakBuddy.speackBuddy_backend.controller;

import com.speakBuddy.speackBuddy_backend.dto.CreateExchangeRequestDTO;
import com.speakBuddy.speackBuddy_backend.dto.ExchangeChatMessageResponseDTO;
import com.speakBuddy.speackBuddy_backend.dto.ExchangeResponseDTO;
import com.speakBuddy.speackBuddy_backend.dto.JoinWithPasswordRequestDTO;
import com.speakBuddy.speackBuddy_backend.dto.JoinRequestResponseDTO;
import com.speakBuddy.speackBuddy_backend.dto.PublicExchangeResponseDTO;
import com.speakBuddy.speackBuddy_backend.dto.SendExchangeMessageRequest;
import com.speakBuddy.speackBuddy_backend.exception.ResourceNotFoundException;
import com.speakBuddy.speackBuddy_backend.models.User;
import com.speakBuddy.speackBuddy_backend.service.ExchangeChatService;
import com.speakBuddy.speackBuddy_backend.service.ExchangeService;
import com.speakBuddy.speackBuddy_backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/exchanges")
public class ExchangeController {

    private final ExchangeService exchangeService;
    private final UserService userService;
    private final ExchangeChatService exchangeChatService;

    public ExchangeController(ExchangeService exchangeService, UserService userService, ExchangeChatService exchangeChatService) {
        this.exchangeService = exchangeService;
        this.userService = userService;
        this.exchangeChatService = exchangeChatService;
    }

    private Long getCurrentUserId(UserDetails userDetails) {
        String email = userDetails.getUsername();
        Optional<User> user = userService.getUserByEmail(email);
        return user.orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado")).getId();
    }

    private Long getCurrentUserIdOrNull(UserDetails userDetails) {
        if (userDetails == null) return null;
        return userService.getUserByEmail(userDetails.getUsername())
                .map(User::getId)
                .orElse(null);
    }

    @PostMapping
    public ResponseEntity<ExchangeResponseDTO> create(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateExchangeRequestDTO dto) {
        Long userId = getCurrentUserId(userDetails);
        ExchangeResponseDTO created = exchangeService.create(userId, dto);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/public")
    public ResponseEntity<Page<PublicExchangeResponseDTO>> getPublic(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String requiredLevel,
            @RequestParam(required = false) Integer requiredLevelOrder,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate minDate,
            @RequestParam(required = false) Integer maxDuration,
            @RequestParam(required = false) String nativeLang,
            @RequestParam(required = false) String targetLang) {
        Long userId = userDetails != null ? getCurrentUserIdOrNull(userDetails) : null;
        LocalDateTime minDateAsDateTime = minDate != null ? minDate.atStartOfDay() : null;
        Page<PublicExchangeResponseDTO> result = exchangeService.searchPublicExchanges(
                userId, q, page, pageSize, requiredLevel, requiredLevelOrder, minDateAsDateTime, maxDuration, nativeLang, targetLang);
        return ResponseEntity.ok(result);
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

    @PostMapping("/{id}/join")
    public ResponseEntity<ExchangeResponseDTO> join(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        Long userId = getCurrentUserId(userDetails);
        ExchangeResponseDTO result = exchangeService.join(id, userId);
        return ResponseEntity.ok(result);
    }

    /**
     * Unirse a un intercambio privado usando contraseña.
     */
    @PostMapping("/{id}/join-with-password")
    public ResponseEntity<ExchangeResponseDTO> joinWithPassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody JoinWithPasswordRequestDTO request) {
        Long userId = getCurrentUserId(userDetails);
        ExchangeResponseDTO result = exchangeService.joinWithPassword(id, request.getPassword(), userId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/join-request")
    public ResponseEntity<Void> requestToJoin(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        Long userId = getCurrentUserId(userDetails);
        exchangeService.requestToJoin(id, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/join-requests")
    public ResponseEntity<List<JoinRequestResponseDTO>> getJoinRequests(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        Long userId = getCurrentUserId(userDetails);
        List<JoinRequestResponseDTO> requests = exchangeService.getJoinRequests(id, userId);
        return ResponseEntity.ok(requests);
    }

    @PostMapping("/{id}/join-requests/{requestId}/accept")
    public ResponseEntity<Void> acceptJoinRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @PathVariable Long requestId) {
        Long userId = getCurrentUserId(userDetails);
        exchangeService.acceptJoinRequest(id, requestId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/join-requests/{requestId}/reject")
    public ResponseEntity<Void> rejectJoinRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @PathVariable Long requestId) {
        Long userId = getCurrentUserId(userDetails);
        exchangeService.rejectJoinRequest(id, requestId, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/leave")
    public ResponseEntity<Void> leave(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        Long userId = getCurrentUserId(userDetails);
        exchangeService.leave(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<ExchangeResponseDTO> confirm(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        Long userId = getCurrentUserId(userDetails);
        ExchangeResponseDTO updated = exchangeService.confirm(id, userId);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<List<ExchangeChatMessageResponseDTO>> getExchangeMessages(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        Long userId = getCurrentUserId(userDetails);
        List<ExchangeChatMessageResponseDTO> messages = exchangeChatService.getMessages(id, userId);
        return ResponseEntity.ok(messages);
    }

    @PostMapping("/{id}/messages")
    public ResponseEntity<ExchangeChatMessageResponseDTO> sendExchangeMessage(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody SendExchangeMessageRequest request) {
        Long userId = getCurrentUserId(userDetails);
        ExchangeChatMessageResponseDTO created = exchangeChatService.sendMessage(id, userId, request);
        return ResponseEntity.ok(created);
    }
}
