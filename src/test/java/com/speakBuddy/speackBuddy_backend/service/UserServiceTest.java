package com.speakBuddy.speackBuddy_backend.service;

import static org.junit.jupiter.api.Assertions.*;

import com.speakBuddy.speackBuddy_backend.dto.LearningLanguageDTO;
import com.speakBuddy.speackBuddy_backend.dto.ProfileResponseDTO;
import com.speakBuddy.speackBuddy_backend.dto.RegisterRequestDTO;
import com.speakBuddy.speackBuddy_backend.exception.EmailAlreadyExistsException;
import com.speakBuddy.speackBuddy_backend.exception.ResourceNotFoundException;
import com.speakBuddy.speackBuddy_backend.models.Language;
import com.speakBuddy.speackBuddy_backend.models.LanguageLevel;
import com.speakBuddy.speackBuddy_backend.models.User;
import com.speakBuddy.speackBuddy_backend.models.UserLanguagesLearning;
import com.speakBuddy.speackBuddy_backend.repository.LanguageRepository;
import com.speakBuddy.speackBuddy_backend.repository.UserRepository;
import com.speakBuddy.speackBuddy_backend.security.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private LanguageRepository languageRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;


    @Test
    void registerUser_HappyPath() {
        RegisterRequestDTO dto = new RegisterRequestDTO();
        dto.setEmail("test@gmail.com");
        dto.setPassword("password123");
        dto.setName("Test");
        dto.setSurname("User");
        dto.setNativeLanguageId(1L);

        Language mockLanguage = new Language();
        mockLanguage.setId(1L);
        mockLanguage.setName("Español");
        mockLanguage.setIsoCode("es");

        // Devuelve false cuando alguien llame a userRepository.existsByEmail
        when(userRepository.existsByEmail("test@gmail.com")).thenReturn(false);

        when(languageRepository.findById(1L)).thenReturn(Optional.of(mockLanguage));

        when(passwordEncoder.encode("password123")).thenReturn("hashed_password_abc");

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.registerUser(dto);


        // Verifico los resultados
        assertNotNull(result); // ¿No es nulo?
        assertEquals("test@gmail.com", result.getEmail()); // ¿El email es correcto?
        assertEquals("hashed_password_abc", result.getPassword()); // ¿La contraseña se hasheó?
        assertEquals("Test User", result.getUsername()); // ¿El username se generó bien?
        assertEquals(Role.ROLE_USER, result.getRole()); // ¿Se asignó el rol por defecto?
        assertEquals(1, result.getLevel()); // ¿Se asignó el nivel por defecto?
        assertEquals(mockLanguage, result.getNativeLanguage()); // ¿Se asignó el idioma correcto?

        verify(userRepository, times(1)).save(any(User.class));
        verify(userRepository, times(1)).existsByEmail("test@gmail.com");
    }

    @Test
    void registerUser_EmailAlreadyExists() {

        RegisterRequestDTO dto = new RegisterRequestDTO();
        dto.setEmail("existente@gmail.com");
        dto.setPassword("password123");
        dto.setName("Test");
        dto.setSurname("User");
        dto.setNativeLanguageId(1L);

        when(userRepository.existsByEmail("existente@gmail.com")).thenReturn(true);

        assertThrows(
                EmailAlreadyExistsException.class,
                () -> { userService.registerUser(dto);
                },
                "Se esperaba que se lanzara EmailAlreadyExistsException"
        );

        verify(languageRepository, never()).findById(anyLong());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void refisterUser_LanguageNotFound() {
        RegisterRequestDTO dto = new RegisterRequestDTO();
        dto.setEmail("nuevo.usuario@gmail.com");
        dto.setPassword("password123");
        dto.setName("Test");
        dto.setSurname("User");
        dto.setNativeLanguageId(99L); // Idioma (id) que no existe

        when(userRepository.existsByEmail("nuevo.usuario@gmail.com")).thenReturn(false);
        when(languageRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> {
                    userService.registerUser(dto);
                },
                "Se esperaba que se lanzara ResourceNotFoundException"
        );

        // "Verifica que el metodo save nunca fue llamado."
        verify(userRepository, never()).save(any(User.class));

        // "Verifica que el metodo save nunca fue llamado."
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void getProfile_HappyPath_ShouldReturnMappedDTO() {

        // --- 1. ARRANGE (Preparar el escenario) ---

        // 1.1. Creamos todos los datos falsos (mocks) que componen un perfil
        Language nativeLang = new Language(1L, "Español", "es");
        Language learnLang = new Language(2L, "Inglés", "en");
        LanguageLevel learnLevel = new LanguageLevel(3L, "B1 - Intermedio", 3);

        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@user.com");
        mockUser.setUsername("Test User");
        mockUser.setName("Test");
        mockUser.setSurname("User");
        mockUser.setLevel(1);
        mockUser.setExperiencePoints(50L);
        mockUser.setNativeLanguage(nativeLang);

        // 1.2. Creamos la relación de aprendizaje
        UserLanguagesLearning relation = new UserLanguagesLearning(
                10L, // ID de la relación
                mockUser,
                learnLang,
                learnLevel
        );
        // Añadimos la relación al Set del usuario
        mockUser.setLanguagesToLearn(Set.of(relation));

        // 1.3. ¡Programamos el Mock!
        // "CUANDO alguien busque al usuario con ID 1... DEVUELVE nuestro mockUser."
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));


        // --- 2. ACT (Actuar) ---

        // Llamamos al método real que queremos probar
        ProfileResponseDTO result = userService.getProfile(1L);


        // --- 3. ASSERT (Verificar el resultado) ---

        // 3.1. Verificar los campos principales
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("test@user.com", result.getEmail());
        assertEquals("Test User", result.getUsername());

        // 3.2. ¡Verificar el mapeo de los DTOs anidados! (Lo más importante)
        assertNotNull(result.getNativeLanguage());
        assertEquals("Español", result.getNativeLanguage().getName());
        assertEquals("es", result.getNativeLanguage().getIsoCode());

        // 3.3. Verificar la lista de idiomas a aprender
        assertNotNull(result.getLanguagesToLearn());
        assertEquals(1, result.getLanguagesToLearn().size());

        // Sacamos el primer (y único) idioma de la lista para inspeccionarlo
        LearningLanguageDTO learningDTO = result.getLanguagesToLearn().iterator().next();
        assertEquals("Inglés", learningDTO.getLanguage().getName());
        assertEquals("B1 - Intermedio", learningDTO.getLevelName());

        // 4.1. Verificar que funcione el calculo de progreso del nivel
        assertEquals(1,result.getLevel());
        assertEquals(50,result.getExperiencePoints());

        assertEquals(100L,result.getXpToNextLevel());
        assertEquals(0.5,result.getProgressPercentage());
    }

    @Test
    void getProfile_UserNotFound_ShouldThrowException() {

        // --- 1. ARRANGE (Preparar el escenario) ---

        // "CUANDO alguien busque al usuario con ID 999... DEVUELVE vacío."
        when(userRepository.findById(999L)).thenReturn(Optional.empty());


        // --- 2. ACT & 3. ASSERT (Actuar y Verificar) ---

        // Esta accion lanzará una excepción...
        assertThrows(
                ResourceNotFoundException.class,
                () -> {
                    userService.getProfile(999L); // cuando se llame a este método
                }
        );
    }

    @Test
    void deleteUserByEmail_WhenUserExists_ShouldDeleteUser() {
        String email =  "test@example.com";
        User mockUser = new User();
        mockUser.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));

        userService.deleteUserByEmail(email);

        //Verifica que se ha llamado al metodo delete en el usuario correcto
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    void deleteUserByEmail_WhenUserDoesNotExist_ShouldThrowException() {
        String email = "fantasma@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            userService.deleteUserByEmail(email);
        });

        //Verificacion de que no se ha llamado al método
        verify(userRepository, never()).findByEmail(email);
    }

}
