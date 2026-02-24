package com.speakBuddy.speackBuddy_backend.service;

import com.speakBuddy.speackBuddy_backend.dto.CreateExchangeRequestDTO;
import com.speakBuddy.speackBuddy_backend.dto.ExchangeResponseDTO;
import com.speakBuddy.speackBuddy_backend.dto.JoinRequestResponseDTO;
import com.speakBuddy.speackBuddy_backend.dto.PublicExchangeResponseDTO;
import com.speakBuddy.speackBuddy_backend.exception.ResourceNotFoundException;
import com.speakBuddy.speackBuddy_backend.models.*;
import com.speakBuddy.speackBuddy_backend.models.AchievementType;
import com.speakBuddy.speackBuddy_backend.repository.ExchangeParticipantRepository;
import com.speakBuddy.speackBuddy_backend.repository.ExchangeRepository;
import com.speakBuddy.speackBuddy_backend.repository.UserRepository;
import com.speakBuddy.speackBuddy_backend.repository.specifications.ExchangeSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExchangeService {

    private final ExchangeRepository exchangeRepository;
    private final ExchangeParticipantRepository participantRepository;
    private final UserRepository userRepository;
    private final ExperienceService experienceService;
    private final AchievementService achievementService;
    private final ExchangeEligibilityService eligibilityService;
    private final ExchangeMapper exchangeMapper;
    private final ExchangeJoinRequestService joinRequestService;

    public ExchangeService(ExchangeRepository exchangeRepository,
                           ExchangeParticipantRepository participantRepository,
                           UserRepository userRepository,
                           ExperienceService experienceService,
                           AchievementService achievementService,
                           ExchangeEligibilityService eligibilityService,
                           ExchangeMapper exchangeMapper,
                           ExchangeJoinRequestService joinRequestService) {
        this.exchangeRepository = exchangeRepository;
        this.participantRepository = participantRepository;
        this.userRepository = userRepository;
        this.experienceService = experienceService;
        this.achievementService = achievementService;
        this.eligibilityService = eligibilityService;
        this.exchangeMapper = exchangeMapper;
        this.joinRequestService = joinRequestService;
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

        if (dto.getIsPublic() != null) {
            exchange.setIsPublic(dto.getIsPublic());
        }
        exchange.setMaxParticipants(dto.getMaxParticipants());
        exchange.setDescription(dto.getDescription());
        exchange.setNativeLanguageCode(dto.getNativeLanguageCode());
        exchange.setTargetLanguageCode(dto.getTargetLanguageCode());

        if (dto.getRequiredLevelMinOrder() != null && dto.getRequiredLevelMaxOrder() != null) {
            if (dto.getRequiredLevelMinOrder() > dto.getRequiredLevelMaxOrder()) {
                throw new IllegalArgumentException("El nivel mínimo no puede ser mayor que el nivel máximo");
            }
            exchange.setRequiredLevelMinOrder(dto.getRequiredLevelMinOrder());
            exchange.setRequiredLevelMaxOrder(dto.getRequiredLevelMaxOrder());
            exchange.setRequiredLevel(eligibilityService.buildLevelRangeLabel(
                    dto.getRequiredLevelMinOrder(), dto.getRequiredLevelMaxOrder()));
        } else {
            exchange.setRequiredLevel(dto.getRequiredLevel());
            int minOrder = eligibilityService.requiredLevelToMinLevel(dto.getRequiredLevel());
            exchange.setRequiredLevelMinOrder(minOrder);
            exchange.setRequiredLevelMaxOrder(ExchangeEligibilityService.LEVEL_ORDER_MAX);
        }

        if (dto.getTopics() != null && !dto.getTopics().isEmpty()) {
            exchange.setTopics(new ArrayList<>(dto.getTopics()));
        }
        if (Boolean.TRUE.equals(dto.getIsPublic()) && (dto.getPlatforms() == null || dto.getPlatforms().isEmpty())) {
            throw new IllegalArgumentException("Selecciona al menos una plataforma de videollamada");
        }
        if (dto.getPlatforms() != null && !dto.getPlatforms().isEmpty()) {
            exchange.setPlatforms(new ArrayList<>(dto.getPlatforms()));
        }

        if (Boolean.FALSE.equals(dto.getIsPublic())) {
            exchange.setPassword(generateRandomPassword(6));
        }
        exchange = exchangeRepository.save(exchange);

        ExchangeParticipant creatorParticipant = new ExchangeParticipant();
        creatorParticipant.setExchange(exchange);
        creatorParticipant.setUser(creator);
        creatorParticipant.setRole("creator");
        participantRepository.save(creatorParticipant);

        long exchangesCreated = participantRepository.countByUserAndRole(creator, "creator");
        achievementService.updateProgressByType(creator.getId(), AchievementType.HOST, (int) exchangesCreated);

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

        return exchangeMapper.toResponseDTO(exchange, creatorUserId);
    }

    public List<ExchangeResponseDTO> getJoinedExchanges(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        List<ExchangeParticipant> myParticipations = participantRepository.findByUser(user);
        List<ExchangeResponseDTO> result = new ArrayList<>();
        for (ExchangeParticipant ep : myParticipations) {
            Exchange e = ep.getExchange();
            if (e.getStatus() != ExchangeStatus.CANCELLED) {
                result.add(exchangeMapper.toResponseDTO(e, userId));
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

        return exchangeMapper.toResponseDTO(exchange, userId);
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

        return exchangeMapper.toResponseDTO(exchange, userId);
    }

    @Transactional
    public ExchangeResponseDTO joinWithPassword(Long exchangeId, String password, Long userId) {
        Exchange exchange = exchangeRepository.findById(exchangeId)
                .orElseThrow(() -> new ResourceNotFoundException("Intercambio no encontrado"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (Boolean.TRUE.equals(exchange.getIsPublic())) {
            throw new IllegalArgumentException("Este intercambio es público. Usa el botón Unirse habitual");
        }
        if (exchange.getPassword() == null || !exchange.getPassword().equalsIgnoreCase(password.trim())) {
            throw new IllegalArgumentException("Contraseña incorrecta");
        }
        if (exchange.getStatus() != ExchangeStatus.SCHEDULED) {
            throw new IllegalArgumentException("Este intercambio ya no está disponible");
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

        return exchangeMapper.toResponseDTO(exchange, userId);
    }

    @Transactional
    public void leave(Long exchangeId, Long userId) {
        Exchange exchange = exchangeRepository.findById(exchangeId)
                .orElseThrow(() -> new ResourceNotFoundException("Intercambio no encontrado"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        ExchangeParticipant participant = participantRepository.findByExchangeAndUser(exchange, user)
                .orElseThrow(() -> new ResourceNotFoundException("No eres participante de este intercambio"));

        if (exchange.getStatus() != ExchangeStatus.SCHEDULED) {
            throw new IllegalArgumentException("No se puede abandonar un intercambio que ya ha terminado o está cancelado");
        }

        if ("creator".equals(participant.getRole())) {
            int participantCount = participantRepository.findByExchange(exchange).size();
            if (participantCount > 1) {
                throw new IllegalArgumentException("El creador no puede abandonar el intercambio");
            }
            exchange.setStatus(ExchangeStatus.CANCELLED);
            exchangeRepository.save(exchange);
            participantRepository.delete(participant);
            return;
        }

        participantRepository.delete(participant);
    }

    // --- Delegación a ExchangeJoinRequestService ---

    @Transactional
    public void requestToJoin(Long exchangeId, Long userId) {
        joinRequestService.requestToJoin(exchangeId, userId);
    }

    public List<JoinRequestResponseDTO> getJoinRequests(Long exchangeId, Long currentUserId) {
        return joinRequestService.getJoinRequests(exchangeId, currentUserId);
    }

    @Transactional
    public void acceptJoinRequest(Long exchangeId, Long requestId, Long creatorUserId) {
        joinRequestService.acceptJoinRequest(exchangeId, requestId, creatorUserId);
    }

    @Transactional
    public void rejectJoinRequest(Long exchangeId, Long requestId, Long creatorUserId) {
        joinRequestService.rejectJoinRequest(exchangeId, requestId, creatorUserId);
    }

    // --- Fin delegación ---

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
            return exchangeMapper.toResponseDTO(exchange, userId);
        }

        participant.setConfirmed(true);
        participant.setConfirmedAt(LocalDateTime.now());
        participantRepository.save(participant);

        boolean allConfirmed = participantRepository.findByExchange(exchange).stream()
                .allMatch(ExchangeParticipant::isConfirmed);

        if (allConfirmed) {
            exchange.setStatus(ExchangeStatus.COMPLETED);
            exchangeRepository.save(exchange);

            int durationMinutes = exchange.getDurationMinutes() != null ? exchange.getDurationMinutes() : 0;
            for (ExchangeParticipant p : participantRepository.findByExchange(exchange)) {
                User u = p.getUser();
                u.setCompletedExchanges((u.getCompletedExchanges() != null ? u.getCompletedExchanges() : 0) + 1);
                u.setTotalExchangeMinutes((u.getTotalExchangeMinutes() != null ? u.getTotalExchangeMinutes() : 0) + durationMinutes);
                userRepository.save(u);

                experienceService.addExperienceForExchange(u, durationMinutes);

                int completedExchanges = u.getCompletedExchanges();
                achievementService.updateProgressByType(u.getId(), AchievementType.CONVERSATIONALIST, completedExchanges);

                int streakDays = u.getCurrentStreakDays() != null ? u.getCurrentStreakDays() : 0;
                achievementService.updateProgressByType(u.getId(), AchievementType.STREAK, streakDays);

                int languagesCount = u.getLanguagesToLearn().size() + 1;
                achievementService.updateProgressByType(u.getId(), AchievementType.POLYGLOT, languagesCount);
            }
        }

        return exchangeMapper.toResponseDTO(exchange, userId);
    }

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
                updated++;
            }
        }
        return updated;
    }

    public Page<PublicExchangeResponseDTO> searchPublicExchanges(
            Long currentUserId,
            String q,
            int page,
            int pageSize,
            String requiredLevel,
            Integer requiredLevelOrder,
            LocalDateTime minDate,
            Integer maxDuration,
            String nativeLang,
            String targetLang) {

        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("scheduledAt").ascending());
        var spec = ExchangeSpecification.publicExchangesWithFilters(
                q, requiredLevel, requiredLevelOrder, minDate, maxDuration, nativeLang, targetLang);

        Page<Exchange> exchangesPage = exchangeRepository.findAll(spec, pageable);
        User currentUser = currentUserId != null
                ? userRepository.findById(currentUserId).orElse(null)
                : null;

        return exchangesPage.map(ex -> exchangeMapper.toPublicExchangeResponseDTO(ex, currentUser));
    }

    private String generateRandomPassword(int length) {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
