package com.speakBuddy.speackBuddy_backend.service;

import com.speakBuddy.speackBuddy_backend.dto.JoinRequestResponseDTO;
import com.speakBuddy.speackBuddy_backend.exception.ResourceNotFoundException;
import com.speakBuddy.speackBuddy_backend.models.*;
import com.speakBuddy.speackBuddy_backend.repository.ExchangeJoinRequestRepository;
import com.speakBuddy.speackBuddy_backend.repository.ExchangeParticipantRepository;
import com.speakBuddy.speackBuddy_backend.repository.ExchangeRepository;
import com.speakBuddy.speackBuddy_backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestión de solicitudes de unión a intercambios públicos.
 */
@Service
public class ExchangeJoinRequestService {

    private final ExchangeRepository exchangeRepository;
    private final ExchangeParticipantRepository participantRepository;
    private final ExchangeJoinRequestRepository joinRequestRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final ExchangeEligibilityService eligibilityService;
    private final ExchangeMapper exchangeMapper;

    public ExchangeJoinRequestService(ExchangeRepository exchangeRepository,
                                      ExchangeParticipantRepository participantRepository,
                                      ExchangeJoinRequestRepository joinRequestRepository,
                                      UserRepository userRepository,
                                      NotificationService notificationService,
                                      ExchangeEligibilityService eligibilityService,
                                      ExchangeMapper exchangeMapper) {
        this.exchangeRepository = exchangeRepository;
        this.participantRepository = participantRepository;
        this.joinRequestRepository = joinRequestRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.eligibilityService = eligibilityService;
        this.exchangeMapper = exchangeMapper;
    }

