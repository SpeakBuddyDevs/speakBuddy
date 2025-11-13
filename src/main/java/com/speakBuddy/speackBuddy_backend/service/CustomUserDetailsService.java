package com.speakBuddy.speackBuddy_backend.service;

import com.speakBuddy.speackBuddy_backend.models.User;
import com.speakBuddy.speackBuddy_backend.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

@Service // La anotación @Service le dice a Spring que esto es un Bean y debe gestionarlo
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    /**
     * Este método será llamado por Spring Security cuando un usuario intente autenticarse.
     * @param email El 'username' que Spring nos pasa, que para nosotros es el email.
     * @return Un objeto UserDetails que Spring Security puede entender.
     * @throws UsernameNotFoundException Si el usuario no se encuentra en la BBDD.
     */

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
         User user = userRepository.findByEmail(email)
                 .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Collection <GrantedAuthority> authorities = Collections.singleton(
                new SimpleGrantedAuthority(user.getRole().name()));

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                authorities);
    }
}
