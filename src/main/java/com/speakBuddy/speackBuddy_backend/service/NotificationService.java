package com.speakBuddy.speackBuddy_backend.service;

import com.speakBuddy.speackBuddy_backend.dto.NotificationResponseDTO;
import com.speakBuddy.speackBuddy_backend.exception.ResourceNotFoundException;
import com.speakBuddy.speackBuddy_backend.models.Notification;
import com.speakBuddy.speackBuddy_backend.models.User;
import com.speakBuddy.speackBuddy_backend.repository.NotificationRepository;
import com.speakBuddy.speackBuddy_backend.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;

    public NotificationService(NotificationRepository notificationRepository,
                               UserRepository userRepository,
                               NotificationMapper notificationMapper) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.notificationMapper = notificationMapper;
    }

    /**
     * Crea una notificación de mensaje nuevo en chat 1:1.
     */
    @Transactional
    public void createDirectMessageNotification(User recipient, User sender, String chatId, String messagePreview) {
        String title = sender.getName() + " " + sender.getSurname();
        String body = notificationMapper.truncate(messagePreview, 100);

        Notification notification = Notification.builder()
                .user(recipient)
                .type(Notification.TYPE_NEW_DIRECT_MESSAGE)
                .title(title)
                .body(body)
                .chatId(chatId)
                .exchangeId(null)
                .read(false)
                .build();

        notificationRepository.save(notification);
    }

    /**
     * Crea una notificación de mensaje nuevo en chat de intercambio.
     */
    @Transactional
    public void createExchangeMessageNotification(User recipient, Long exchangeId, String senderName, String messagePreview) {
        String title = "Nuevo mensaje en intercambio";
        String body = senderName + ": " + notificationMapper.truncate(messagePreview, 80);

        Notification notification = Notification.builder()
                .user(recipient)
                .type(Notification.TYPE_NEW_EXCHANGE_MESSAGE)
                .title(title)
                .body(body)
                .chatId(null)
                .exchangeId(exchangeId)
                .read(false)
                .build();

        notificationRepository.save(notification);
    }

    /**
     * Crea una notificación de solicitud de unión a un intercambio público.
     * Se envía al creador del intercambio.
     */
    @Transactional
    public void createExchangeJoinRequestNotification(User recipient, Long exchangeId, User requester, String exchangeTitle) {
        String title = "Solicitud para unirse a intercambio";
        String body = requester.getUsername() + " quiere unirse a \"" + notificationMapper.truncate(exchangeTitle != null ? exchangeTitle : "Intercambio", 50) + "\"";

        Notification notification = Notification.builder()
                .user(recipient)
                .type(Notification.TYPE_EXCHANGE_JOIN_REQUEST)
                .title(title)
                .body(body)
                .chatId(null)
                .exchangeId(exchangeId)
                .requester(requester)
                .read(false)
                .build();

        notificationRepository.save(notification);
    }

    /**
     * Notifica al solicitante que su solicitud de unión fue aceptada o rechazada.
     */
    @Transactional
    public void createJoinRequestResponseNotification(User recipient, Long exchangeId, String exchangeTitle, boolean accepted) {
        String title = accepted ? "Solicitud aceptada" : "Solicitud rechazada";
        String body = accepted
                ? "Tu solicitud para unirte a \"" + notificationMapper.truncate(exchangeTitle != null ? exchangeTitle : "Intercambio", 50) + "\" ha sido aceptada."
                : "Tu solicitud para unirte a \"" + notificationMapper.truncate(exchangeTitle != null ? exchangeTitle : "Intercambio", 50) + "\" ha sido rechazada.";

        Notification notification = Notification.builder()
                .user(recipient)
                .type(accepted ? Notification.TYPE_JOIN_REQUEST_ACCEPTED : Notification.TYPE_JOIN_REQUEST_REJECTED)
                .title(title)
                .body(body)
                .chatId(null)
                .exchangeId(exchangeId)
                .requester(null)
                .read(false)
                .build();

        notificationRepository.save(notification);
    }

    /**
     * Lista notificaciones del usuario con paginación.
     */
    public Page<NotificationResponseDTO> getNotifications(Long userId, Boolean unreadOnly, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> pageResult = unreadOnly != null && unreadOnly
                ? notificationRepository.findByUser_IdAndReadOrderByCreatedAtDesc(userId, false, pageable)
                : notificationRepository.findByUser_IdOrderByCreatedAtDesc(userId, pageable);

        return pageResult.map(notificationMapper::toDto);
    }

    /**
     * Cuenta notificaciones no leídas del usuario.
     */
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUser_IdAndRead(userId, false);
    }

    /**
     * Marca una notificación como leída.
     */
    @Transactional
    public void markAsRead(Long userId, Long notificationId) {
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notificación no encontrada"));
        if (!n.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Notificación no encontrada");
        }
        n.setRead(true);
        notificationRepository.save(n);
    }

    /**
     * Marca varias notificaciones como leídas.
     */
    @Transactional
    public void markAsRead(Long userId, List<Long> ids) {
        if (ids == null || ids.isEmpty()) return;
        for (Long id : ids) {
            markAsRead(userId, id);
        }
    }

}