    @Transactional
    public void requestToJoin(Long exchangeId, Long userId) {
        Exchange exchange = exchangeRepository.findById(exchangeId)
                .orElseThrow(() -> new ResourceNotFoundException("Intercambio no encontrado"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (!Boolean.TRUE.equals(exchange.getIsPublic())) {
            throw new IllegalArgumentException("Este intercambio no es público");
        }
        if (exchange.getStatus() != ExchangeStatus.SCHEDULED) {
            throw new IllegalArgumentException("No se pueden unir intercambios que ya han terminado o están cancelados");
        }
        if (participantRepository.existsByExchangeAndUser(exchange, user)) {
            throw new IllegalArgumentException("Ya eres participante de este intercambio");
        }

        int currentCount = participantRepository.findByExchange(exchange).size();
        if (exchange.getMaxParticipants() != null && currentCount >= exchange.getMaxParticipants()) {
            throw new IllegalArgumentException("El intercambio está completo");
        }
        if (joinRequestRepository.existsByExchangeAndUser(exchange, user)) {
            if (joinRequestRepository.existsByExchangeAndUserAndStatus(exchange, user, ExchangeJoinRequestStatus.PENDING)) {
                throw new IllegalArgumentException("Ya has enviado una solicitud para este intercambio");
            }
            throw new IllegalArgumentException("Ya tienes una solicitud previa para este intercambio");
        }

        ExchangeJoinRequest request = ExchangeJoinRequest.builder()
                .exchange(exchange)
                .user(user)
                .status(ExchangeJoinRequestStatus.PENDING)
                .build();
        joinRequestRepository.save(request);

        User creator = exchangeMapper.findCreator(exchange);
        if (creator != null) {
            notificationService.createExchangeJoinRequestNotification(
                    creator, exchangeId, user,
                    exchange.getTitle() != null ? exchange.getTitle() : "Intercambio");
        }
    }

    public List<JoinRequestResponseDTO> getJoinRequests(Long exchangeId, Long currentUserId) {
        Exchange exchange = exchangeRepository.findById(exchangeId)
                .orElseThrow(() -> new ResourceNotFoundException("Intercambio no encontrado"));

        User creator = exchangeMapper.findCreator(exchange);
        if (creator == null || !creator.getId().equals(currentUserId)) {
            throw new IllegalArgumentException("Solo el creador puede ver las solicitudes de unión");
        }

        int minOrder = eligibilityService.effectiveMinOrder(exchange);
        int maxOrder = eligibilityService.effectiveMaxOrder(exchange);
        String nativeLanguageName = eligibilityService.resolveLanguageName(exchange.getNativeLanguageCode());
        String targetLanguageName = eligibilityService.resolveLanguageName(exchange.getTargetLanguageCode());

        List<ExchangeJoinRequest> requests = joinRequestRepository.findByExchangeIdAndStatus(
                exchangeId, ExchangeJoinRequestStatus.PENDING);
        List<JoinRequestResponseDTO> result = new ArrayList<>();
        for (ExchangeJoinRequest req : requests) {
            var eligibility = eligibilityService.computeEligibility(
                    exchange, req.getUser(), minOrder, maxOrder, targetLanguageName, nativeLanguageName);
            result.add(JoinRequestResponseDTO.builder()
                    .id(req.getId())
                    .userId(req.getUser().getId())
                    .username(req.getUser().getUsername())
                    .createdAt(req.getCreatedAt())
                    .unmetRequirements(eligibility.unmetRequirements())
                    .build());
        }
        return result;
    }

    @Transactional
    public void acceptJoinRequest(Long exchangeId, Long requestId, Long creatorUserId) {
        Exchange exchange = exchangeRepository.findById(exchangeId)
                .orElseThrow(() -> new ResourceNotFoundException("Intercambio no encontrado"));

        User creator = userRepository.findById(creatorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        User actualCreator = exchangeMapper.findCreator(exchange);
        if (actualCreator == null || !actualCreator.getId().equals(creatorUserId)) {
            throw new IllegalArgumentException("Solo el creador puede aceptar solicitudes");
        }

        ExchangeJoinRequest request = joinRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada"));

        if (!request.getExchange().getId().equals(exchangeId)) {
            throw new IllegalArgumentException("La solicitud no pertenece a este intercambio");
        }
        if (request.getStatus() != ExchangeJoinRequestStatus.PENDING) {
            throw new IllegalArgumentException("La solicitud ya fue respondida");
        }

        int currentCount = participantRepository.findByExchange(exchange).size();
        if (exchange.getMaxParticipants() != null && currentCount >= exchange.getMaxParticipants()) {
            throw new IllegalArgumentException("El intercambio está completo");
        }

        ExchangeParticipant participant = new ExchangeParticipant();
        participant.setExchange(exchange);
        participant.setUser(request.getUser());
        participant.setRole("participant");
        participantRepository.save(participant);

        request.setStatus(ExchangeJoinRequestStatus.ACCEPTED);
        request.setRespondedAt(LocalDateTime.now());
        request.setRespondedBy(creator);
        joinRequestRepository.save(request);

        notificationService.createJoinRequestResponseNotification(
                request.getUser(), exchangeId,
                exchange.getTitle() != null ? exchange.getTitle() : "Intercambio",
                true);
    }

    @Transactional
    public void rejectJoinRequest(Long exchangeId, Long requestId, Long creatorUserId) {
        Exchange exchange = exchangeRepository.findById(exchangeId)
                .orElseThrow(() -> new ResourceNotFoundException("Intercambio no encontrado"));

        User creator = userRepository.findById(creatorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        User actualCreator = exchangeMapper.findCreator(exchange);
        if (actualCreator == null || !actualCreator.getId().equals(creatorUserId)) {
            throw new IllegalArgumentException("Solo el creador puede rechazar solicitudes");
        }

        ExchangeJoinRequest request = joinRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada"));

        if (!request.getExchange().getId().equals(exchangeId)) {
            throw new IllegalArgumentException("La solicitud no pertenece a este intercambio");
        }
        if (request.getStatus() != ExchangeJoinRequestStatus.PENDING) {
            throw new IllegalArgumentException("La solicitud ya fue respondida");
        }

        request.setStatus(ExchangeJoinRequestStatus.REJECTED);
        request.setRespondedAt(LocalDateTime.now());
        request.setRespondedBy(creator);
        joinRequestRepository.save(request);

        notificationService.createJoinRequestResponseNotification(
                request.getUser(), exchangeId,
                exchange.getTitle() != null ? exchange.getTitle() : "Intercambio",
                false);
    }
}
