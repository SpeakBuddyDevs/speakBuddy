package com.speakBuddy.speackBuddy_backend.controller;

import com.speakBuddy.speackBuddy_backend.dto.ChatMessageDTO;
import com.speakBuddy.speackBuddy_backend.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
public class ChatController {
    private final SimpMessagingTemplate  messagingTemplate;
    private final ChatService chatService;

    public ChatController(SimpMessagingTemplate messagingTemplate, ChatService chatService) {
        this.messagingTemplate = messagingTemplate;
        this.chatService = chatService;
    }

    @MessageMapping("/chat")
    public void processMessage(@Payload ChatMessageDTO chatMessage){
        ChatMessageDTO savedMsg = chatService.saveMessage(chatMessage);
        messagingTemplate.convertAndSendToUser(
                savedMsg.getRecipient(),
                "/queue/messages",
                savedMsg
        );
    }

    @GetMapping("/api/messages/{otherUserEmail}")
    public ResponseEntity<List<ChatMessageDTO>> getChatHistory(
            @PathVariable String otherUserEmail,
            @AuthenticationPrincipal UserDetails userDetails) {

        // Obtenemos el email del usuario autenticado
        String myEmail = userDetails.getUsername();

        // Pedimos al servicio la conversación entre dos usuarios
        List<ChatMessageDTO> history = chatService.getChatHistory(myEmail, otherUserEmail);

        return ResponseEntity.ok(history);
    }
}
