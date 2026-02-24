package com.speakBuddy.speackBuddy_backend.repository;

import com.speakBuddy.speackBuddy_backend.models.Achievement;
import com.speakBuddy.speackBuddy_backend.models.User;
import com.speakBuddy.speackBuddy_backend.models.UserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserAchievementRepository extends JpaRepository<UserAchievement, Long> {

    List<UserAchievement> findByUser_IdOrderByAchievement_IdAsc(Long userId);

    List<UserAchievement> findByUser_IdAndIsUnlockedOrderByUnlockedAtDesc(Long userId, Boolean isUnlocked);

    Optional<UserAchievement> findByUserAndAchievement(User user, Achievement achievement);

    long countByUser_IdAndIsUnlocked(Long userId, Boolean isUnlocked);
}
