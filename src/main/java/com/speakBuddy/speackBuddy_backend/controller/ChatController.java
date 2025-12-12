package com.speakBuddy.speackBuddy_backend.controller;

import com.speakBuddy.speackBuddy_backend.dto.ChatMessageDTO;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {
    private final SimpMessagingTemplate  messagingTemplate;

    public ChatController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat")
    public void processMessage(@Payload ChatMessageDTO chatMessage){
        messagingTemplate.convertAndSendToUser(
                chatMessage.getRecipient(), //Username del destinatario
                "/queue/messages",
                chatMessage
        );
    }
}
