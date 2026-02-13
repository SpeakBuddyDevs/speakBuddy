package com.speakBuddy.speackBuddy_backend.service;

import com.speakBuddy.speackBuddy_backend.dto.ExchangeChatMessageResponseDTO;
import com.speakBuddy.speackBuddy_backend.dto.SendExchangeMessageRequest;
import com.speakBuddy.speackBuddy_backend.exception.ResourceNotFoundException;
import com.speakBuddy.speackBuddy_backend.models.Exchange;
import com.speakBuddy.speackBuddy_backend.models.ExchangeChatMessage;
import com.speakBuddy.speackBuddy_backend.models.User;
import com.speakBuddy.speackBuddy_backend.repository.ExchangeChatMessageRepository;
import com.speakBuddy.speackBuddy_backend.repository.ExchangeParticipantRepository;
import com.speakBuddy.speackBuddy_backend.repository.ExchangeRepository;
import com.speakBuddy.speackBuddy_backend.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExchangeChatService {

    private final ExchangeRepository exchangeRepository;
    private final ExchangeParticipantRepository participantRepository;
    private final ExchangeChatMessageRepository messageRepository;
    private final UserRepository userRepository;

    public ExchangeChatService(ExchangeRepository exchangeRepository,
                               ExchangeParticipantRepository participantRepository,
                               ExchangeChatMessageRepository messageRepository,
                               UserRepository userRepository) {
        this.exchangeRepository = exchangeRepository;
        this.participantRepository = participantRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    /**
     * Verifica que el usuario es participante del intercambio.
     * Lanza ResourceNotFoundException si no existe el intercambio o el usuario no es participante.
     */
    private void ensureParticipant(Long exchangeId, Long userId) {
        Exchange exchange = exchangeRepository.findById(exchangeId)
                .orElseThrow(() -> new ResourceNotFoundException("Intercambio no encontrado"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        participantRepository.findByExchangeAndUser(exchange, user)
                .orElseThrow(() -> new ResourceNotFoundException("No eres participante de este intercambio"));
    }

    /**
     * Lista mensajes del chat del intercambio (solo participantes).
     */
    public List<ExchangeChatMessageResponseDTO> getMessages(Long exchangeId, Long userId) {
        ensureParticipant(exchangeId, userId);
        Exchange exchange = exchangeRepository.findById(exchangeId)
                .orElseThrow(() -> new ResourceNotFoundException("Intercambio no encontrado"));
        List<ExchangeChatMessage> messages = messageRepository.findByExchangeOrderByTimestampAsc(exchange);
        return messages.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Lista mensajes del chat del intercambio con paginación (solo participantes).
     */
    public Page<ExchangeChatMessageResponseDTO> getMessages(Long exchangeId, Long userId, Pageable pageable) {
        ensureParticipant(exchangeId, userId);
        Exchange exchange = exchangeRepository.findById(exchangeId)
                .orElseThrow(() -> new ResourceNotFoundException("Intercambio no encontrado"));
        return messageRepository.findByExchangeOrderByTimestampAsc(exchange, pageable)
                .map(this::toDto);
    }

    /**
     * Envía un mensaje al chat del intercambio (solo participantes).
     */
    @Transactional
    public ExchangeChatMessageResponseDTO sendMessage(Long exchangeId, Long userId, SendExchangeMessageRequest request) {
        ensureParticipant(exchangeId, userId);
        Exchange exchange = exchangeRepository.findById(exchangeId)
                .orElseThrow(() -> new ResourceNotFoundException("Intercambio no encontrado"));
        User sender = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        ExchangeChatMessage message = ExchangeChatMessage.builder()
                .exchange(exchange)
                .sender(sender)
                .content(request.getContent().trim())
                .build();
        message = messageRepository.save(message);
        return toDto(message);
    }

    private ExchangeChatMessageResponseDTO toDto(ExchangeChatMessage msg) {
        return ExchangeChatMessageResponseDTO.builder()
                .id(msg.getId())
                .content(msg.getContent())
                .senderId(msg.getSender().getId())
                .senderName(msg.getSender().getUsername())
                .timestamp(msg.getTimestamp())
                .build();
    }
}
