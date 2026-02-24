package com.speakBuddy.speackBuddy_backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class UserSummaryDTO {
    private Long id;
    private String username;
    private String profilePicture;
    private String nativeLanguage;
    private String nativeLanguageCode;
    private String country;
    private Boolean isPro;
    private Integer level;
    private Double averageRating;
    private Integer totalReviews;
    private Integer exchanges;

    private List<LearningSummaryDTO> languagesToLearn;

    @Data
    public static class LearningSummaryDTO {
        private String languageName;
        private String languageCode;
        private String level;
    }
}