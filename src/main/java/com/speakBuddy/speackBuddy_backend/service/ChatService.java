package com.speakBuddy.speackBuddy_backend.service;

import com.speakBuddy.speackBuddy_backend.dto.ChatMessageDTO;
import com.speakBuddy.speackBuddy_backend.exception.ResourceNotFoundException;
import com.speakBuddy.speackBuddy_backend.models.ChatMessage;
import com.speakBuddy.speackBuddy_backend.models.User;
import com.speakBuddy.speackBuddy_backend.repository.ChatMessageRepository;
import com.speakBuddy.speackBuddy_backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;

    public ChatService(ChatMessageRepository chatMessageRepository, UserRepository userRepository) {
        this.chatMessageRepository = chatMessageRepository;
        this.userRepository = userRepository;
    }

    /**
     * Guarda un mensaje en la base de datos.
     * Convierte el DTO (Strings) a Entidad (Users).
     */
    @Transactional
    public ChatMessageDTO saveMessage(ChatMessageDTO chatMessageDTO) {

        User sender = userRepository.findByEmail(chatMessageDTO.getSender())
                .orElseThrow(() -> new ResourceNotFoundException("Emisor no encontrado: " + chatMessageDTO.getSender()));

        User recipient = userRepository.findByEmail(chatMessageDTO.getRecipient())
                .orElseThrow(() -> new ResourceNotFoundException("Receptor no encontrado: " + chatMessageDTO.getRecipient()));

        ChatMessage message = ChatMessage.builder()
                .sender(sender)
                .recipient(recipient)
                .content(chatMessageDTO.getContent())
                .build();

        ChatMessage savedMessage = chatMessageRepository.save(message);

        // 5. Devolver DTO actualizado (con la fecha real de guardado)
        return new ChatMessageDTO(
                savedMessage.getContent(),
                savedMessage.getSender().getEmail(),
                savedMessage.getRecipient().getEmail(),
                ChatMessageDTO.MessageType.CHAT,
                savedMessage.getTimestamp()
        );
    }

    /**
     * Obtiene el historial de chat entre dos usuarios.
     */
    public List<ChatMessageDTO> getChatHistory(String email1, String email2) {
        List<ChatMessage> messages = chatMessageRepository.findChatHistory(email1, email2);

        // Convertir Entidades a DTOs para enviar al Frontend
        return messages.stream()
                .map(msg -> new ChatMessageDTO(
                        msg.getContent(),
                        msg.getSender().getEmail(),
                        msg.getRecipient().getEmail(),
                        ChatMessageDTO.MessageType.CHAT,
                        msg.getTimestamp()
                ))
                .collect(Collectors.toList());
    }
}