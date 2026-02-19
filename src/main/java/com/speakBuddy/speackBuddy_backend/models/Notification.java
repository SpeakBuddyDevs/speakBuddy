package com.speakBuddy.speackBuddy_backend.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 50)
    private String type; // NEW_MESSAGE, NEW_EXCHANGE_MESSAGE, etc.

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    /** chatId formato "chat_{min}_{max}" o null si es de intercambio */
    @Column(name = "chat_id", length = 100)
    private String chatId;

    /** ID del intercambio si type=NEW_EXCHANGE_MESSAGE, null si es chat 1:1 */
    @Column(name = "exchange_id")
    private Long exchangeId;

    @Column(name = "`read`", nullable = false)
    @Builder.Default
    private Boolean read = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static final String TYPE_NEW_DIRECT_MESSAGE = "NEW_MESSAGE";
    public static final String TYPE_NEW_EXCHANGE_MESSAGE = "NEW_EXCHANGE_MESSAGE";
}
