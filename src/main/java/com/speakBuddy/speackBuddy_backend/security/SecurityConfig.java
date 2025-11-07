package com.speakBuddy.speackBuddy_backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration // Le dice a Spring que esta clase contiene configuraciones de seguridad (define Beans)
public class SecurityConfig {

    @Bean // Crea un Bean para el PasswordEncoder que utiliza BCrypt
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
