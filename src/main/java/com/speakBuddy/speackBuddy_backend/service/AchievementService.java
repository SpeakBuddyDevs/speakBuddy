package com.speakBuddy.speackBuddy_backend.service;

import com.speakBuddy.speackBuddy_backend.dto.AchievementResponseDTO;
import com.speakBuddy.speackBuddy_backend.models.Achievement;
import com.speakBuddy.speackBuddy_backend.models.AchievementType;
import com.speakBuddy.speackBuddy_backend.models.User;
import com.speakBuddy.speackBuddy_backend.models.UserAchievement;
import com.speakBuddy.speackBuddy_backend.repository.AchievementRepository;
import com.speakBuddy.speackBuddy_backend.repository.UserAchievementRepository;
import com.speakBuddy.speackBuddy_backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AchievementService {

    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final UserRepository userRepository;

    public AchievementService(
            AchievementRepository achievementRepository,
            UserAchievementRepository userAchievementRepository,
            UserRepository userRepository) {
        this.achievementRepository = achievementRepository;
        this.userAchievementRepository = userAchievementRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<AchievementResponseDTO> getUserAchievements(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<Achievement> allAchievements = achievementRepository.findAll();
        List<UserAchievement> userAchievements = userAchievementRepository.findByUser_IdOrderByAchievement_IdAsc(userId);

        Map<Long, UserAchievement> userAchMap = userAchievements.stream()
                .collect(Collectors.toMap(ua -> ua.getAchievement().getId(), ua -> ua));

        List<AchievementResponseDTO> result = new ArrayList<>();

        for (Achievement achievement : allAchievements) {
            UserAchievement userAch = userAchMap.get(achievement.getId());

            AchievementResponseDTO dto = AchievementResponseDTO.builder()
                    .id(achievement.getId())
                    .type(achievement.getType().name())
                    .title(achievement.getTitle())
                    .description(achievement.getDescription())
                    .targetProgress(achievement.getTargetProgress())
                    .currentProgress(userAch != null ? userAch.getCurrentProgress() : 0)
                    .isUnlocked(userAch != null && Boolean.TRUE.equals(userAch.getIsUnlocked()))
                    .unlockedAt(userAch != null ? userAch.getUnlockedAt() : null)
                    .build();

            result.add(dto);
        }

        return result;
    }

    @Transactional
    public void initializeUserAchievements(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<Achievement> allAchievements = achievementRepository.findAll();
        List<UserAchievement> existing = userAchievementRepository.findByUser_IdOrderByAchievement_IdAsc(userId);

        Map<Long, UserAchievement> existingMap = existing.stream()
                .collect(Collectors.toMap(ua -> ua.getAchievement().getId(), ua -> ua));

        for (Achievement achievement : allAchievements) {
            if (!existingMap.containsKey(achievement.getId())) {
                UserAchievement newUserAch = new UserAchievement(user, achievement);
                userAchievementRepository.save(newUserAch);
            }
        }
    }

    @Transactional
    public void updateProgress(Long userId, Achievement achievement, int newProgress) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        UserAchievement userAch = userAchievementRepository.findByUserAndAchievement(user, achievement)
                .orElseGet(() -> {
                    UserAchievement newUserAch = new UserAchievement(user, achievement);
                    return userAchievementRepository.save(newUserAch);
                });

        userAch.setCurrentProgress(newProgress);

        if (newProgress >= achievement.getTargetProgress() && !Boolean.TRUE.equals(userAch.getIsUnlocked())) {
            userAch.setIsUnlocked(true);
            userAch.setUnlockedAt(java.time.LocalDateTime.now());
        }

        userAchievementRepository.save(userAch);
    }

    public long countUnlockedAchievements(Long userId) {
        return userAchievementRepository.countByUser_IdAndIsUnlocked(userId, true);
    }

    @Transactional
    public void updateProgressByType(Long userId, AchievementType type, int newProgress) {
        Achievement achievement = achievementRepository.findByType(type)
                .orElseThrow(() -> new RuntimeException("Logro no encontrado: " + type));
        updateProgress(userId, achievement, newProgress);
    }
}
