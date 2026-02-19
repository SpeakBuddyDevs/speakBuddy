package com.speakBuddy.speackBuddy_backend.repository;

import com.speakBuddy.speackBuddy_backend.models.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage,Long> {
    /**
     * Busca la conversacion entre dos usuarios
     * y la muestra cronologicamente
     */
    @Query("SELECT m FROM ChatMessage m WHERE " +
            "(m.sender.email = :user1 AND m.recipient.email = :user2) OR " +
            "(m.sender.email = :user2 AND m.recipient.email = :user1) " +
            "ORDER BY m.timestamp ASC")
    List<ChatMessage> findChatHistory(@Param("user1") String email1, @Param("user2") String email2);

    /**
     * Busca la conversación entre dos usuarios por sus IDs.
     */
    @Query("SELECT m FROM ChatMessage m WHERE " +
            "(m.sender.id = :userId1 AND m.recipient.id = :userId2) OR " +
            "(m.sender.id = :userId2 AND m.recipient.id = :userId1) " +
            "ORDER BY m.timestamp ASC")
    List<ChatMessage> findChatHistoryByUserIds(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
}
