package com.speakBuddy.speackBuddy_backend.service;

import com.speakBuddy.speackBuddy_backend.dto.*;
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
    private final NotificationService notificationService;

    public ChatService(ChatMessageRepository chatMessageRepository,
                       UserRepository userRepository,
                       NotificationService notificationService) {
        this.chatMessageRepository = chatMessageRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
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

    // --- Chat 1:1 por userId (para Flutter) ---

    /**
     * Genera el chatId determinista para dos usuarios.
     * Formato: chat_{minUserId}_{maxUserId}
     */
    public String getOrCreateChatId(Long myUserId, Long otherUserId) {
        User me = userRepository.findById(myUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + myUserId));
        User other = userRepository.findById(otherUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + otherUserId));

        long min = Math.min(myUserId, otherUserId);
        long max = Math.max(myUserId, otherUserId);
        return "chat_" + min + "_" + max;
    }

    /**
     * Obtiene el historial de chat entre dos usuarios por sus IDs.
     */
    public List<DirectChatMessageResponseDTO> getMessagesByUserIds(Long myUserId, Long otherUserId) {
        validateParticipants(myUserId, otherUserId);
        List<ChatMessage> messages = chatMessageRepository.findChatHistoryByUserIds(myUserId, otherUserId);

        return messages.stream()
                .map(this::toDirectChatMessageDTO)
                .collect(Collectors.toList());
    }

    /**
     * Envía un mensaje en el chat 1:1 entre dos usuarios.
     */
    @Transactional
    public DirectChatMessageResponseDTO sendMessageByUserIds(Long myUserId, Long otherUserId, SendDirectMessageRequest request) {
        validateParticipants(myUserId, otherUserId);

        User sender = userRepository.findById(myUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + myUserId));
        User recipient = userRepository.findById(otherUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + otherUserId));

        ChatMessage message = ChatMessage.builder()
                .sender(sender)
                .recipient(recipient)
                .content(request.getContent())
                .build();

        ChatMessage saved = chatMessageRepository.save(message);

        // Crear notificación para el destinatario
        long min = Math.min(myUserId, otherUserId);
        long max = Math.max(myUserId, otherUserId);
        String chatId = "chat_" + min + "_" + max;
        notificationService.createDirectMessageNotification(recipient, sender, chatId, request.getContent());

        return toDirectChatMessageDTO(saved);
    }

    /**
     * Valida que el usuario autenticado sea participante del chat.
     */
    private void validateParticipants(Long myUserId, Long otherUserId) {
        if (myUserId.equals(otherUserId)) {
            throw new IllegalArgumentException("No puedes chatear contigo mismo");
        }
    }

    private DirectChatMessageResponseDTO toDirectChatMessageDTO(ChatMessage msg) {
        return DirectChatMessageResponseDTO.builder()
                .id(msg.getId())
                .content(msg.getContent())
                .senderId(msg.getSender().getId())
                .senderName(msg.getSender().getName() + " " + msg.getSender().getSurname())
                .timestamp(msg.getTimestamp())
                .build();
    }
}