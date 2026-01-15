package com.speakBuddy.speackBuddy_backend.config;

import com.speakBuddy.speackBuddy_backend.models.Language;
import com.speakBuddy.speackBuddy_backend.models.LanguageLevel;
import com.speakBuddy.speackBuddy_backend.repository.LanguageLevelRepository;
import com.speakBuddy.speackBuddy_backend.repository.LanguageRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.logging.Logger;

@Configuration
public class DataInitializer {

    private Logger logger = Logger.getLogger(DataInitializer.class.getName());

    @Bean
    CommandLineRunner initDatabase(LanguageRepository languageRepository,
                                   LanguageLevelRepository languageLevelRepository) {
        return args -> {
            if (languageRepository.count() == 0) {

                List<Language> languages = List.of(
                        new Language(null, "Español", "es"),
                        new Language(null, "English", "en"),
                        new Language(null, "Français", "fr"),
                        new Language(null, "Deutsch", "de"),
                        new Language(null, "Italiano", "it")
                );

                languageRepository.saveAll(languages);
                logger.info("Base de datos inicializada con idiomas por defecto.");
            } else {
                logger.info("La base de datos ya contiene idiomas. No se requiere inicialización.");
            }

            if (languageLevelRepository.count() == 0) {
                List<LanguageLevel> languagesLevels = List.of(
                        new LanguageLevel(null, "A1 - Beginner", 1),
                        new LanguageLevel(null, "A2 - Elementary", 2),
                        new LanguageLevel(null, "B1 - Intermediate", 3),
                        new LanguageLevel(null, "B2 - Upper Intermediate", 4),
                        new LanguageLevel(null, "C1 - Advanced", 5),
                        new LanguageLevel(null, "C2 - Proficient", 6)
                );
                languageLevelRepository.saveAll(languagesLevels);
                logger.info("Base de datos inicializada con niveles de idiomas por defecto.");
            }
        };
    }
}
