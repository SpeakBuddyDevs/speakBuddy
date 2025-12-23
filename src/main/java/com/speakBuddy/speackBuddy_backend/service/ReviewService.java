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

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    // anti-spam
    private static final int SPAM_PREVENTION_HOURS = 24;

    public ReviewService(ReviewRepository reviewRepository, UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void createReview(Long reviewerId, Long revieweeId, ReviewRequestDTO dto) {

        // 1. Validar que no se valore a sí mismo
        if (reviewerId.equals(revieweeId)) {
            throw new IllegalArgumentException("No puedes valorarte a ti mismo.");
        }

        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new ResourceNotFoundException("Reviewer not found"));

        User reviewee = userRepository.findById(revieweeId)
                .orElseThrow(() -> new ResourceNotFoundException("User to review not found"));

        // 2. Anti-Spam Check
        LocalDateTime limitDate = LocalDateTime.now().minusHours(SPAM_PREVENTION_HOURS);
        boolean recentReviewExists = reviewRepository
                .existsByReviewerAndRevieweeAndTimestampAfter(reviewer, reviewee, limitDate);

        if (recentReviewExists) {
            throw new IllegalStateException("Ya has valorado a este usuario recientemente. Inténtalo mañana.");
        }

        // 3. Crear y guardar la Review
        Review review = new Review();
        review.setReviewer(reviewer);
        review.setReviewee(reviewee);
        review.setScore(dto.getScore());
        review.setComment(dto.getComment());
        reviewRepository.save(review);

        // 4. Actualizar la media del Usuario (Matemática incremental)
        updateUserRating(reviewee, dto.getScore());
    }

    private void updateUserRating(User user, int newScore) {
        Double currentTotalScore = user.getAverageRating() * user.getTotalReviews();
        user.setTotalReviews(user.getTotalReviews() + 1);

        Double newAverage = (currentTotalScore + newScore) / user.getTotalReviews();

        newAverage = Math.round(newAverage * 100.0) / 100.0;

        user.setAverageRating(newAverage);
        userRepository.save(user);
    }
}