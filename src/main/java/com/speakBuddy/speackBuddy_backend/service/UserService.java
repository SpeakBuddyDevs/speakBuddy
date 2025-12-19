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
import com.speakBuddy.speackBuddy_backend.repository.UserLanguageLearningRepository;
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
    private final LanguageLevelRepository languageLevelRepository;
    private final UserLanguageLearningRepository userLanguageLearningRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository,
                       LanguageRepository languageRepository,
                       LanguageLevelRepository languageLevelRepository,
                       UserLanguageLearningRepository userLanguageLearningRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.languageRepository = languageRepository;
        this.languageLevelRepository = languageLevelRepository;
        this.userLanguageLearningRepository = userLanguageLearningRepository;
        this.passwordEncoder = passwordEncoder;
    }


    // Metodo principal de lógica de HU 1.1
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

    public ProfileResponseDTO addLearningLanguage(Long userId, AddLearningLanguageDTO addDTO) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Language language = languageRepository.findById(addDTO.getLanguageId())
                .orElseThrow(() -> new ResourceNotFoundException("Language not found"));

        LanguageLevel level = languageLevelRepository.findById(addDTO.getLevelId())
                .orElseThrow(() -> new ResourceNotFoundException("Language level not found"));

        boolean alreadyLearning = user.getLanguagesToLearn().stream()
                .anyMatch(learning -> learning.getLanguage().getId().equals(language.getId()));

        if (alreadyLearning) {
            throw new IllegalArgumentException("User is already learning this language");
        }

        UserLanguagesLearning newLearning = new UserLanguagesLearning();
        newLearning.setUser(user);
        newLearning.setLanguage(language);
        newLearning.setLevel(level);

        user.getLanguagesToLearn().add(newLearning);
        User updatesdUser = userRepository.save(user);

        return mapUserToProfileResponseDTO(updatesdUser);
    }

    // --- Lógica de HU 1.2: Eliminar Idioma de Aprendizaje ---
    public ProfileResponseDTO deleteLearningLanguage(Long userId, Long learningId) {
        // 1. Buscar al usuario
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + userId));

        // 2. Buscar la *relación* de aprendizaje específica
        UserLanguagesLearning learningToRemove = userLanguageLearningRepository.findById(learningId)
                .orElseThrow(() -> new ResourceNotFoundException("Relación de aprendizaje no encontrada con ID: " + learningId));

        // 3. ¡VALIDACIÓN DE SEGURIDAD CLAVE!
        if (!learningToRemove.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Acceso denegado. La relación no pertenece al usuario.");
        }

        // 4. Eliminar la relación
        user.getLanguagesToLearn().remove(learningToRemove);
        User updatedUser = userRepository.save(user);

        // 5. Devolver el perfil actualizado
        return mapUserToProfileResponseDTO(updatedUser);
    }

    // --- Lógica de HU 1.2: Actualizar Idioma Nativo ---
    public ProfileResponseDTO updateNativeLanguage(Long userId, UpdateNativeLanguageDTO dto) {

        // 1. Buscar al usuario
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 2. Buscar la entidad del *nuevo* idioma nativo
        Language newNativeLanguage = languageRepository.findById(dto.getNewNativeLanguageId())
                .orElseThrow(() -> new ResourceNotFoundException("Language not found"));

        // 3. Realizar la actualización
        user.setNativeLanguage(newNativeLanguage);

        // 4. Guardar los cambios en la BBDD
        User updatedUser = userRepository.save(user);

        // 5. Devolver el perfil completo y actualizado (reutilizando nuestro helper)
        return mapUserToProfileResponseDTO(updatedUser);
    }

    // --- Lógica de HU 1.2: Actualizar Nivel de Idioma de Aprendizaje ---
    public ProfileResponseDTO updateLearningLevel(Long userId, Long learningId, UpdateLearningLevelDTO dto) {

        // 1. Buscar el nuevo nivel al que se quiere actualizar
        LanguageLevel newLevel = languageLevelRepository.findById(dto.getNewLevelId())
                .orElseThrow(() -> new ResourceNotFoundException("Nivel de idioma no encontrado con ID: " + dto.getNewLevelId()));

        // 2. Buscar la *relación* de aprendizaje que queremos editar
        UserLanguagesLearning learningToUpdate = userLanguageLearningRepository.findById(learningId)
                .orElseThrow(() -> new ResourceNotFoundException("Relación de aprendizaje no encontrada con ID: " + learningId));

        // 3. ¡VALIDACIÓN DE SEGURIDAD CLAVE!
        if (!learningToUpdate.getUser().getId().equals(userId)) {
            throw new RuntimeException("Acceso denegado. La relación no pertenece al usuario.");
        }

        // 4. Realizar la actualización
        learningToUpdate.setLevel(newLevel);

        // 5. Guardar la entidad de relación actualizada
        userLanguageLearningRepository.save(learningToUpdate);

        // 6. Devolver el perfil completo y actualizado del usuario
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + userId));

        return mapUserToProfileResponseDTO(user);
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

        //Calculo de experiencia necesaria por nivel
        //La experiencia necesaria para subir de nivel es 100 veces la del nivel actual
        long threshold = user.getLevel() * 100L;
        dto.setXpToNextLevel(threshold);

        if (threshold > 0) {
            double percentage = (double) user.getExperiencePoints() / threshold;
            //Aseguramos que no pase del 100% del nivel
            dto.setProgressPercentage(Math.min(percentage, 1.0));
        } else {
            dto.setProgressPercentage(0.0);
        }

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

    /**
     * Eliminar el usuario y sus datos
     * @param email
     */

    public void deleteUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        userRepository.delete(user);
    }
}
