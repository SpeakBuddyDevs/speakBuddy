package com.speakBuddy.speackBuddy_backend.repository;

import com.speakBuddy.speackBuddy_backend.models.LanguageLevel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LanguageLevelRepository extends JpaRepository<LanguageLevel, Long> {

    Optional<LanguageLevel> findByLevelOrder(Integer levelOrder);
}
