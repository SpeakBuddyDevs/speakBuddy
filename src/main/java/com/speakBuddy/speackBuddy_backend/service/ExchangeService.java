package com.speakBuddy.speackBuddy_backend.service;

import com.speakBuddy.speackBuddy_backend.dto.CreateExchangeRequestDTO;
import com.speakBuddy.speackBuddy_backend.dto.ExchangeParticipantDTO;
import com.speakBuddy.speackBuddy_backend.dto.ExchangeResponseDTO;
import com.speakBuddy.speackBuddy_backend.dto.JoinRequestResponseDTO;
import com.speakBuddy.speackBuddy_backend.dto.PublicExchangeResponseDTO;
import com.speakBuddy.speackBuddy_backend.exception.ResourceNotFoundException;
import com.speakBuddy.speackBuddy_backend.models.*;
import com.speakBuddy.speackBuddy_backend.models.AchievementType;
import com.speakBuddy.speackBuddy_backend.repository.ExchangeChatMessageRepository;
import com.speakBuddy.speackBuddy_backend.repository.ExchangeJoinRequestRepository;
import com.speakBuddy.speackBuddy_backend.repository.ExchangeParticipantRepository;
import com.speakBuddy.speackBuddy_backend.repository.ExchangeRepository;
import com.speakBuddy.speackBuddy_backend.repository.LanguageLevelRepository;
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

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ExchangeService {

    private static final int MIN_LEVEL_PRINCIPIANTE = 1;
    private static final int MIN_LEVEL_INTERMEDIO = 4;
    private static final int MIN_LEVEL_AVANZADO = 5; // C1

    private static final int LEVEL_ORDER_MIN = 1;
    private static final int LEVEL_ORDER_MAX = 6;

    private final ExchangeRepository exchangeRepository;
    private final ExchangeParticipantRepository participantRepository;
    private final ExchangeJoinRequestRepository joinRequestRepository;
    private final ExchangeChatMessageRepository exchangeChatMessageRepository;
    private final UserRepository userRepository;
    private final LanguageRepository languageRepository;
    private final LanguageLevelRepository languageLevelRepository;
    private final NotificationService notificationService;
    private final ExperienceService experienceService;
    private final AchievementService achievementService;

    public ExchangeService(ExchangeRepository exchangeRepository,
                           ExchangeParticipantRepository participantRepository,
                           ExchangeJoinRequestRepository joinRequestRepository,
                           ExchangeChatMessageRepository exchangeChatMessageRepository,
                           UserRepository userRepository,
                           LanguageRepository languageRepository,
                           LanguageLevelRepository languageLevelRepository,
                           NotificationService notificationService,
                           ExperienceService experienceService,
                           AchievementService achievementService) {
        this.exchangeRepository = exchangeRepository;
        this.participantRepository = participantRepository;
        this.joinRequestRepository = joinRequestRepository;
        this.exchangeChatMessageRepository = exchangeChatMessageRepository;
        this.userRepository = userRepository;
        this.languageRepository = languageRepository;
        this.languageLevelRepository = languageLevelRepository;
        this.notificationService = notificationService;
        this.experienceService = experienceService;
        this.achievementService = achievementService;
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
        if (dto.getRequiredLevelMinOrder() != null && dto.getRequiredLevelMaxOrder() != null) {
            if (dto.getRequiredLevelMinOrder() > dto.getRequiredLevelMaxOrder()) {
                throw new IllegalArgumentException("El nivel mínimo no puede ser mayor que el nivel máximo");
            }
            exchange.setRequiredLevelMinOrder(dto.getRequiredLevelMinOrder());
            exchange.setRequiredLevelMaxOrder(dto.getRequiredLevelMaxOrder());
            exchange.setRequiredLevel(buildLevelRangeLabel(dto.getRequiredLevelMinOrder(), dto.getRequiredLevelMaxOrder()));
        } else {
            exchange.setRequiredLevel(dto.getRequiredLevel());
            int minOrder = requiredLevelToMinLevel(dto.getRequiredLevel());
            exchange.setRequiredLevelMinOrder(minOrder);
            exchange.setRequiredLevelMaxOrder(LEVEL_ORDER_MAX);
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
        // Generar contraseña para intercambios privados
        if (Boolean.FALSE.equals(dto.getIsPublic())) {
            String password = generateRandomPassword(6);
            exchange.setPassword(password);
        }
        exchange = exchangeRepository.save(exchange);

        ExchangeParticipant creatorParticipant = new ExchangeParticipant();
        creatorParticipant.setExchange(exchange);
        creatorParticipant.setUser(creator);
        creatorParticipant.setRole("creator");
        participantRepository.save(creatorParticipant);

        // Actualizar logro HOST (intercambios creados)
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

    /**
     * Permite unirse a un intercambio privado validando la contraseña.
     * No comprueba requisitos de nivel/idioma (los intercambios privados son por invitación).
     */
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

        if (exchange.getStatus() != ExchangeStatus.SCHEDULED) {
            throw new IllegalArgumentException("No se puede abandonar un intercambio que ya ha terminado o está cancelado");
        }

        if ("creator".equals(participant.getRole())) {
            int participantCount = participantRepository.findByExchange(exchange).size();
            if (participantCount > 1) {
                throw new IllegalArgumentException("El creador no puede abandonar el intercambio");
            }
            // Solo el creador está en el intercambio: cancelar para que deje de mostrarse en públicos
            exchange.setStatus(ExchangeStatus.CANCELLED);
            exchangeRepository.save(exchange);
            participantRepository.delete(participant);
            return;
        }

        participantRepository.delete(participant);
    }

    /**
     * Usuario no elegible solicita unirse a un intercambio público.
     * Crea una solicitud PENDING y notifica al creador.
     */
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

        User creator = findCreator(exchange);
        if (creator != null) {
            notificationService.createExchangeJoinRequestNotification(
                    creator, exchangeId, user,
                    exchange.getTitle() != null ? exchange.getTitle() : "Intercambio");
        }
    }

    /**
     * Lista solicitudes pendientes de un intercambio. Solo el creador puede verlas.
     */
    public List<JoinRequestResponseDTO> getJoinRequests(Long exchangeId, Long currentUserId) {
        Exchange exchange = exchangeRepository.findById(exchangeId)
                .orElseThrow(() -> new ResourceNotFoundException("Intercambio no encontrado"));

        User creator = findCreator(exchange);
        if (creator == null || !creator.getId().equals(currentUserId)) {
            throw new IllegalArgumentException("Solo el creador puede ver las solicitudes de unión");
        }

        int minOrder = effectiveMinOrder(exchange);
        int maxOrder = effectiveMaxOrder(exchange);
        String nativeLanguageName = resolveLanguageName(exchange.getNativeLanguageCode());
        String targetLanguageName = resolveLanguageName(exchange.getTargetLanguageCode());

        List<ExchangeJoinRequest> requests = joinRequestRepository.findByExchangeIdAndStatus(exchangeId, ExchangeJoinRequestStatus.PENDING);
        List<JoinRequestResponseDTO> result = new ArrayList<>();
        for (ExchangeJoinRequest req : requests) {
            var eligibility = computeEligibility(exchange, req.getUser(), minOrder, maxOrder, targetLanguageName, nativeLanguageName);
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

    /**
     * El creador acepta una solicitud de unión. El solicitante pasa a ser participante.
     */
    @Transactional
    public void acceptJoinRequest(Long exchangeId, Long requestId, Long creatorUserId) {
        Exchange exchange = exchangeRepository.findById(exchangeId)
                .orElseThrow(() -> new ResourceNotFoundException("Intercambio no encontrado"));

        User creator = userRepository.findById(creatorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        User actualCreator = findCreator(exchange);
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

    /**
     * El creador rechaza una solicitud de unión.
     */
    @Transactional
    public void rejectJoinRequest(Long exchangeId, Long requestId, Long creatorUserId) {
        Exchange exchange = exchangeRepository.findById(exchangeId)
                .orElseThrow(() -> new ResourceNotFoundException("Intercambio no encontrado"));

        User creator = userRepository.findById(creatorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        User actualCreator = findCreator(exchange);
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

            int durationMinutes = exchange.getDurationMinutes() != null ? exchange.getDurationMinutes() : 0;
            for (ExchangeParticipant p : participantRepository.findByExchange(exchange)) {
                User u = p.getUser();
                u.setCompletedExchanges((u.getCompletedExchanges() != null ? u.getCompletedExchanges() : 0) + 1);
                u.setTotalExchangeMinutes((u.getTotalExchangeMinutes() != null ? u.getTotalExchangeMinutes() : 0) + durationMinutes);
                userRepository.save(u);

                experienceService.addExperienceForExchange(u, durationMinutes);

                // Actualizar logros relacionados con intercambios completados
                int completedExchanges = u.getCompletedExchanges();
                achievementService.updateProgressByType(u.getId(), AchievementType.CONVERSATIONALIST, completedExchanges);

                // Actualizar logro de racha
                int streakDays = u.getCurrentStreakDays() != null ? u.getCurrentStreakDays() : 0;
                achievementService.updateProgressByType(u.getId(), AchievementType.STREAK, streakDays);

                // Actualizar logro de políglota (idiomas que practica)
                int languagesCount = u.getLanguagesToLearn().size() + 1; // idiomas que aprende + nativo
                achievementService.updateProgressByType(u.getId(), AchievementType.POLYGLOT, languagesCount);
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

        return exchangesPage.map(ex -> toPublicExchangeResponseDTO(ex, currentUser));
    }

    private PublicExchangeResponseDTO toPublicExchangeResponseDTO(Exchange exchange, User currentUser) {
        User creator = findCreator(exchange);
        var participants = participantRepository.findByExchange(exchange);
        int currentParticipants = participants.size();
        int minOrder = effectiveMinOrder(exchange);
        int maxOrder = effectiveMaxOrder(exchange);
        String requiredLevelLabel = buildLevelRangeLabel(minOrder, maxOrder);

        boolean isJoined = currentUser != null && participants.stream()
                .anyMatch(p -> p.getUser().getId().equals(currentUser.getId()));

        boolean hasPendingJoinRequest = currentUser != null && !isJoined
                && joinRequestRepository.existsByExchangeAndUserAndStatus(exchange, currentUser, ExchangeJoinRequestStatus.PENDING);

        String nativeLanguageName = resolveLanguageName(exchange.getNativeLanguageCode());
        String targetLanguageName = resolveLanguageName(exchange.getTargetLanguageCode());

        var eligibility = computeEligibility(exchange, currentUser, minOrder, maxOrder, targetLanguageName, nativeLanguageName);

        // Contraseña solo visible para el creador
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
                .topics(exchange.getTopics() != null && !exchange.getTopics().isEmpty() ? exchange.getTopics() : null)
                .platforms(exchange.getPlatforms() != null && !exchange.getPlatforms().isEmpty() ? exchange.getPlatforms() : null)
                .isEligible(eligibility.isEligible())
                .unmetRequirements(eligibility.unmetRequirements())
                .isJoined(isJoined)
                .hasPendingJoinRequest(hasPendingJoinRequest)
                .isPublic(Boolean.TRUE.equals(exchange.getIsPublic()))
                .password(password)
                .build();
    }

    private User findCreator(Exchange exchange) {
        return participantRepository.findByExchange(exchange).stream()
                .filter(p -> "creator".equals(p.getRole()))
                .map(ExchangeParticipant::getUser)
                .findFirst()
                .orElse(null);
    }

    private int effectiveMinOrder(Exchange exchange) {
        if (exchange.getRequiredLevelMinOrder() != null) return exchange.getRequiredLevelMinOrder();
        return requiredLevelToMinLevel(exchange.getRequiredLevel());
    }

    private int effectiveMaxOrder(Exchange exchange) {
        if (exchange.getRequiredLevelMaxOrder() != null) return exchange.getRequiredLevelMaxOrder();
        return LEVEL_ORDER_MAX;
    }

    private String buildLevelRangeLabel(int minOrder, int maxOrder) {
        String minName = languageLevelRepository.findByLevelOrder(minOrder)
                .map(LanguageLevel::getName)
                .orElse("A1");
        String maxName = languageLevelRepository.findByLevelOrder(maxOrder)
                .map(LanguageLevel::getName)
                .orElse("C2");
        if (minOrder == maxOrder) return minName;
        return minName + " – " + maxName;
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
                                                 int minOrder, int maxOrder, String targetLanguageName, String nativeLanguageName) {
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
        String levelRangeLabel = buildLevelRangeLabel(minOrder, maxOrder);
        if (nativeIso != null) {
            Optional<UserLanguagesLearning> learning = currentUser.getLanguagesToLearn().stream()
                    .filter(l -> l.getLanguage() != null
                            && nativeIso.equals(l.getLanguage().getIsoCode().toLowerCase()))
                    .findFirst();
            if (learning.isEmpty()) {
                unmet.add("Nivel de " + (nativeLanguageName != null ? nativeLanguageName : nativeIso) + ": " + levelRangeLabel);
            } else {
                int userLevelOrder = learning.get().getLevel() != null
                        ? learning.get().getLevel().getLevelOrder()
                        : 0;
                if (userLevelOrder < minOrder || userLevelOrder > maxOrder) {
                    unmet.add("Nivel de " + (nativeLanguageName != null ? nativeLanguageName : nativeIso) + ": " + levelRangeLabel);
                }
            }
        }

        return new EligibilityResult(unmet.isEmpty(), unmet.isEmpty() ? null : unmet);
    }

    private ExchangeResponseDTO toResponseDTO(Exchange exchange, Long currentUserId) {
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

        boolean canConfirm = exchange.getStatus() == ExchangeStatus.ENDED_PENDING_CONFIRMATION
                && currentUserParticipant != null
                && !currentUserParticipant.isConfirmed();

        boolean allConfirmed = participants.stream().allMatch(ExchangeParticipant::isConfirmed);

        LocalDateTime lastMessageAt = exchangeChatMessageRepository
                .findFirstByExchangeOrderByTimestampDesc(exchange)
                .map(ExchangeChatMessage::getTimestamp)
                .orElse(null);

        // Contraseña solo visible para el creador
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
                .nativeLanguage(resolveLanguageName(exchange.getNativeLanguageCode()))
                .targetLanguage(resolveLanguageName(exchange.getTargetLanguageCode()))
                .platforms(exchange.getPlatforms() != null && !exchange.getPlatforms().isEmpty()
                        ? exchange.getPlatforms() : null)
                .maxParticipants(exchange.getMaxParticipants())
                .topics(exchange.getTopics() != null && !exchange.getTopics().isEmpty()
                        ? exchange.getTopics() : null)
                .build();
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
