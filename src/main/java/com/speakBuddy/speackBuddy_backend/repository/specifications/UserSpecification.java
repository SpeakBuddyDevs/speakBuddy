package com.speakBuddy.speackBuddy_backend.repository.specifications;

import com.speakBuddy.speackBuddy_backend.models.User;
import com.speakBuddy.speackBuddy_backend.models.UserLanguagesLearning;
import com.speakBuddy.speackBuddy_backend.models.Language;
import com.speakBuddy.speackBuddy_backend.security.Role;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class UserSpecification {

    public static Specification<User> withFilters(String searchText, String nativeLangIso, String learningLangIso, String country, Boolean proOnly, Double minRating) {
        return (root, query, criteriaBuilder) -> {

            query.distinct(true);

            var predicate = criteriaBuilder.conjunction();

            // --- FILTRO 0: Búsqueda por texto (name, surname, username) ---
            if (StringUtils.hasText(searchText)) {
                String trimmed = searchText.trim();
                String pattern = "%" + trimmed.toLowerCase() + "%";

                Predicate nameMatch = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        pattern
                );
                Predicate surnameMatch = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("surname")),
                        pattern
                );
                Predicate usernameMatch = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("username")),
                        pattern
                );

                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.or(nameMatch, surnameMatch, usernameMatch)
                );
            }

            // --- FILTRO 1: Idioma Nativo (case-insensitive) ---
            if (StringUtils.hasText(nativeLangIso)) {
                String isoLower = nativeLangIso.trim().toLowerCase();
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(
                                criteriaBuilder.lower(root.get("nativeLanguage").get("isoCode")),
                                isoLower
                        )
                );
            }

            // --- FILTRO 2: Idioma que aprende (case-insensitive) ---
            if (StringUtils.hasText(learningLangIso)) {
                String isoLower = learningLangIso.trim().toLowerCase();
                Join<User, UserLanguagesLearning> learningJoin = root.join("languagesToLearn", JoinType.INNER);
                Join<UserLanguagesLearning, Language> languageJoin = learningJoin.join("language", JoinType.INNER);

                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(
                                criteriaBuilder.lower(languageJoin.get("isoCode")),
                                isoLower
                        )
                );
            }

            // --- FILTRO 3: País ---
            if (StringUtils.hasText(country)) {
                String countryTrimmed = country.trim();
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(root.get("country"), countryTrimmed)
                );
            }

            // --- FILTRO 4: Solo PRO (role = ROLE_PREMIUM) ---
            if (Boolean.TRUE.equals(proOnly)) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(root.get("role"), Role.ROLE_PREMIUM)
                );
            }

            // --- FILTRO 5: Rating mínimo ---
            if (minRating != null && minRating > 0) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.greaterThanOrEqualTo(root.get("averageRating"), minRating)
                );
            }

            return predicate;
        };
    }
}