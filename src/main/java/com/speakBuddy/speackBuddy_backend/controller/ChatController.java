package com.speakBuddy.speackBuddy_backend.controller;

import com.speakBuddy.speackBuddy_backend.dto.ChatMessageDTO;
import com.speakBuddy.speackBuddy_backend.service.ChatService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

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

        if (chatMessage.getType() == ChatMessageDTO.MessageType.CHAT){
            //Solo se guarda en la base de datos si es un mensaje de texto
            ChatMessageDTO savedMsg = chatService.saveMessage(chatMessage);
            messagingTemplate.convertAndSendToUser(
                    savedMsg.getRecipient(),
                    "/queue/messages",
                    savedMsg
            );
        } else {
            //Si entra aqui significa que s un mensaje de WEBRTC y se envia directamente al destinatario
            messagingTemplate.convertAndSendToUser(
                    chatMessage.getRecipient(),
                    "/queue/messages",
                    chatMessage
            );
        }

    }

}
