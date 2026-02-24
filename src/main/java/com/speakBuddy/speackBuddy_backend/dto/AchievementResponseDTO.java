package com.speakBuddy.speackBuddy_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AchievementResponseDTO {

    private Long id;
    private String type;
    private String title;
    private String description;
    private Integer currentProgress;
    private Integer targetProgress;
    private Boolean isUnlocked;
    private LocalDateTime unlockedAt;
}
