package com.speakBuddy.speackBuddy_backend.repository;

import com.speakBuddy.speackBuddy_backend.models.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByUser_IdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<Notification> findByUser_IdAndReadOrderByCreatedAtDesc(Long userId, Boolean read, Pageable pageable);

    long countByUser_IdAndRead(Long userId, Boolean read);
}
