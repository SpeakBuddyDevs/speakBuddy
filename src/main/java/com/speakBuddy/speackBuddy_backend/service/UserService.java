package com.speakBuddy.speackBuddy_backend.service;

import com.speakBuddy.speackBuddy_backend.dto.LanguageDTO;
import com.speakBuddy.speackBuddy_backend.dto.LearningLanguageDTO;
import com.speakBuddy.speackBuddy_backend.dto.ProfileResponseDTO;
import com.speakBuddy.speackBuddy_backend.dto.ProfileUpdateDTO;
import com.speakBuddy.speackBuddy_backend.dto.RegisterRequestDTO;
import com.speakBuddy.speackBuddy_backend.exception.EmailAlreadyExistsException;
import com.speakBuddy.speackBuddy_backend.exception.ResourceNotFoundException;
import com.speakBuddy.speackBuddy_backend.models.Language;
import com.speakBuddy.speackBuddy_backend.models.User;
import com.speakBuddy.speackBuddy_backend.models.UserLanguagesLearning;
import com.speakBuddy.speackBuddy_backend.repository.LanguageRepository;
import com.speakBuddy.speackBuddy_backend.repository.UserRepository;
import com.speakBuddy.speackBuddy_backend.security.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final LanguageRepository languageRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, LanguageRepository languageRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.languageRepository = languageRepository;
        this.passwordEncoder = passwordEncoder;
    }


    // Método principal de lógica de HU 1.1
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

        // Asignar la entidad relacionada de Language
        newUser.setNativeLanguage(nativeLanguage);

        // Asignar valores por defecto
        newUser.setLevel(1);
        newUser.setExperiencePoints(0L);

        // Asignar rol por defecto
        newUser.setRole(Role.ROLE_USER);


        return userRepository.save(newUser);
    }

    // Logica de HU 1.2: Obtener información del usuario (perfil)
    public ProfileResponseDTO getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return mapUserToProfileResponseDTO(user);
    }

    // Logica de HU 1.2: Actualizar información del usuario (perfil)
    public ProfileResponseDTO updateProfile(Long userId, ProfileUpdateDTO updateDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Actualizar solo los campos permitidos
        user.setName(updateDTO.getName());
        user.setSurname(updateDTO.getSurname());
        user.setProfilePicture(updateDTO.getProfilePictureUrl());

        // Actualizar el nombre de usuario público
        user.setUsername(updateDTO.getName() + " " + updateDTO.getSurname());

        User updatedUser = userRepository.save(user);

        return mapUserToProfileResponseDTO(updatedUser);
    }

    // Métodos auxiliares para mapeo de entidades a DTOs
    private ProfileResponseDTO mapUserToProfileResponseDTO(User user) {
        ProfileResponseDTO dto = new ProfileResponseDTO();

        // Mapeo de campos
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setUsername(user.getUsername());
        dto.setName(user.getName());
        dto.setSurname(user.getSurname());
        dto.setProfilePictureURL(user.getProfilePicture());
        dto.setLevel(user.getLevel());
        dto.setExperiencePoints(user.getExperiencePoints());

        // Mapeo del idioma nativo
        if (user.getNativeLanguage() != null) {
            dto.setNativeLanguage(mapLanguageToDTO(user.getNativeLanguage()));
        }

        // Mapeo del idioma de aprendizaje
        Set<LearningLanguageDTO> learningDTOs = user.getLanguagesToLearn().stream()
                .map(this::mapLearningToDTO)
                .collect(Collectors.toSet());
        dto.setLanguagesToLearn(learningDTOs);

        return dto;
    }

    private LanguageDTO mapLanguageToDTO(Language language) {
        LanguageDTO dto = new LanguageDTO();
        dto.setId(language.getId());
        dto.setName(language.getName());
        dto.setIsoCode(language.getIsoCode());
        return dto;
    }

    private LearningLanguageDTO mapLearningToDTO(UserLanguagesLearning learning) {
        LearningLanguageDTO dto = new LearningLanguageDTO();
        // Mapea el idioma dentro de la relación
        dto.setLanguage(mapLanguageToDTO(learning.getLanguage()));
        // Mapea el nivel dentro de la relación
        dto.setLevelName(learning.getLevel().getName());
        return dto;
    }
}
