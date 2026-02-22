package com.speakBuddy.speackBuddy_backend.service;

import com.speakBuddy.speackBuddy_backend.models.User;
import com.speakBuddy.speackBuddy_backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class ExperienceService {

    private static final int MAX_LEVEL = 100;
    private static final int DAILY_BONUS_XP = 5;
    private static final int XP_PER_MINUTE = 2;

    private final UserRepository userRepository;

    @Autowired
    public ExperienceService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Calcula el XP requerido para subir desde un nivel dado al siguiente.
     * Progresión escalonada por rangos:
     * - Niveles 1-10: 100 XP
     * - Niveles 11-25: 200 XP
     * - Niveles 26-50: 400 XP
     * - Niveles 51-75: 600 XP
     * - Niveles 76-100: 800 XP
     */
    public long getXpRequiredForLevel(int level) {
        if (level <= 10) return 100L;
        if (level <= 25) return 200L;
        if (level <= 50) return 400L;
        if (level <= 75) return 600L;
        return 800L;
    }

    /**
     * Calcula el multiplicador de racha.
     * +10% por cada día de racha, máximo x2.0 (10 días).
     */
    public double getStreakMultiplier(int streakDays) {
        return Math.min(1.0 + (streakDays * 0.10), 2.0);
    }

    /**
     * Añade XP al usuario aplicando el multiplicador de racha.
     * Sube de nivel automáticamente si corresponde.
     */
    @Transactional
    public void addExperience(User user, long baseXp) {
        double multiplier = getStreakMultiplier(
                user.getCurrentStreakDays() != null ? user.getCurrentStreakDays() : 0
        );
        long finalXp = (long) Math.ceil(baseXp * multiplier);

        long currentXp = user.getExperiencePoints() != null ? user.getExperiencePoints() : 0L;
        user.setExperiencePoints(currentXp + finalXp);

        int currentLevel = user.getLevel() != null ? user.getLevel() : 1;

        while (currentLevel < MAX_LEVEL) {
            long required = getXpRequiredForLevel(currentLevel);
            if (user.getExperiencePoints() >= required) {
                user.setExperiencePoints(user.getExperiencePoints() - required);
                currentLevel++;
                user.setLevel(currentLevel);
            } else {
                break;
            }
        }

        if (currentLevel >= MAX_LEVEL) {
            user.setLevel(MAX_LEVEL);
        }

        userRepository.save(user);
    }

    /**
     * Añade XP por un intercambio completado.
     * XP = duración en minutos × 2
     */
    @Transactional
    public void addExperienceForExchange(User user, int durationMinutes) {
        long baseXp = (long) durationMinutes * XP_PER_MINUTE;
        addExperience(user, baseXp);
    }

    /**
     * Registra actividad diaria y actualiza la racha.
     * Debe llamarse cuando el usuario accede a la app.
     */
    @Transactional
    public void recordDailyActivity(User user) {
        LocalDate today = LocalDate.now();
        LocalDate lastActivity = user.getLastActivityDate();

        int currentStreak = user.getCurrentStreakDays() != null ? user.getCurrentStreakDays() : 0;
        int bestStreak = user.getBestStreakDays() != null ? user.getBestStreakDays() : 0;

        if (lastActivity == null) {
            currentStreak = 1;
        } else if (lastActivity.equals(today)) {
            return;
        } else if (lastActivity.equals(today.minusDays(1))) {
            currentStreak++;
        } else {
            currentStreak = 1;
        }

        if (currentStreak > bestStreak) {
            bestStreak = currentStreak;
        }

        user.setLastActivityDate(today);
        user.setCurrentStreakDays(currentStreak);
        user.setBestStreakDays(bestStreak);

        userRepository.save(user);
    }

    /**
     * Otorga el bonus diario de XP.
     * Solo se puede reclamar una vez al día.
     * @return true si se otorgó el bonus, false si ya fue reclamado hoy
     */
    @Transactional
    public boolean claimDailyBonus(User user) {
        LocalDate today = LocalDate.now();
        LocalDate lastBonusDate = user.getLastDailyBonusDate();

        if (lastBonusDate != null && lastBonusDate.equals(today)) {
            return false;
        }

        recordDailyActivity(user);

        addExperience(user, DAILY_BONUS_XP);

        user.setLastDailyBonusDate(today);
        userRepository.save(user);

        return true;
    }

    /**
     * Verifica si el usuario puede reclamar el bonus diario.
     */
    public boolean canClaimDailyBonus(User user) {
        LocalDate today = LocalDate.now();
        LocalDate lastBonusDate = user.getLastDailyBonusDate();
        return lastBonusDate == null || !lastBonusDate.equals(today);
    }

    /**
     * Calcula el porcentaje de progreso hacia el siguiente nivel.
     */
    public double getProgressPercentage(User user) {
        if (user.getLevel() >= MAX_LEVEL) {
            return 1.0;
        }
        long required = getXpRequiredForLevel(user.getLevel());
        long currentXp = user.getExperiencePoints() != null ? user.getExperiencePoints() : 0L;
        return Math.min((double) currentXp / required, 1.0);
    }
}
