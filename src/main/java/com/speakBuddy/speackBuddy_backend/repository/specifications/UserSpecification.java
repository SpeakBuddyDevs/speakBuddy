package com.speakBuddy.speackBuddy_backend.repository.specifications;

import com.speakBuddy.speackBuddy_backend.models.User;
import com.speakBuddy.speackBuddy_backend.models.UserLanguagesLearning;
import com.speakBuddy.speackBuddy_backend.models.Language;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class UserSpecification {

    public static Specification<User> withFilters(String nativeLangIso, String learningLangIso) {
        return (root, query, criteriaBuilder) -> {

            query.distinct(true);

            var predicate = criteriaBuilder.conjunction();

            // --- FILTRO 1: Idioma Nativo ---
            if (StringUtils.hasText(nativeLangIso)) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(
                                root.get("nativeLanguage").get("isoCode"),
                                nativeLangIso
                        )
                );
            }

            // --- FILTRO 2: Idioma que aprende ---
            if (StringUtils.hasText(learningLangIso)) {
                Join<User, UserLanguagesLearning> learningJoin = root.join("languagesToLearn", JoinType.INNER);
                Join<UserLanguagesLearning, Language> languageJoin = learningJoin.join("language", JoinType.INNER);

                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(
                                languageJoin.get("isoCode"),
                                learningLangIso
                        )
                );
            }

            return predicate;
        };
    }
}