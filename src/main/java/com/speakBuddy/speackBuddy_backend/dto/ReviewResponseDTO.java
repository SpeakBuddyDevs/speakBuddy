package com.speakBuddy.speackBuddy_backend.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReviewResponseDTO {
    private Long id;
    private Long reviewerId;
    private String reviewerName;
    private String reviewerPhotoUrl;
    private Integer score;
    private String comment;
    private LocalDateTime timestamp;
}
