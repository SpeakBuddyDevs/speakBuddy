package com.speakBuddy.speackBuddy_backend.service;

import com.speakBuddy.speackBuddy_backend.dto.CreateExchangeRequestDTO;
import com.speakBuddy.speackBuddy_backend.dto.ExchangeParticipantDTO;
import com.speakBuddy.speackBuddy_backend.dto.ExchangeResponseDTO;
import com.speakBuddy.speackBuddy_backend.dto.PublicExchangeResponseDTO;
import com.speakBuddy.speackBuddy_backend.exception.ResourceNotFoundException;
import com.speakBuddy.speackBuddy_backend.models.*;
import com.speakBuddy.speackBuddy_backend.repository.ExchangeParticipantRepository;
import com.speakBuddy.speackBuddy_backend.repository.ExchangeRepository;
import com.speakBuddy.speackBuddy_backend.repository.LanguageRepository;
import com.speakBuddy.speackBuddy_backend.repository.UserRepository;
import com.speakBuddy.speackBuddy_backend.repository.specifications.ExchangeSpecification;
import com.speakBuddy.speackBuddy_backend.security.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ExchangeService {

    private static final int MIN_LEVEL_PRINCIPIANTE = 1;
    private static final int MIN_LEVEL_INTERMEDIO = 4;
    private static final int MIN_LEVEL_AVANZADO = 7;

    private final ExchangeRepository exchangeRepository;
    private final ExchangeParticipantRepository participantRepository;
    private final UserRepository userRepository;
    private final LanguageRepository languageRepository;

    public ExchangeService(ExchangeRepository exchangeRepository,
                           ExchangeParticipantRepository participantRepository,
                           UserRepository userRepository,
                           LanguageRepository languageRepository) {
        this.exchangeRepository = exchangeRepository;
        this.participantRepository = participantRepository;
        this.userRepository = userRepository;
        this.languageRepository = languageRepository;
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
        // Campos para intercambios públicos
        if (dto.getIsPublic() != null) {
            exchange.setIsPublic(dto.getIsPublic());
        }
        exchange.setMaxParticipants(dto.getMaxParticipants());
        exchange.setDescription(dto.getDescription());
        exchange.setNativeLanguageCode(dto.getNativeLanguageCode());
        exchange.setTargetLanguageCode(dto.getTargetLanguageCode());
        exchange.setRequiredLevel(dto.getRequiredLevel());
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
    public ExchangeResponseDTO join(Long exchangeId, Long userId) {
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

        ExchangeParticipant participant = new ExchangeParticipant();
        participant.setExchange(exchange);
        participant.setUser(user);
        participant.setRole("participant");
        participantRepository.save(participant);

        return toResponseDTO(exchange, userId);
    }

    @Transactional
    public void leave(Long exchangeId, Long userId) {
        Exchange exchange = exchangeRepository.findById(exchangeId)
                .orElseThrow(() -> new ResourceNotFoundException("Intercambio no encontrado"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        ExchangeParticipant participant = participantRepository.findByExchangeAndUser(exchange, user)
                .orElseThrow(() -> new ResourceNotFoundException("No eres participante de este intercambio"));

        if ("creator".equals(participant.getRole())) {
            throw new IllegalArgumentException("El creador no puede abandonar el intercambio");
        }

        if (exchange.getStatus() != ExchangeStatus.SCHEDULED) {
            throw new IllegalArgumentException("No se puede abandonar un intercambio que ya ha terminado o está cancelado");
        }

        participantRepository.delete(participant);
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

    /**
     * Busca intercambios públicos con filtros y paginación.
     */
    public Page<PublicExchangeResponseDTO> searchPublicExchanges(
            Long currentUserId,
            String q,
            int page,
            int pageSize,
            String requiredLevel,
            LocalDateTime minDate,
            Integer maxDuration,
            String nativeLang,
            String targetLang) {

        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("scheduledAt").ascending());
        var spec = ExchangeSpecification.publicExchangesWithFilters(
                q, requiredLevel, minDate, maxDuration, nativeLang, targetLang);

        Page<Exchange> exchangesPage = exchangeRepository.findAll(spec, pageable);
        User currentUser = currentUserId != null
                ? userRepository.findById(currentUserId).orElse(null)
                : null;

        return exchangesPage.map(ex -> toPublicExchangeResponseDTO(ex, currentUser));
    }

    private PublicExchangeResponseDTO toPublicExchangeResponseDTO(Exchange exchange, User currentUser) {
        User creator = findCreator(exchange);
        var participants = participantRepository.findByExchange(exchange);
        int currentParticipants = participants.size();
        int minLevel = requiredLevelToMinLevel(exchange.getRequiredLevel());

        boolean isJoined = currentUser != null && participants.stream()
                .anyMatch(p -> p.getUser().getId().equals(currentUser.getId()));

        String nativeLanguageName = resolveLanguageName(exchange.getNativeLanguageCode());
        String targetLanguageName = resolveLanguageName(exchange.getTargetLanguageCode());

        var eligibility = computeEligibility(exchange, currentUser, minLevel, targetLanguageName, nativeLanguageName);

        return PublicExchangeResponseDTO.builder()
                .id(exchange.getId())
                .title(exchange.getTitle() != null ? exchange.getTitle() : "Intercambio")
                .description(exchange.getDescription())
                .creatorId(creator != null ? creator.getId() : null)
                .creatorName(creator != null ? creator.getUsername() : null)
                .creatorAvatarUrl(creator != null ? creator.getProfilePicture() : null)
                .creatorIsPro(creator != null && creator.getRole() == Role.ROLE_PREMIUM)
                .requiredLevel(exchange.getRequiredLevel())
                .minLevel(minLevel)
                .scheduledAt(exchange.getScheduledAt())
                .durationMinutes(exchange.getDurationMinutes())
                .currentParticipants(currentParticipants)
                .maxParticipants(exchange.getMaxParticipants())
                .nativeLanguage(nativeLanguageName)
                .targetLanguage(targetLanguageName)
                .topics(null)
                .isEligible(eligibility.isEligible())
                .unmetRequirements(eligibility.unmetRequirements())
                .isJoined(isJoined)
                .isPublic(Boolean.TRUE.equals(exchange.getIsPublic()))
                .shareLink(null)
                .build();
    }

    private User findCreator(Exchange exchange) {
        return participantRepository.findByExchange(exchange).stream()
                .filter(p -> "creator".equals(p.getRole()))
                .map(ExchangeParticipant::getUser)
                .findFirst()
                .orElse(null);
    }

    private int requiredLevelToMinLevel(String requiredLevel) {
        if (requiredLevel == null) return MIN_LEVEL_PRINCIPIANTE;
        return switch (requiredLevel.trim().toLowerCase()) {
            case "intermedio" -> MIN_LEVEL_INTERMEDIO;
            case "avanzado" -> MIN_LEVEL_AVANZADO;
            default -> MIN_LEVEL_PRINCIPIANTE;
        };
    }

    private String resolveLanguageName(String isoCode) {
        if (isoCode == null || isoCode.isBlank()) return null;
        return languageRepository.findByIsoCode(isoCode.trim())
                .map(Language::getName)
                .orElse(isoCode);
    }

    private record EligibilityResult(boolean isEligible, List<String> unmetRequirements) {}

    private EligibilityResult computeEligibility(Exchange exchange, User currentUser,
                                                 int minLevel, String targetLanguageName, String nativeLanguageName) {
        if (currentUser == null) {
            return new EligibilityResult(false, List.of("Inicia sesión para unirte"));
        }

        List<String> unmet = new ArrayList<>();

        String userNativeIso = currentUser.getNativeLanguage() != null
                ? currentUser.getNativeLanguage().getIsoCode()
                : null;
        String targetIso = exchange.getTargetLanguageCode() != null
                ? exchange.getTargetLanguageCode().trim().toLowerCase()
                : null;

        if (targetIso != null && (userNativeIso == null || !userNativeIso.trim().equalsIgnoreCase(targetIso))) {
            unmet.add("Idioma nativo: " + (targetLanguageName != null ? targetLanguageName : targetIso));
        }

        String nativeIso = exchange.getNativeLanguageCode() != null
                ? exchange.getNativeLanguageCode().trim().toLowerCase()
                : null;
        if (nativeIso != null) {
            Optional<UserLanguagesLearning> learning = currentUser.getLanguagesToLearn().stream()
                    .filter(l -> l.getLanguage() != null
                            && nativeIso.equals(l.getLanguage().getIsoCode().toLowerCase()))
                    .findFirst();
            if (learning.isEmpty()) {
                unmet.add("Nivel de " + (nativeLanguageName != null ? nativeLanguageName : nativeIso) + ": "
                        + (exchange.getRequiredLevel() != null ? exchange.getRequiredLevel() : "Principiante"));
            } else {
                int userLevelOrder = learning.get().getLevel() != null
                        ? learning.get().getLevel().getLevelOrder()
                        : 0;
                if (userLevelOrder < minLevel) {
                    unmet.add("Nivel de " + (nativeLanguageName != null ? nativeLanguageName : nativeIso) + ": "
                            + (exchange.getRequiredLevel() != null ? exchange.getRequiredLevel() : "Principiante"));
                }
            }
        }

        return new EligibilityResult(unmet.isEmpty(), unmet.isEmpty() ? null : unmet);
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
