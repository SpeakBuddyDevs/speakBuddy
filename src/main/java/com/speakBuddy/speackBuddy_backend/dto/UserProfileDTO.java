package com.speakBuddy.speackBuddy_backend.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class UserProfileDTO {
    private Long id;
    private String name;
    private String email;
    private String nativeLanguage;
    private Double rating;
    private Integer exchanges;
    private List<LearningLanguageDTO> learningLanguages;

    private Integer level;
    private Double progressPct;
    private Integer languagesCount;
    private Integer hoursTotal;
    private Integer currentStreakDays;
    private Integer bestStreakDays;
    private Integer medals;
    private Boolean isPro;
    private String avatarUrl;
    private String description;

    @Data
    @Builder
    public static class LearningLanguageDTO {
        private String code;  // Código ISO
        private String name;  // Nombre legible
        private String level; // Nivel (A1, B2...)
        private boolean active;
    }
}