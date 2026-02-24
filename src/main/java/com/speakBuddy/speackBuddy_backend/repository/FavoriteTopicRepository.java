package com.speakBuddy.speackBuddy_backend.repository;

import com.speakBuddy.speackBuddy_backend.models.FavoriteTopic;
import com.speakBuddy.speackBuddy_backend.models.TopicCategory;
import com.speakBuddy.speackBuddy_backend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteTopicRepository extends JpaRepository<FavoriteTopic, Long> {

    List<FavoriteTopic> findByUserOrderBySavedAtDesc(User user);

    List<FavoriteTopic> findByUserAndCategoryOrderBySavedAtDesc(User user, TopicCategory category);

    Optional<FavoriteTopic> findByIdAndUser(Long id, User user);

    boolean existsByIdAndUser(Long id, User user);
}
