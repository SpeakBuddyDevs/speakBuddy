package com.speakBuddy.speackBuddy_backend.config;

import com.speakBuddy.speackBuddy_backend.models.Achievement;
import com.speakBuddy.speackBuddy_backend.models.AchievementType;
import com.speakBuddy.speackBuddy_backend.repository.AchievementRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class AchievementDataInitializer implements CommandLineRunner {

    private final AchievementRepository achievementRepository;

    public AchievementDataInitializer(AchievementRepository achievementRepository) {
        this.achievementRepository = achievementRepository;
    }

    @Override
    public void run(String... args) {
        if (achievementRepository.count() == 0) {
            System.out.println("🏆 Inicializando logros predefinidos...");
            
            createAchievement(AchievementType.POLYGLOT, "Políglota", "5 idiomas practicados", 5);
            createAchievement(AchievementType.CONVERSATIONALIST, "Conversador", "50 conversaciones", 50);
            createAchievement(AchievementType.EARLY_BIRD, "Madrugador", "20 sesiones matutinas", 20);
            createAchievement(AchievementType.STAR, "Estrella", "100 valoraciones 5★", 100);
            createAchievement(AchievementType.STREAK, "Racha", "30 días consecutivos", 30);
            createAchievement(AchievementType.EXPLORER, "Explorador", "10 países diferentes", 10);
            createAchievement(AchievementType.MENTOR, "Mentor", "25 principiantes ayudados", 25);
            createAchievement(AchievementType.HOST, "Anfitrión", "10 intercambios creados", 10);
            
            System.out.println("✅ " + achievementRepository.count() + " logros creados correctamente");
        } else {
            System.out.println("🏆 Logros ya inicializados: " + achievementRepository.count());
        }
    }

    private void createAchievement(AchievementType type, String title, String description, int targetProgress) {
        Achievement achievement = new Achievement();
        achievement.setType(type);
        achievement.setTitle(title);
        achievement.setDescription(description);
        achievement.setTargetProgress(targetProgress);
        achievementRepository.save(achievement);
    }
}
