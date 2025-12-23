package com.speakBuddy.speackBuddy_backend.repository;

import com.speakBuddy.speackBuddy_backend.models.Review;
import com.speakBuddy.speackBuddy_backend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {


    boolean existsByReviewerAndRevieweeAndTimestampAfter(User reviewer, User reviewee, LocalDateTime date);
}