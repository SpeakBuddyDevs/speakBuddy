package com.speakBuddy.speackBuddy_backend.controller;

import com.speakBuddy.speackBuddy_backend.dto.SendDirectMessageRequest;
import com.speakBuddy.speackBuddy_backend.dto.WebSocketChatMessageDTO;
import com.speakBuddy.speackBuddy_backend.exception.ResourceNotFoundException;
import com.speakBuddy.speackBuddy_backend.service.ChatService;
import com.speakBuddy.speackBuddy_backend.service.UserService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;

/**
 * Controlador WebSocket para chat 1:1 en tiempo real.
 * Recibe mensajes en /app/chat y delega en ChatService (que guarda y broadcast al destinatario).
 */
@Controller
public class ChatController {

    private final ChatService chatService;
    private final UserService userService;

    public ChatController(ChatService chatService, UserService userService) {
        this.chatService = chatService;
        this.userService = userService;
    }

    @MessageMapping("/chat")
    public void processMessage(@Payload WebSocketChatMessageDTO chatMessage, Principal principal) {
        if (chatMessage == null || chatMessage.getContent() == null || chatMessage.getRecipientId() == null) {
            return;
        }
        String senderEmail = principal.getName();
        Long senderId = userService.getUserByEmail(senderEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"))
                .getId();
        Long recipientId = chatMessage.getRecipientId();
        SendDirectMessageRequest request = new SendDirectMessageRequest();
        request.setContent(chatMessage.getContent());
        chatService.sendMessageByUserIds(senderId, recipientId, request);
    }
}
