package com.speakBuddy.speackBuddy_backend.service;

import com.speakBuddy.speackBuddy_backend.dto.ExchangeParticipantDTO;
import com.speakBuddy.speackBuddy_backend.dto.ExchangeResponseDTO;
import com.speakBuddy.speackBuddy_backend.dto.PublicExchangeResponseDTO;
import com.speakBuddy.speackBuddy_backend.models.*;
import com.speakBuddy.speackBuddy_backend.repository.ExchangeChatMessageRepository;
import com.speakBuddy.speackBuddy_backend.repository.ExchangeJoinRequestRepository;
import com.speakBuddy.speackBuddy_backend.repository.ExchangeParticipantRepository;
import com.speakBuddy.speackBuddy_backend.repository.UserRepository;
import com.speakBuddy.speackBuddy_backend.security.Role;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Construye DTOs de intercambio a partir de entidades de dominio.
 */
@Component
public class ExchangeMapper {

    private final ExchangeParticipantRepository participantRepository;
    private final ExchangeChatMessageRepository exchangeChatMessageRepository;
    private final ExchangeJoinRequestRepository joinRequestRepository;
    private final UserRepository userRepository;
    private final ExchangeEligibilityService eligibilityService;

    public ExchangeMapper(ExchangeParticipantRepository participantRepository,
                          ExchangeChatMessageRepository exchangeChatMessageRepository,
                          ExchangeJoinRequestRepository joinRequestRepository,
                          UserRepository userRepository,
                          ExchangeEligibilityService eligibilityService) {
        this.participantRepository = participantRepository;
        this.exchangeChatMessageRepository = exchangeChatMessageRepository;
        this.joinRequestRepository = joinRequestRepository;
        this.userRepository = userRepository;
        this.eligibilityService = eligibilityService;
    }

    public User findCreator(Exchange exchange) {
        return participantRepository.findByExchange(exchange).stream()
                .filter(p -> "creator".equals(p.getRole()))
                .map(ExchangeParticipant::getUser)
                .findFirst()
                .orElse(null);
    }

    public ExchangeResponseDTO toResponseDTO(Exchange exchange, Long currentUserId) {
        List<ExchangeParticipant> participants = participantRepository.findByExchange(exchange);
        List<ExchangeParticipantDTO> participantDTOs = participants.stream()
                .map(p -> {
                    User u = p.getUser();
                    ExchangeParticipantDTO dto = new ExchangeParticipantDTO();
                    dto.setUserId(u.getId());
                    dto.setUsername(u.getUsername());
                    dto.setConfirmed(p.isConfirmed());
                    dto.setRole(p.getRole());
                    dto.setAvatarUrl(u.getProfilePicture());
                    dto.setRating(u.getAverageRating());
                    dto.setCountry(u.getCountry());
                    dto.setIsPro(u.getRole() == Role.ROLE_PREMIUM);
                    return dto;
                })
                .collect(Collectors.toList());

        ExchangeParticipant currentUserParticipant = participants.stream()
                .filter(p -> p.getUser().getId().equals(currentUserId))
                .findFirst()
                .orElse(null);

        // Calcular si el intercambio ya terminó (basado en hora actual)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime = exchange.getScheduledAt().plusMinutes(exchange.getDurationMinutes());
        boolean hasEnded = !endTime.isAfter(now);

        // canConfirm es true si el intercambio ya terminó, no está cancelado/completado,
        // el usuario es participante y no ha confirmado aún
        boolean canConfirm = hasEnded
                && exchange.getStatus() != ExchangeStatus.COMPLETED
                && exchange.getStatus() != ExchangeStatus.CANCELLED
                && currentUserParticipant != null
                && !currentUserParticipant.isConfirmed();

        boolean allConfirmed = participants.stream().allMatch(ExchangeParticipant::isConfirmed);

        LocalDateTime lastMessageAt = exchangeChatMessageRepository
                .findFirstByExchangeOrderByTimestampDesc(exchange)
                .map(ExchangeChatMessage::getTimestamp)
                .orElse(null);

        String password = null;
        User creator = findCreator(exchange);
        User currentUser = userRepository.findById(currentUserId).orElse(null);
        if (Boolean.FALSE.equals(exchange.getIsPublic())
                && currentUser != null
                && creator != null
                && currentUser.getId().equals(creator.getId())) {
            password = exchange.getPassword();
        }

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
                .lastMessageAt(lastMessageAt)
                .password(password)
                .nativeLanguage(eligibilityService.resolveLanguageName(exchange.getNativeLanguageCode()))
                .targetLanguage(eligibilityService.resolveLanguageName(exchange.getTargetLanguageCode()))
                .platforms(exchange.getPlatforms() != null && !exchange.getPlatforms().isEmpty()
                        ? exchange.getPlatforms() : null)
                .maxParticipants(exchange.getMaxParticipants())
                .topics(exchange.getTopics() != null && !exchange.getTopics().isEmpty()
                        ? exchange.getTopics() : null)
                .build();
    }

