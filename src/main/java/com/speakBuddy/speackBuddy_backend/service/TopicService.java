package com.speakBuddy.speackBuddy_backend.service;

import com.speakBuddy.speackBuddy_backend.dto.GeneratedTopicResponseDTO;
import com.speakBuddy.speackBuddy_backend.dto.SaveFavoriteTopicRequestDTO;
import com.speakBuddy.speackBuddy_backend.models.FavoriteTopic;
import com.speakBuddy.speackBuddy_backend.models.TopicCategory;
import com.speakBuddy.speackBuddy_backend.models.User;
import com.speakBuddy.speackBuddy_backend.repository.FavoriteTopicRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TopicService {

    private final TopicGeneratorService generatorService;
    private final FavoriteTopicRepository favoriteRepository;

    @Autowired
    public TopicService(TopicGeneratorService generatorService, FavoriteTopicRepository favoriteRepository) {
        this.generatorService = generatorService;
        this.favoriteRepository = favoriteRepository;
    }

    public GeneratedTopicResponseDTO generate(TopicCategory category, String level, String languageCode) {
        return generatorService.generate(category, level, languageCode);
    }

    public List<GeneratedTopicResponseDTO> getFavorites(User user) {
        return favoriteRepository.findByUserOrderBySavedAtDesc(user)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public GeneratedTopicResponseDTO addToFavorites(User user, SaveFavoriteTopicRequestDTO request) {
        TopicCategory category = TopicCategory.valueOf(request.getCategory().toUpperCase());

        FavoriteTopic topic = FavoriteTopic.builder()
                .user(user)
                .category(category)
                .level(request.getLevel())
                .mainText(request.getMainText())
                .positionA(request.getPositionA())
                .positionB(request.getPositionB())
                .suggestedVocabulary(request.getSuggestedVocabulary() != null ?
                        request.getSuggestedVocabulary() : List.of())
                .language(request.getLanguage())
                .generatedAt(request.getGeneratedAt())
                .build();

        FavoriteTopic saved = favoriteRepository.save(topic);
        return toDTO(saved);
    }

    @Transactional
    public void removeFromFavorites(User user, Long topicId) {
        FavoriteTopic topic = favoriteRepository.findByIdAndUser(topicId, user)
                .orElseThrow(() -> new EntityNotFoundException("Tema favorito no encontrado"));

        favoriteRepository.delete(topic);
    }

    private GeneratedTopicResponseDTO toDTO(FavoriteTopic topic) {
        return GeneratedTopicResponseDTO.builder()
                .id(topic.getId())
                .category(topic.getCategory().name())
                .level(topic.getLevel())
                .mainText(topic.getMainText())
                .positionA(topic.getPositionA())
                .positionB(topic.getPositionB())
                .suggestedVocabulary(topic.getSuggestedVocabulary())
                .language(topic.getLanguage())
                .generatedAt(topic.getGeneratedAt())
                .isFavorite(true)
                .build();
    }
}
