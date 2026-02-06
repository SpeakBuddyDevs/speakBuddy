package com.speakBuddy.speackBuddy_backend.repository.specifications;

import com.speakBuddy.speackBuddy_backend.models.Exchange;
import com.speakBuddy.speackBuddy_backend.models.ExchangeStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * Specification para filtrar intercambios públicos.
 */
public class ExchangeSpecification {

    public static Specification<Exchange> publicExchangesWithFilters(
            String q,
            String requiredLevel,
            LocalDateTime minDate,
            Integer maxDuration,
            String nativeLang,
            String targetLang) {

        return (root, query, criteriaBuilder) -> {

            var predicate = criteriaBuilder.conjunction();

            // Solo intercambios públicos
            predicate = criteriaBuilder.and(predicate,
                    criteriaBuilder.isTrue(root.get("isPublic")));

            // Solo programados (no cancelados ni completados)
            predicate = criteriaBuilder.and(predicate,
                    criteriaBuilder.equal(root.get("status"), ExchangeStatus.SCHEDULED));

            // Fecha futura o actual
            predicate = criteriaBuilder.and(predicate,
                    criteriaBuilder.greaterThanOrEqualTo(root.get("scheduledAt"), LocalDateTime.now()));

            // Búsqueda por texto (título, descripción)
            if (StringUtils.hasText(q)) {
                String pattern = "%" + q.trim().toLowerCase() + "%";
                Predicate titleMatch = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("title")),
                        pattern
                );
                Predicate descMatch = criteriaBuilder.and(
                        criteriaBuilder.isNotNull(root.get("description")),
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("description")),
                                pattern
                        )
                );
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.or(titleMatch, descMatch));
            }

            // Nivel requerido
            if (StringUtils.hasText(requiredLevel)) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(root.get("requiredLevel"), requiredLevel.trim()));
            }

            // Fecha mínima
            if (minDate != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.greaterThanOrEqualTo(root.get("scheduledAt"), minDate));
            }

            // Duración máxima (minutos)
            if (maxDuration != null && maxDuration > 0) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.lessThanOrEqualTo(root.get("durationMinutes"), maxDuration));
            }

            // Idioma nativo (el que el creador ofrece)
            if (StringUtils.hasText(nativeLang)) {
                String isoLower = nativeLang.trim().toLowerCase();
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(
                                criteriaBuilder.lower(root.get("nativeLanguageCode")),
                                isoLower
                        ));
            }

            // Idioma objetivo (el que el creador quiere practicar)
            if (StringUtils.hasText(targetLang)) {
                String isoLower = targetLang.trim().toLowerCase();
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(
                                criteriaBuilder.lower(root.get("targetLanguageCode")),
                                isoLower
                        ));
            }

            return predicate;
        };
    }
}
