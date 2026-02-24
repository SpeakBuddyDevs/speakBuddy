package com.speakBuddy.speackBuddy_backend.service;

import com.speakBuddy.speackBuddy_backend.models.*;
import com.speakBuddy.speackBuddy_backend.repository.LanguageLevelRepository;
import com.speakBuddy.speackBuddy_backend.repository.LanguageRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Encapsula la lógica de elegibilidad de intercambios:
 * rangos de nivel, resolución de idiomas y cálculo de requisitos no cumplidos.
 */
@Service
public class ExchangeEligibilityService {

    static final int MIN_LEVEL_PRINCIPIANTE = 1;
    static final int MIN_LEVEL_INTERMEDIO = 4;
    static final int MIN_LEVEL_AVANZADO = 5;
    static final int LEVEL_ORDER_MIN = 1;
    static final int LEVEL_ORDER_MAX = 6;

    private final LanguageRepository languageRepository;
    private final LanguageLevelRepository languageLevelRepository;

    public ExchangeEligibilityService(LanguageRepository languageRepository,
                                      LanguageLevelRepository languageLevelRepository) {
        this.languageRepository = languageRepository;
        this.languageLevelRepository = languageLevelRepository;
    }

    public int effectiveMinOrder(Exchange exchange) {
        if (exchange.getRequiredLevelMinOrder() != null) return exchange.getRequiredLevelMinOrder();
        return requiredLevelToMinLevel(exchange.getRequiredLevel());
    }

    public int effectiveMaxOrder(Exchange exchange) {
        if (exchange.getRequiredLevelMaxOrder() != null) return exchange.getRequiredLevelMaxOrder();
        return LEVEL_ORDER_MAX;
    }

    public String buildLevelRangeLabel(int minOrder, int maxOrder) {
        String minName = languageLevelRepository.findByLevelOrder(minOrder)
                .map(LanguageLevel::getName)
                .orElse("A1");
        String maxName = languageLevelRepository.findByLevelOrder(maxOrder)
                .map(LanguageLevel::getName)
                .orElse("C2");
        if (minOrder == maxOrder) return minName;
        return minName + " – " + maxName;
    }

    public int requiredLevelToMinLevel(String requiredLevel) {
        if (requiredLevel == null) return MIN_LEVEL_PRINCIPIANTE;
        return switch (requiredLevel.trim().toLowerCase()) {
            case "intermedio" -> MIN_LEVEL_INTERMEDIO;
            case "avanzado" -> MIN_LEVEL_AVANZADO;
            default -> MIN_LEVEL_PRINCIPIANTE;
        };
    }

    public String resolveLanguageName(String isoCode) {
        if (isoCode == null || isoCode.isBlank()) return null;
        return languageRepository.findByIsoCode(isoCode.trim())
                .map(Language::getName)
                .orElse(isoCode);
    }

    public record EligibilityResult(boolean isEligible, List<String> unmetRequirements) {}

    public EligibilityResult computeEligibility(Exchange exchange, User currentUser,
                                                int minOrder, int maxOrder,
                                                String targetLanguageName, String nativeLanguageName) {
        if (currentUser == null) {
            return new EligibilityResult(false, List.of("Inicia sesión para unirte"));
        }

        List<String> unmet = new ArrayList<>();

        String userNativeIso = currentUser.getNativeLanguage() != null
                ? currentUser.getNativeLanguage().getIsoCode()
                : null;
        String targetIso = exchange.getTargetLanguageCode() != null
                ? exchange.getTargetLanguageCode().trim().toLowerCase()
                : null;

        if (targetIso != null && (userNativeIso == null || !userNativeIso.trim().equalsIgnoreCase(targetIso))) {
            unmet.add("Idioma nativo: " + (targetLanguageName != null ? targetLanguageName : targetIso));
        }

        String nativeIso = exchange.getNativeLanguageCode() != null
                ? exchange.getNativeLanguageCode().trim().toLowerCase()
                : null;
        String levelRangeLabel = buildLevelRangeLabel(minOrder, maxOrder);
        if (nativeIso != null) {
            Optional<UserLanguagesLearning> learning = currentUser.getLanguagesToLearn().stream()
                    .filter(l -> l.getLanguage() != null
                            && nativeIso.equals(l.getLanguage().getIsoCode().toLowerCase()))
                    .findFirst();
            if (learning.isEmpty()) {
                unmet.add("Nivel de " + (nativeLanguageName != null ? nativeLanguageName : nativeIso) + ": " + levelRangeLabel);
            } else {
                int userLevelOrder = learning.get().getLevel() != null
                        ? learning.get().getLevel().getLevelOrder()
                        : 0;
                if (userLevelOrder < minOrder || userLevelOrder > maxOrder) {
                    unmet.add("Nivel de " + (nativeLanguageName != null ? nativeLanguageName : nativeIso) + ": " + levelRangeLabel);
                }
            }
        }

        return new EligibilityResult(unmet.isEmpty(), unmet.isEmpty() ? null : unmet);
    }
}
