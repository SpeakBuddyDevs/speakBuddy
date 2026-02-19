package com.speakBuddy.speackBuddy_backend.controller;

import com.speakBuddy.speackBuddy_backend.dto.ChatIdResponseDTO;
import com.speakBuddy.speackBuddy_backend.dto.DirectChatMessageResponseDTO;
import com.speakBuddy.speackBuddy_backend.dto.SendDirectMessageRequest;
import com.speakBuddy.speackBuddy_backend.exception.ResourceNotFoundException;
import com.speakBuddy.speackBuddy_backend.models.User;
import com.speakBuddy.speackBuddy_backend.service.ChatService;
import com.speakBuddy.speackBuddy_backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller para chat 1:1 entre usuarios.
 * Usa userId para alinearse con Flutter.
 */
@RestController
@RequestMapping("/api/chats")
public class DirectChatController {

    private static final String CHAT_PREFIX = "chat_";

    private final ChatService chatService;
    private final UserService userService;

    public DirectChatController(ChatService chatService, UserService userService) {
        this.chatService = chatService;
        this.userService = userService;
    }

    private Long getCurrentUserId(UserDetails userDetails) {
        String email = userDetails.getUsername();
        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        return user.getId();
    }

    /**
     * GET /api/chats/with/{userId}
     * Devuelve el chatId para la conversación con el usuario indicado.
     */
    @GetMapping("/with/{userId}")
    public ResponseEntity<ChatIdResponseDTO> getOrCreateChatId(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long userId) {
        Long myUserId = getCurrentUserId(userDetails);
        String chatId = chatService.getOrCreateChatId(myUserId, userId);
        return ResponseEntity.ok(new ChatIdResponseDTO(chatId));
    }

    /**
     * GET /api/chats/{chatId}/messages
     * Lista los mensajes del chat.
     * chatId formato: chat_{minUserId}_{maxUserId}
     */
    @GetMapping("/{chatId}/messages")
    public ResponseEntity<List<DirectChatMessageResponseDTO>> getMessages(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String chatId) {
        Long myUserId = getCurrentUserId(userDetails);
        long[] ids = parseChatId(chatId);
        if (ids[0] != myUserId && ids[1] != myUserId) {
            throw new ResourceNotFoundException("No eres participante de este chat");
        }
        Long otherUserId = ids[0] == myUserId ? ids[1] : ids[0];
        List<DirectChatMessageResponseDTO> messages = chatService.getMessagesByUserIds(myUserId, otherUserId);
        return ResponseEntity.ok(messages);
    }

    /**
     * POST /api/chats/{chatId}/messages
     * Envía un mensaje en el chat.
     */
    @PostMapping("/{chatId}/messages")
    public ResponseEntity<DirectChatMessageResponseDTO> sendMessage(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String chatId,
            @Valid @RequestBody SendDirectMessageRequest request) {
        Long myUserId = getCurrentUserId(userDetails);
        long[] ids = parseChatId(chatId);
        if (ids[0] != myUserId && ids[1] != myUserId) {
            throw new ResourceNotFoundException("No eres participante de este chat");
        }
        Long otherUserId = ids[0] == myUserId ? ids[1] : ids[0];
        DirectChatMessageResponseDTO created = chatService.sendMessageByUserIds(myUserId, otherUserId, request);
        return ResponseEntity.ok(created);
    }

    private long[] parseChatId(String chatId) {
        if (chatId == null || !chatId.startsWith(CHAT_PREFIX)) {
            throw new IllegalArgumentException("chatId inválido: " + chatId);
        }
        String[] parts = chatId.substring(CHAT_PREFIX.length()).split("_");
        if (parts.length != 2) {
            throw new IllegalArgumentException("chatId inválido: " + chatId);
        }
        try {
            long id1 = Long.parseLong(parts[0]);
            long id2 = Long.parseLong(parts[1]);
            return new long[]{id1, id2};
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("chatId inválido: " + chatId);
        }
    }
}
