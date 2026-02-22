package com.speakBuddy.speackBuddy_backend.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "favorite_topics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoriteTopic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TopicCategory category;

    @Column(nullable = false)
    private String level;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String mainText;

    @Column(columnDefinition = "TEXT")
    private String positionA;

    @Column(columnDefinition = "TEXT")
    private String positionB;

    @ElementCollection
    @CollectionTable(name = "favorite_topic_vocabulary", joinColumns = @JoinColumn(name = "topic_id"))
    @Column(name = "word")
    @Builder.Default
    private List<String> suggestedVocabulary = new ArrayList<>();

    @Column(nullable = false, length = 10)
    private String language;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime savedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime generatedAt;
}
