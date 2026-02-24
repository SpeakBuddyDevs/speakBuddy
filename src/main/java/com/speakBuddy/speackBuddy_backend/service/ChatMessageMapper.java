package com.speakBuddy.speackBuddy_backend.service;

import com.speakBuddy.speackBuddy_backend.dto.ChatMessageDTO;
import com.speakBuddy.speackBuddy_backend.dto.DirectChatMessageResponseDTO;
import com.speakBuddy.speackBuddy_backend.dto.ExchangeChatMessageResponseDTO;
import com.speakBuddy.speackBuddy_backend.models.ChatMessage;
import com.speakBuddy.speackBuddy_backend.models.ExchangeChatMessage;
import org.springframework.stereotype.Component;

/**
 * Mapper centralizado para mensajes de chat (1:1 y de intercambio).
 */
@Component
public class ChatMessageMapper {

    public ChatMessageDTO toChatMessageDTO(ChatMessage msg) {
        return new ChatMessageDTO(
                msg.getContent(),
                msg.getSender().getEmail(),
                msg.getRecipient().getEmail(),
                ChatMessageDTO.MessageType.CHAT,
                msg.getTimestamp()
        );
    }

    public DirectChatMessageResponseDTO toDirectChatMessageDTO(ChatMessage msg) {
        return DirectChatMessageResponseDTO.builder()
                .id(msg.getId())
                .content(msg.getContent())
                .senderId(msg.getSender().getId())
                .senderName(msg.getSender().getName() + " " + msg.getSender().getSurname())
                .timestamp(msg.getTimestamp())
                .build();
    }

    public ExchangeChatMessageResponseDTO toExchangeChatMessageDTO(ExchangeChatMessage msg) {
        return ExchangeChatMessageResponseDTO.builder()
                .id(msg.getId())
                .content(msg.getContent())
                .senderId(msg.getSender().getId())
                .senderName(msg.getSender().getUsername())
                .timestamp(msg.getTimestamp())
                .build();
    }
}
