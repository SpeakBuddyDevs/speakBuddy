package com.speakBuddy.speackBuddy_backend.repository;

import com.speakBuddy.speackBuddy_backend.models.LanguageLevel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LanguageLevelRepository extends JpaRepository<LanguageLevel,Long> {
    // findById() nos lo regala JpaRepository
}
