package com.speakBuddy.speackBuddy_backend.repository;

import com.speakBuddy.speackBuddy_backend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {

    /**
     * Busca un usuario por su correo electrónico.
     */
    Optional<User> findByEmail(String email);

    /**
     * Busca un usuario por su nombre de usuario.
     */
    Optional<User> findByUsername(String username);

    /**
     * Verifica si existe un usuario con el correo electrónico dado.
     */
    boolean existsByEmail(String email);

    /**
     * Verifica si existe un usuario con el nombre de usuario dado.
     */
    boolean existsByUsername(String username);
}
