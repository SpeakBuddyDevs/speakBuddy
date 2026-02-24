package com.speakBuddy.speackBuddy_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneratedTopicResponseDTO {

    private Long id;
    private String category;
    private String level;
    private String mainText;
    private String positionA;
    private String positionB;
    private List<String> suggestedVocabulary;
    private String language;
    private LocalDateTime generatedAt;
    private boolean isFavorite;
}