    public PublicExchangeResponseDTO toPublicExchangeResponseDTO(Exchange exchange, User currentUser) {
        User creator = findCreator(exchange);
        var participants = participantRepository.findByExchange(exchange);
        int currentParticipants = participants.size();
        int minOrder = eligibilityService.effectiveMinOrder(exchange);
        int maxOrder = eligibilityService.effectiveMaxOrder(exchange);
        String requiredLevelLabel = eligibilityService.buildLevelRangeLabel(minOrder, maxOrder);

        boolean isJoined = currentUser != null && participants.stream()
                .anyMatch(p -> p.getUser().getId().equals(currentUser.getId()));

        boolean hasPendingJoinRequest = currentUser != null && !isJoined
                && joinRequestRepository.existsByExchangeAndUserAndStatus(
                        exchange, currentUser, ExchangeJoinRequestStatus.PENDING);

        String nativeLanguageName = eligibilityService.resolveLanguageName(exchange.getNativeLanguageCode());
        String targetLanguageName = eligibilityService.resolveLanguageName(exchange.getTargetLanguageCode());

        var eligibility = eligibilityService.computeEligibility(
                exchange, currentUser, minOrder, maxOrder, targetLanguageName, nativeLanguageName);

        String password = null;
        if (Boolean.FALSE.equals(exchange.getIsPublic())
                && currentUser != null
                && creator != null
                && currentUser.getId().equals(creator.getId())) {
            password = exchange.getPassword();
        }

        return PublicExchangeResponseDTO.builder()
                .id(exchange.getId())
                .title(exchange.getTitle() != null ? exchange.getTitle() : "Intercambio")
                .description(exchange.getDescription())
                .creatorId(creator != null ? creator.getId() : null)
                .creatorName(creator != null ? creator.getUsername() : null)
                .creatorAvatarUrl(creator != null ? creator.getProfilePicture() : null)
                .creatorIsPro(creator != null && creator.getRole() == Role.ROLE_PREMIUM)
                .requiredLevel(requiredLevelLabel)
                .requiredLevelMinOrder(minOrder)
                .requiredLevelMaxOrder(maxOrder)
                .minLevel(minOrder)
                .scheduledAt(exchange.getScheduledAt())
                .durationMinutes(exchange.getDurationMinutes())
                .currentParticipants(currentParticipants)
                .maxParticipants(exchange.getMaxParticipants())
                .nativeLanguage(nativeLanguageName)
                .targetLanguage(targetLanguageName)
                .topics(exchange.getTopics() != null && !exchange.getTopics().isEmpty()
                        ? exchange.getTopics() : null)
                .platforms(exchange.getPlatforms() != null && !exchange.getPlatforms().isEmpty()
                        ? exchange.getPlatforms() : null)
                .isEligible(eligibility.isEligible())
                .unmetRequirements(eligibility.unmetRequirements())
                .isJoined(isJoined)
                .hasPendingJoinRequest(hasPendingJoinRequest)
                .isPublic(Boolean.TRUE.equals(exchange.getIsPublic()))
                .password(password)
                .build();
    }
}
