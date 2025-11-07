package com.speakBuddy.speackBuddy_backend.service;

import com.speakBuddy.speackBuddy_backend.dto.RegisterRequestDTO;
import com.speakBuddy.speackBuddy_backend.models.User;
import com.speakBuddy.speackBuddy_backend.repository.LanguageRepository;
import com.speakBuddy.speackBuddy_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
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
    public User registerUSer(RegisterRequestDTO request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("El email ya está registrado");
        }




    }

}
