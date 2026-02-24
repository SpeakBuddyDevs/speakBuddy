package com.speakBuddy.speackBuddy_backend.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

    /** Nivel requerido (legacy): Principiante, Intermedio, Avanzado. Se usa para display si min/max son null. */
    @Column(name = "required_level", length = 50)
    private String requiredLevel;

    /** Nivel mínimo del rango CEFR (level_order 1-6: A1=1 .. C2=6). Null = usar legacy requiredLevel. */
    @Column(name = "required_level_min_order")
    private Integer requiredLevelMinOrder;

    /** Nivel máximo del rango CEFR (level_order 1-6). Null = usar legacy. */
    @Column(name = "required_level_max_order")
    private Integer requiredLevelMaxOrder;

    /** Temas de conversación opcionales (ej: Música, viajes, practicar entrevista) */
    @ElementCollection
    @CollectionTable(name = "exchange_topics", joinColumns = @JoinColumn(name = "exchange_id"))
    @Column(name = "topic", length = 100)
    private List<String> topics = new ArrayList<>();

    /** Plataformas de videollamada (Zoom, Google Meet, Discord, Otra: nombre, etc.) */
    @ElementCollection
    @CollectionTable(name = "exchange_platforms", joinColumns = @JoinColumn(name = "exchange_id"))
    @Column(name = "platform", length = 100)
    private List<String> platforms = new ArrayList<>();

    /** Contraseña para unirse a intercambios privados. Null en intercambios públicos. */
    @Column(name = "password", length = 20)
    private String password;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "exchange", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ExchangeParticipant> participants = new HashSet<>();
}
