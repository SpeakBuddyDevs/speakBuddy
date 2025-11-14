package com.speakBuddy.speackBuddy_backend.repository;

import com.speakBuddy.speackBuddy_backend.models.UserLanguagesLearning;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserLanguageLearningRepository extends JpaRepository<UserLanguagesLearning,Long> {
    // posibles métodos que añadir a futuro
}
