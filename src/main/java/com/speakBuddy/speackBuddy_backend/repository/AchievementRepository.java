package com.speakBuddy.speackBuddy_backend.repository;

import com.speakBuddy.speackBuddy_backend.models.Achievement;
import com.speakBuddy.speackBuddy_backend.models.AchievementType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AchievementRepository extends JpaRepository<Achievement, Long> {

    Optional<Achievement> findByType(AchievementType type);
}
