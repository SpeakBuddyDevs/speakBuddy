package com.speakBuddy.speackBuddy_backend.service;

import com.speakBuddy.speackBuddy_backend.dto.CreateExchangeRequestDTO;
import com.speakBuddy.speackBuddy_backend.dto.ExchangeParticipantDTO;
import com.speakBuddy.speackBuddy_backend.dto.ExchangeResponseDTO;
import com.speakBuddy.speackBuddy_backend.exception.ResourceNotFoundException;
import com.speakBuddy.speackBuddy_backend.models.*;
import com.speakBuddy.speackBuddy_backend.repository.ExchangeParticipantRepository;
import com.speakBuddy.speackBuddy_backend.repository.ExchangeRepository;
import com.speakBuddy.speackBuddy_backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExchangeService {

    private final ExchangeRepository exchangeRepository;
    private final ExchangeParticipantRepository participantRepository;
    private final UserRepository userRepository;

    public ExchangeService(ExchangeRepository exchangeRepository,
                           ExchangeParticipantRepository participantRepository,
                           UserRepository userRepository) {
        this.exchangeRepository = exchangeRepository;
        this.participantRepository = participantRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ExchangeResponseDTO create(Long creatorUserId, CreateExchangeRequestDTO dto) {
        User creator = userRepository.findById(creatorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Exchange exchange = new Exchange();
        exchange.setScheduledAt(dto.getScheduledAt());
        exchange.setDurationMinutes(dto.getDurationMinutes());
        exchange.setStatus(ExchangeStatus.SCHEDULED);
        exchange.setType("group");
        exchange.setTitle(dto.getTitle() != null ? dto.getTitle() : "Intercambio");
        exchange = exchangeRepository.save(exchange);

        ExchangeParticipant creatorParticipant = new ExchangeParticipant();
        creatorParticipant.setExchange(exchange);
        creatorParticipant.setUser(creator);
        creatorParticipant.setRole("creator");
        participantRepository.save(creatorParticipant);

        if (dto.getParticipantUserIds() != null) {
            for (Long userId : dto.getParticipantUserIds()) {
                if (userId.equals(creatorUserId)) continue;
                User user = userRepository.findById(userId).orElse(null);
                if (user != null && !participantRepository.existsByExchangeAndUser(exchange, user)) {
                    ExchangeParticipant p = new ExchangeParticipant();
                    p.setExchange(exchange);
                    p.setUser(user);
                    p.setRole("participant");
                    participantRepository.save(p);
                }
            }
        }

        return toResponseDTO(exchange, creatorUserId);
    }

    public List<ExchangeResponseDTO> getJoinedExchanges(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        List<ExchangeParticipant> myParticipations = participantRepository.findByUser(user);
        List<ExchangeResponseDTO> result = new ArrayList<>();
        for (ExchangeParticipant ep : myParticipations) {
            Exchange e = ep.getExchange();
            if (e.getStatus() != ExchangeStatus.CANCELLED) {
                result.add(toResponseDTO(e, userId));
            }
        }
        result.sort((a, b) -> a.getScheduledAt().compareTo(b.getScheduledAt()));
        return result;
    }

    public ExchangeResponseDTO getById(Long exchangeId, Long userId) {
        Exchange exchange = exchangeRepository.findById(exchangeId)
                .orElseThrow(() -> new ResourceNotFoundException("Intercambio no encontrado"));

        if (!participantRepository.existsByExchangeAndUser(exchange, userRepository.findById(userId).orElseThrow())) {
            throw new ResourceNotFoundException("No tienes acceso a este intercambio");
        }

        return toResponseDTO(exchange, userId);
    }

    @Transactional
    public ExchangeResponseDTO confirm(Long exchangeId, Long userId) {
        Exchange exchange = exchangeRepository.findById(exchangeId)
                .orElseThrow(() -> new ResourceNotFoundException("Intercambio no encontrado"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        ExchangeParticipant participant = participantRepository.findByExchangeAndUser(exchange, user)
                .orElseThrow(() -> new ResourceNotFoundException("No eres participante de este intercambio"));

        if (exchange.getStatus() != ExchangeStatus.ENDED_PENDING_CONFIRMATION) {
            throw new IllegalArgumentException("Este intercambio no está pendiente de confirmación");
        }

        if (participant.isConfirmed()) {
            return toResponseDTO(exchange, userId);
        }

        participant.setConfirmed(true);
        participant.setConfirmedAt(LocalDateTime.now());
        participantRepository.save(participant);

        boolean allConfirmed = participantRepository.findByExchange(exchange).stream()
                .allMatch(ExchangeParticipant::isConfirmed);

        if (allConfirmed) {
            exchange.setStatus(ExchangeStatus.COMPLETED);
            exchangeRepository.save(exchange);

            for (ExchangeParticipant p : participantRepository.findByExchange(exchange)) {
                User u = p.getUser();
                u.setCompletedExchanges((u.getCompletedExchanges() != null ? u.getCompletedExchanges() : 0) + 1);
                userRepository.save(u);
            }
        }

        return toResponseDTO(exchange, userId);
    }

    /**
     * Pasa intercambios SCHEDULED a ENDED_PENDING_CONFIRMATION cuando scheduled_at + duration ha pasado.
     * Invocado por un job programado.
     */
    @Transactional
    public int processEndedExchanges() {
        List<Exchange> scheduled = exchangeRepository.findByStatus(ExchangeStatus.SCHEDULED);
        LocalDateTime now = LocalDateTime.now();
        int updated = 0;
        for (Exchange e : scheduled) {
            LocalDateTime endTime = e.getScheduledAt().plusMinutes(e.getDurationMinutes());
            if (!endTime.isAfter(now)) {
                e.setStatus(ExchangeStatus.ENDED_PENDING_CONFIRMATION);
                exchangeRepository.save(e);
                // TODO: Enviar notificación push a cada participante (FCM)
                // "Confirma que el intercambio se realizó"
                updated++;
            }
        }
        return updated;
    }

    private ExchangeResponseDTO toResponseDTO(Exchange exchange, Long currentUserId) {
        List<ExchangeParticipant> participants = participantRepository.findByExchange(exchange);
        List<ExchangeParticipantDTO> participantDTOs = participants.stream()
                .map(p -> {
                    ExchangeParticipantDTO dto = new ExchangeParticipantDTO();
                    dto.setUserId(p.getUser().getId());
                    dto.setUsername(p.getUser().getUsername());
                    dto.setConfirmed(p.isConfirmed());
                    dto.setRole(p.getRole());
                    return dto;
                })
                .collect(Collectors.toList());

        ExchangeParticipant currentUserParticipant = participants.stream()
                .filter(p -> p.getUser().getId().equals(currentUserId))
                .findFirst()
                .orElse(null);

        boolean canConfirm = exchange.getStatus() == ExchangeStatus.ENDED_PENDING_CONFIRMATION
                && currentUserParticipant != null
                && !currentUserParticipant.isConfirmed();

        boolean allConfirmed = participants.stream().allMatch(ExchangeParticipant::isConfirmed);

        return ExchangeResponseDTO.builder()
                .id(exchange.getId())
                .scheduledAt(exchange.getScheduledAt())
                .durationMinutes(exchange.getDurationMinutes())
                .status(exchange.getStatus())
                .type(exchange.getType())
                .title(exchange.getTitle())
                .createdAt(exchange.getCreatedAt())
                .participants(participantDTOs)
                .canConfirm(canConfirm)
                .allConfirmed(allConfirmed)
                .build();
    }
}
