package com.speakBuddy.speackBuddy_backend.models;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_languages_learning", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "language_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserLanguagesLearning {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "language_id", nullable = false)
    private Language language;

    @ManyToOne(optional = false)
    @JoinColumn(name = "level_id", nullable = false)
    private LanguageLevel level;

}
