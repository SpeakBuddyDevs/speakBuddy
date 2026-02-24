package com.speakBuddy.speackBuddy_backend.service;

import com.speakBuddy.speackBuddy_backend.dto.AddLearningLanguageDTO;
import com.speakBuddy.speackBuddy_backend.dto.ProfileResponseDTO;
import com.speakBuddy.speackBuddy_backend.dto.UpdateLearningLevelDTO;
import com.speakBuddy.speackBuddy_backend.dto.UpdateNativeLanguageDTO;
import com.speakBuddy.speackBuddy_backend.exception.AlreadyLearningLanguageException;
import com.speakBuddy.speackBuddy_backend.exception.ResourceNotFoundException;
import com.speakBuddy.speackBuddy_backend.models.Language;
import com.speakBuddy.speackBuddy_backend.models.LanguageLevel;
import com.speakBuddy.speackBuddy_backend.models.User;
import com.speakBuddy.speackBuddy_backend.models.UserLanguagesLearning;
import com.speakBuddy.speackBuddy_backend.repository.LanguageLevelRepository;
import com.speakBuddy.speackBuddy_backend.repository.LanguageRepository;
import com.speakBuddy.speackBuddy_backend.repository.UserLanguageLearningRepository;
import com.speakBuddy.speackBuddy_backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

/**
 * Gestión de idiomas de aprendizaje y idioma nativo del usuario.
 * Unifica las variantes por ID y por código ISO.
 */
@Service
public class UserLanguageService {

    private final UserRepository userRepository;
    private final LanguageRepository languageRepository;
    private final LanguageLevelRepository languageLevelRepository;
    private final UserLanguageLearningRepository userLanguageLearningRepository;
    private final UserProfileMapper profileMapper;

    public UserLanguageService(UserRepository userRepository,
                               LanguageRepository languageRepository,
                               LanguageLevelRepository languageLevelRepository,
                               UserLanguageLearningRepository userLanguageLearningRepository,
                               UserProfileMapper profileMapper) {
        this.userRepository = userRepository;
        this.languageRepository = languageRepository;
        this.languageLevelRepository = languageLevelRepository;
        this.userLanguageLearningRepository = userLanguageLearningRepository;
        this.profileMapper = profileMapper;
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + userId));
    }

    public ProfileResponseDTO addLearningLanguage(Long userId, AddLearningLanguageDTO addDTO) {
        if (addDTO.getLanguageId() == null || addDTO.getLevelId() == null) {
            throw new IllegalArgumentException("languageId and levelId are required");
        }

        User user = findUser(userId);
        Language language = languageRepository.findById(addDTO.getLanguageId())
                .orElseThrow(() -> new ResourceNotFoundException("Language not found"));
        LanguageLevel level = languageLevelRepository.findById(addDTO.getLevelId())
                .orElseThrow(() -> new ResourceNotFoundException("Language level not found"));

        boolean alreadyLearning = user.getLanguagesToLearn().stream()
                .anyMatch(l -> l.getLanguage().getId().equals(language.getId()));
        if (alreadyLearning) {
            throw new AlreadyLearningLanguageException("User is already learning this language");
        }

        UserLanguagesLearning newLearning = new UserLanguagesLearning();
        newLearning.setUser(user);
        newLearning.setLanguage(language);
        newLearning.setLevel(level);
        user.getLanguagesToLearn().add(newLearning);

        return profileMapper.toProfileResponseDTO(userRepository.save(user));
    }

    public ProfileResponseDTO deleteLearningLanguage(Long userId, Long learningId) {
        User user = findUser(userId);
        UserLanguagesLearning learning = userLanguageLearningRepository.findById(learningId)
                .orElseThrow(() -> new ResourceNotFoundException("Relación de aprendizaje no encontrada con ID: " + learningId));

        if (!learning.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Acceso denegado. La relación no pertenece al usuario.");
        }

        user.getLanguagesToLearn().remove(learning);
        return profileMapper.toProfileResponseDTO(userRepository.save(user));
    }

    @Transactional
    public void deleteLearningLanguageByCode(Long userId, String languageCode) {
        User user = findUser(userId);
        UserLanguagesLearning learning = findLearningByCode(userId, languageCode);
        user.getLanguagesToLearn().remove(learning);
        userRepository.save(user);
    }

    public ProfileResponseDTO updateNativeLanguage(Long userId, UpdateNativeLanguageDTO dto) {
        User user = findUser(userId);
        Language newNativeLanguage = languageRepository.findById(dto.getNewNativeLanguageId())
                .orElseThrow(() -> new ResourceNotFoundException("Language not found"));
        user.setNativeLanguage(newNativeLanguage);
        return profileMapper.toProfileResponseDTO(userRepository.save(user));
    }

    public ProfileResponseDTO updateLearningLevel(Long userId, Long learningId, UpdateLearningLevelDTO dto) {
        LanguageLevel newLevel = languageLevelRepository.findById(dto.getNewLevelId())
                .orElseThrow(() -> new ResourceNotFoundException("Nivel de idioma no encontrado con ID: " + dto.getNewLevelId()));

        UserLanguagesLearning learning = userLanguageLearningRepository.findById(learningId)
                .orElseThrow(() -> new ResourceNotFoundException("Relación de aprendizaje no encontrada con ID: " + learningId));

        if (!learning.getUser().getId().equals(userId)) {
            throw new RuntimeException("Acceso denegado. La relación no pertenece al usuario.");
        }

        learning.setLevel(newLevel);
        userLanguageLearningRepository.save(learning);

        return profileMapper.toProfileResponseDTO(findUser(userId));
    }

    @Transactional
    public void updateLearningLevelByCode(Long userId, String languageCode, Long newLevelId) {
        LanguageLevel newLevel = languageLevelRepository.findById(newLevelId)
                .orElseThrow(() -> new ResourceNotFoundException("Nivel de idioma no encontrado con ID: " + newLevelId));

        UserLanguagesLearning learning = findLearningByCode(userId, languageCode);
        learning.setLevel(newLevel);
        userLanguageLearningRepository.save(learning);
    }

    @Transactional
    public void setLearningLanguageActive(Long userId, String languageCode) {
        userLanguageLearningRepository.deactivateAllForUser(userId);
        UserLanguagesLearning target = findLearningByCode(userId, languageCode);
        target.setActive(true);
        userLanguageLearningRepository.save(target);
    }

    @Transactional
    public void setLearningLanguageInactive(Long userId, String languageCode) {
        UserLanguagesLearning target = findLearningByCode(userId, languageCode);
        target.setActive(false);
        userLanguageLearningRepository.save(target);
    }

    private UserLanguagesLearning findLearningByCode(Long userId, String languageCode) {
        return userLanguageLearningRepository
                .findByUserIdAndLanguageIsoCode(userId, languageCode.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "El usuario no está aprendiendo el idioma con código: " + languageCode));
    }
}
