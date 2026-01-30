package com.speakBuddy.speackBuddy_backend.repository;

import com.speakBuddy.speackBuddy_backend.models.UserLanguagesLearning;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserLanguageLearningRepository extends JpaRepository<UserLanguagesLearning,Long> {
    @Query("SELECT ull FROM UserLanguagesLearning ull WHERE ull.user.id = :userId AND ull.language.isoCode = :isoCode")
    Optional<UserLanguagesLearning> findByUserIdAndLanguageIsoCode(@Param("userId") Long userId, @Param("isoCode") String isoCode);

    @Modifying
    @Query("UPDATE UserLanguagesLearning ull SET ull.active = false WHERE ull.user.id = :userId")
    void deactivateAllForUser(@Param("userId") Long userId);
}
