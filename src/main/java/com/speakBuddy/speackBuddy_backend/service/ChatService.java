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
    private final ChatMessageMapper chatMessageMapper;

    public ChatService(ChatMessageRepository chatMessageRepository,
                       UserRepository userRepository,
                       NotificationService notificationService,
                       ChatMessageMapper chatMessageMapper) {
        this.chatMessageRepository = chatMessageRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.chatMessageMapper = chatMessageMapper;
    }

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
        return chatMessageMapper.toChatMessageDTO(savedMessage);
    }

    public String getOrCreateChatId(Long myUserId, Long otherUserId) {
        userRepository.findById(myUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + myUserId));
        userRepository.findById(otherUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + otherUserId));

        long min = Math.min(myUserId, otherUserId);
        long max = Math.max(myUserId, otherUserId);
        return "chat_" + min + "_" + max;
    }

    public List<DirectChatMessageResponseDTO> getMessagesByUserIds(Long myUserId, Long otherUserId) {
        validateParticipants(myUserId, otherUserId);
        return chatMessageRepository.findChatHistoryByUserIds(myUserId, otherUserId).stream()
                .map(chatMessageMapper::toDirectChatMessageDTO)
                .collect(Collectors.toList());
    }

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

        long min = Math.min(myUserId, otherUserId);
        long max = Math.max(myUserId, otherUserId);
        String chatId = "chat_" + min + "_" + max;
        notificationService.createDirectMessageNotification(recipient, sender, chatId, request.getContent());

        return chatMessageMapper.toDirectChatMessageDTO(saved);
    }

    private void validateParticipants(Long myUserId, Long otherUserId) {
        if (myUserId.equals(otherUserId)) {
            throw new IllegalArgumentException("No puedes chatear contigo mismo");
        }
    }
}
