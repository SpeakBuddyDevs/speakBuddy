package com.speakBuddy.speackBuddy_backend.service;

import com.speakBuddy.speackBuddy_backend.dto.*;
import com.speakBuddy.speackBuddy_backend.models.Language;
import com.speakBuddy.speackBuddy_backend.models.User;
import com.speakBuddy.speackBuddy_backend.models.UserLanguagesLearning;
import com.speakBuddy.speackBuddy_backend.security.Role;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapeo de entidades User a los distintos DTOs de perfil/resumen.
 */
@Component
public class UserProfileMapper {

    private final ExperienceService experienceService;

    public UserProfileMapper(ExperienceService experienceService) {
        this.experienceService = experienceService;
    }

    public ProfileResponseDTO toProfileResponseDTO(User user) {
        ProfileResponseDTO dto = new ProfileResponseDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setUsername(user.getUsername());
        dto.setName(user.getName());
        dto.setSurname(user.getSurname());
        dto.setProfilePictureURL(user.getProfilePicture());
        dto.setLevel(user.getLevel());
        dto.setExperiencePoints(user.getExperiencePoints());
        dto.setCountry(user.getCountry());
        dto.setDescription(user.getDescription());
        dto.setAverageRating(user.getAverageRating() != null ? user.getAverageRating() : 0.0);
        dto.setCompletedExchanges(user.getCompletedExchanges() != null ? user.getCompletedExchanges() : 0);
        dto.setIsPro(user.getRole() != null && user.getRole() == Role.ROLE_PREMIUM);

        long threshold = user.getLevel() * 100L;
        dto.setXpToNextLevel(threshold);
        if (threshold > 0) {
            double percentage = (double) user.getExperiencePoints() / threshold;
            dto.setProgressPercentage(Math.min(percentage, 1.0));
        } else {
            dto.setProgressPercentage(0.0);
        }

        if (user.getNativeLanguage() != null) {
            dto.setNativeLanguage(toLanguageDTO(user.getNativeLanguage()));
        }

        Set<LearningLanguageDTO> learningDTOs = user.getLanguagesToLearn().stream()
                .map(this::toLearningLanguageDTO)
                .collect(Collectors.toSet());
        dto.setLanguagesToLearn(learningDTOs);

        return dto;
    }

    public UserSummaryDTO toSummaryDTO(User user) {
        UserSummaryDTO dto = new UserSummaryDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setProfilePicture(user.getProfilePicture());
        dto.setCountry(user.getCountry());

        if (user.getNativeLanguage() != null) {
            dto.setNativeLanguage(user.getNativeLanguage().getName());
            dto.setNativeLanguageCode(user.getNativeLanguage().getIsoCode());
        }

        List<UserSummaryDTO.LearningSummaryDTO> learningList = user.getLanguagesToLearn().stream()
                .map(l -> {
                    UserSummaryDTO.LearningSummaryDTO lDto = new UserSummaryDTO.LearningSummaryDTO();
                    lDto.setLanguageName(l.getLanguage().getName());
                    lDto.setLanguageCode(l.getLanguage().getIsoCode());
                    lDto.setLevel(l.getLevel().getName());
                    return lDto;
                })
                .collect(Collectors.toList());

        dto.setLanguagesToLearn(learningList);
        dto.setIsPro(user.getRole() != null && user.getRole() == Role.ROLE_PREMIUM);
        dto.setLevel(user.getLevel() != null ? user.getLevel() : 1);
        dto.setAverageRating(user.getAverageRating() != null ? user.getAverageRating() : 0.0);
        dto.setTotalReviews(user.getTotalReviews() != null ? user.getTotalReviews() : 0);
        dto.setExchanges(user.getCompletedExchanges() != null ? user.getCompletedExchanges() : 0);
        return dto;
    }

    public UserProfileDTO toUserProfileDTO(User user) {
        List<UserProfileDTO.LearningLanguageDTO> learningDTOs = user.getLanguagesToLearn().stream()
                .map(l -> UserProfileDTO.LearningLanguageDTO.builder()
                        .code(l.getLanguage().getIsoCode())
                        .name(l.getLanguage().getName())
                        .level(l.getLevel().getName())
                        .active(l.isActive())
                        .build())
                .toList();

        int currentLevel = user.getLevel() != null ? user.getLevel() : 1;
        long currentXp = user.getExperiencePoints() != null ? user.getExperiencePoints() : 0L;
        long xpToNextLevel = experienceService.getXpRequiredForLevel(currentLevel);
        double progressPct = experienceService.getProgressPercentage(user);
        int streakDays = user.getCurrentStreakDays() != null ? user.getCurrentStreakDays() : 0;
        double streakMultiplier = experienceService.getStreakMultiplier(streakDays);
        boolean canClaimDailyBonus = experienceService.canClaimDailyBonus(user);

        return UserProfileDTO.builder()
                .id(user.getId())
                .name(user.getName() + " " + user.getSurname())
                .email(user.getEmail())
                .country(user.getCountry() != null ? user.getCountry() : "")
                .nativeLanguage(user.getNativeLanguage() != null ? user.getNativeLanguage().getIsoCode() : "ES")
                .rating(user.getAverageRating())
                .exchanges(user.getCompletedExchanges() != null ? user.getCompletedExchanges() : 0)
                .learningLanguages(learningDTOs)
                .level(currentLevel)
                .experiencePoints(currentXp)
                .xpToNextLevel(xpToNextLevel)
                .progressPct(progressPct)
                .streakMultiplier(streakMultiplier)
                .canClaimDailyBonus(canClaimDailyBonus)
                .languagesCount(learningDTOs.size())
                .hoursTotal(user.getTotalExchangeMinutes() != null
                        ? user.getTotalExchangeMinutes() / 60.0
                        : 0.0)
                .currentStreakDays(streakDays)
                .bestStreakDays(user.getBestStreakDays() != null ? user.getBestStreakDays() : 0)
                .medals(0)
                .isPro(user.getRole() != null && user.getRole() == Role.ROLE_PREMIUM)
                .avatarUrl(user.getProfilePicture())
                .description(user.getDescription() != null ? user.getDescription() : "")
                .build();
    }

    private LanguageDTO toLanguageDTO(Language language) {
        LanguageDTO dto = new LanguageDTO();
        dto.setId(language.getId());
        dto.setName(language.getName());
        dto.setIsoCode(language.getIsoCode());
        return dto;
    }

    private LearningLanguageDTO toLearningLanguageDTO(UserLanguagesLearning learning) {
        return LearningLanguageDTO.builder()
                .language(toLanguageDTO(learning.getLanguage()))
                .levelName(learning.getLevel().getName())
                .active(learning.isActive())
                .build();
    }
}
