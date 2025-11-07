package com.speakBuddy.speackBuddy_backend.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "language_levels")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LanguageLevel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // "A1 - Beginner", "A2 - Elementary", etc.

    private Integer levelOrder;


}
