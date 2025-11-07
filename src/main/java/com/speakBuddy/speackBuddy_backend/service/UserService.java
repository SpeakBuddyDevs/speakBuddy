package com.speakBuddy.speackBuddy_backend.service;

import com.speakBuddy.speackBuddy_backend.dto.RegisterRequestDTO;
import com.speakBuddy.speackBuddy_backend.models.Language;
import com.speakBuddy.speackBuddy_backend.models.User;
import com.speakBuddy.speackBuddy_backend.repository.LanguageRepository;
import com.speakBuddy.speackBuddy_backend.repository.UserRepository;
import com.speakBuddy.speackBuddy_backend.security.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
            throw new RuntimeException("El email ya está registrado");
        }

        Language nativeLanguage = languageRepository.findById(request.getNativeLanguageId())
                .orElseThrow(() -> new RuntimeException("Idioma nativo no encontrado"));

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
}
