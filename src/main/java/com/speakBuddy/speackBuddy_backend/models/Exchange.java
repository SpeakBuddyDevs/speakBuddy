package com.speakBuddy.speackBuddy_backend.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "exchanges")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Exchange {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ExchangeStatus status = ExchangeStatus.SCHEDULED;

    @Column(length = 50)
    private String type = "group"; // one_to_one, group

    @Column(length = 200)
    private String title;

    /** true si el intercambio aparece en el catálogo público; false para intercambios privados */
    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = false;

    /** Máximo de participantes (null = sin límite, típico en intercambios privados) */
    @Column(name = "max_participants")
    private Integer maxParticipants;

    @Column(length = 2000)
    private String description;

    /** Código ISO del idioma nativo que el creador ofrece (ej: ES, EN) */
    @Column(name = "native_language_code", length = 10)
    private String nativeLanguageCode;

    /** Código ISO del idioma que el creador quiere practicar (ej: EN, FR) */
    @Column(name = "target_language_code", length = 10)
    private String targetLanguageCode;

    /** Nivel requerido: Principiante, Intermedio, Avanzado */
    @Column(name = "required_level", length = 50)
    private String requiredLevel;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "exchange", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ExchangeParticipant> participants = new HashSet<>();
}
