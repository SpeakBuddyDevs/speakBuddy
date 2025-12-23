package com.speakBuddy.speackBuddy_backend.service;

import com.speakBuddy.speackBuddy_backend.dto.ReviewRequestDTO;
import com.speakBuddy.speackBuddy_backend.exception.ResourceNotFoundException;
import com.speakBuddy.speackBuddy_backend.models.Review;
import com.speakBuddy.speackBuddy_backend.models.User;
import com.speakBuddy.speackBuddy_backend.repository.ReviewRepository;
import com.speakBuddy.speackBuddy_backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects; // <--- IMPORTANTE para comparar nulls de forma segura
import java.util.Optional;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    public ReviewService(ReviewRepository reviewRepository, UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void createOrUpdateReview(Long reviewerId, Long revieweeId, ReviewRequestDTO dto) {

        // 1. Validaciones básicas
        if (reviewerId.equals(revieweeId)) {
            throw new IllegalArgumentException("No puedes valorarte a ti mismo.");
        }

        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new ResourceNotFoundException("Reviewer not found"));

        User reviewee = userRepository.findById(revieweeId)
                .orElseThrow(() -> new ResourceNotFoundException("User to review not found"));

        // 2. Buscar si ya existe la review
        Optional<Review> existingReviewOpt = reviewRepository.findByReviewerAndReviewee(reviewer, reviewee);

        if (existingReviewOpt.isPresent()) {
            Review existingReview = existingReviewOpt.get();

            boolean sameScore = Objects.equals(existingReview.getScore(), dto.getScore());
            boolean sameComment = Objects.equals(existingReview.getComment(), dto.getComment());

            if (sameScore && sameComment) {
                // Si todo es igual.
                return;
            }

            // ALGO ha cambiado
            int oldScore = existingReview.getScore();

            existingReview.setScore(dto.getScore());
            existingReview.setComment(dto.getComment());
            existingReview.setTimestamp(LocalDateTime.now()); // actualizar la fecha

            reviewRepository.save(existingReview);

            if (!sameScore) {
                recalculateRatingOnUpdate(reviewee, oldScore, dto.getScore());
            }

        } else {
            // Crear una review
            Review newReview = new Review();
            newReview.setReviewer(reviewer);
            newReview.setReviewee(reviewee);
            newReview.setScore(dto.getScore());
            newReview.setComment(dto.getComment());


            reviewRepository.save(newReview);

            recalculateRatingOnCreate(reviewee, dto.getScore());
        }
    }

    private void recalculateRatingOnCreate(User user, int newScore) {
        Double currentTotalPoints = user.getAverageRating() * user.getTotalReviews();
        user.setTotalReviews(user.getTotalReviews() + 1);
        Double newAverage = (currentTotalPoints + newScore) / user.getTotalReviews();
        user.setAverageRating(round(newAverage));
        userRepository.save(user);
    }

    private void recalculateRatingOnUpdate(User user, int oldScore, int newScore) {
        Double currentTotalPoints = user.getAverageRating() * user.getTotalReviews();
        Double newTotalPoints = currentTotalPoints - oldScore + newScore;
        Double newAverage = newTotalPoints / user.getTotalReviews();
        user.setAverageRating(round(newAverage));
        userRepository.save(user);
    }

    private Double round(Double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}