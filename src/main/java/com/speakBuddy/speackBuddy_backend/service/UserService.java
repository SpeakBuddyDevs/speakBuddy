package com.speakBuddy.speackBuddy_backend.service;

import com.speakBuddy.speackBuddy_backend.dto.*;
import com.speakBuddy.speackBuddy_backend.exception.EmailAlreadyExistsException;
import com.speakBuddy.speackBuddy_backend.exception.ResourceNotFoundException;
import com.speakBuddy.speackBuddy_backend.models.Language;
import com.speakBuddy.speackBuddy_backend.models.LanguageLevel;
import com.speakBuddy.speackBuddy_backend.models.User;
import com.speakBuddy.speackBuddy_backend.models.UserLanguagesLearning;
import com.speakBuddy.speackBuddy_backend.repository.LanguageLevelRepository;
import com.speakBuddy.speackBuddy_backend.repository.LanguageRepository;
import com.speakBuddy.speackBuddy_backend.repository.UserRepository;
import com.speakBuddy.speackBuddy_backend.repository.specifications.UserSpecification;
import com.speakBuddy.speackBuddy_backend.security.Role;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Casos de uso de alto nivel sobre usuarios: registro, perfil, búsqueda.
 * Delega la gestión de idiomas a {@link UserLanguageService} y
 * el mapeo a DTOs a {@link UserProfileMapper}.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final LanguageRepository languageRepository;
    private final LanguageLevelRepository languageLevelRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserProfileMapper profileMapper;
    private final UserLanguageService languageService;

    public UserService(UserRepository userRepository,
                       LanguageRepository languageRepository,
                       LanguageLevelRepository languageLevelRepository,
                       PasswordEncoder passwordEncoder,
                       UserProfileMapper profileMapper,
                       UserLanguageService languageService) {
        this.userRepository = userRepository;
        this.languageRepository = languageRepository;
        this.languageLevelRepository = languageLevelRepository;
        this.passwordEncoder = passwordEncoder;
        this.profileMapper = profileMapper;
        this.languageService = languageService;
    }

    public User registerUser(RegisterRequestDTO request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("El email ya está registrado");
        }

        Language nativeLanguage = languageRepository.findById(request.getNativeLanguageId())
                .orElseThrow(() -> new ResourceNotFoundException("Idioma nativo no encontrado"));

        String publicUsername = request.getName() + " " + request.getSurname();

        User newUser = new User();
        newUser.setEmail(request.getEmail());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setName(request.getName());
        newUser.setSurname(request.getSurname());
        newUser.setUsername(publicUsername);
        newUser.setCountry(request.getCountry() != null ? request.getCountry().trim() : null);
        newUser.setNativeLanguage(nativeLanguage);
        newUser.setLevel(1);
        newUser.setExperiencePoints(0L);
        newUser.setRole(Role.ROLE_USER);

        if (request.getLearningLanguageId() != null && request.getLearningLanguageId() > 0) {
            Language learningLanguage = languageRepository.findById(request.getLearningLanguageId())
                    .orElseThrow(() -> new ResourceNotFoundException("Idioma de aprendizaje no encontrado"));
            LanguageLevel defaultLevel = languageLevelRepository.findById(1L)
                    .orElseThrow(() -> new ResourceNotFoundException("Nivel de idioma no encontrado"));

            UserLanguagesLearning learning = new UserLanguagesLearning();
            learning.setUser(newUser);
            learning.setLanguage(learningLanguage);
            learning.setLevel(defaultLevel);
            learning.setActive(true);
            newUser.getLanguagesToLearn().add(learning);
        }

        return userRepository.save(newUser);
    }

    public ProfileResponseDTO getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return profileMapper.toProfileResponseDTO(user);
    }

    public ProfileResponseDTO updateProfile(Long userId, ProfileUpdateDTO updateDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (updateDTO.getName() != null) user.setName(updateDTO.getName());
        if (updateDTO.getSurname() != null) user.setSurname(updateDTO.getSurname());
        if (updateDTO.getProfilePictureUrl() != null) user.setProfilePicture(updateDTO.getProfilePictureUrl());
        if (updateDTO.getDescription() != null) user.setDescription(updateDTO.getDescription());

        if (updateDTO.getName() != null || updateDTO.getSurname() != null) {
            user.setUsername(user.getName() + " " + user.getSurname());
        }

        return profileMapper.toProfileResponseDTO(userRepository.save(user));
    }

    public Page<UserSummaryDTO> searchUsers(String query, String nativeLang, String learningLang,
                                            String country, Boolean proOnly, Double minRating,
                                            Pageable pageable) {
        Specification<User> spec = UserSpecification.withFilters(query, nativeLang, learningLang, country, proOnly, minRating);
        return userRepository.findAll(spec, pageable).map(profileMapper::toSummaryDTO);
    }

    public UserProfileDTO mapToUserProfileDTO(User user) {
        return profileMapper.toUserProfileDTO(user);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public void deleteUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        userRepository.delete(user);
    }

    @Transactional
    public void updateProfilePicture(Long userId, String imageUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        user.setProfilePicture(imageUrl);
        userRepository.save(user);
    }

    // --- Delegaciones a UserLanguageService ---

    public ProfileResponseDTO addLearningLanguage(Long userId, AddLearningLanguageDTO addDTO) {
        return languageService.addLearningLanguage(userId, addDTO);
    }

    public ProfileResponseDTO updateNativeLanguage(Long userId, UpdateNativeLanguageDTO dto) {
        return languageService.updateNativeLanguage(userId, dto);
    }

    @Transactional
    public void setLearningLanguageActive(Long userId, String languageCode) {
        languageService.setLearningLanguageActive(userId, languageCode);
    }

    @Transactional
    public void setLearningLanguageInactive(Long userId, String languageCode) {
        languageService.setLearningLanguageInactive(userId, languageCode);
    }

    @Transactional
    public void deleteLearningLanguageByCode(Long userId, String languageCode) {
        languageService.deleteLearningLanguageByCode(userId, languageCode);
    }

    @Transactional
    public void updateLearningLevelByCode(Long userId, String languageCode, Long newLevelId) {
        languageService.updateLearningLevelByCode(userId, languageCode, newLevelId);
    }
}
