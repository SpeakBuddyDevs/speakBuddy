package com.speakBuddy.speackBuddy_backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.speakBuddy.speackBuddy_backend.dto.GeneratedTopicResponseDTO;
import com.speakBuddy.speackBuddy_backend.models.TopicCategory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class TopicGeneratorService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    @Autowired
    public TopicGeneratorService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = new ObjectMapper();
    }

    public GeneratedTopicResponseDTO generate(TopicCategory category, String level, String languageCode) {
        String prompt = buildPrompt(category, level, languageCode);

        String response = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        return parseResponse(response, category, level, languageCode);
    }

    private String buildPrompt(TopicCategory category, String level, String languageCode) {
        String languageName = getLanguageName(languageCode);
        String levelDescription = getLevelDescription(level);
        int randomSeed = ThreadLocalRandom.current().nextInt(1, 10001);

        StringBuilder sb = new StringBuilder();
        sb.append("[Solicitud #").append(randomSeed).append("]\n\n");
        sb.append("Genera un tema de conversación ÚNICO y DIFERENTE para practicar idiomas. ");
        sb.append("El tema debe estar escrito en ").append(languageName).append(". ");
        sb.append("El nivel de dificultad es: ").append(levelDescription).append(". ");

        switch (category) {
            case CONVERSATION -> {
                sb.append("\n\nTipo: CONVERSACIÓN GENERAL\n");
                sb.append("Genera un tema de conversación cotidiano que invite a la reflexión y el intercambio de opiniones. ");
                sb.append("Debe ser una pregunta abierta o una situación sobre la que hablar. ");
                sb.append("Elige UN tema de entre estas categorías (varía tu elección): ");
                sb.append("viajes y aventuras, hobbies y tiempo libre, recuerdos y experiencias, ");
                sb.append("cultura y tradiciones, tecnología y futuro, vida diaria y rutinas, ");
                sb.append("comida y gastronomía, música y entretenimiento, deportes, naturaleza y medio ambiente, ");
                sb.append("familia y amigos, trabajo y estudios, sueños y metas, libros y películas, moda y tendencias.\n");
            }
            case DEBATE -> {
                sb.append("\n\nTipo: DEBATE\n");
                sb.append("Genera un tema de debate con dos posturas claramente opuestas. ");
                sb.append("Una persona defenderá la Postura A y otra la Postura B. ");
                sb.append("El tema debe ser controvertido pero respetuoso. ");
                sb.append("Incluye una breve descripción de cada postura. ");
                sb.append("Elige UN área temática (varía tu elección): ");
                sb.append("tecnología, educación, medio ambiente, trabajo, redes sociales, ");
                sb.append("salud, economía, cultura, política social, estilo de vida, ");
                sb.append("inteligencia artificial, transporte, urbanismo, alimentación, ocio.\n");
            }
            case ROLEPLAY -> {
                sb.append("\n\nTipo: ROLEPLAY (Juego de roles)\n");
                sb.append("Genera un escenario realista de la vida cotidiana para practicar situaciones reales. ");
                sb.append("Describe brevemente la situación y los roles de cada participante. ");
                sb.append("Elige UN escenario de entre estos (varía tu elección): ");
                sb.append("entrevista de trabajo, restaurante o cafetería, tienda de ropa, ");
                sb.append("consulta médica, aeropuerto o estación, hotel o alojamiento, ");
                sb.append("banco, oficina de correos, llamada telefónica de servicio al cliente, ");
                sb.append("alquiler de coche o piso, gimnasio, peluquería, farmacia, ");
                sb.append("fiesta o evento social, clase o tutoría, reunión de vecinos, cita a ciegas, ");
                sb.append("mudanza, visita turística guiada, queja formal, negociación de precio.\n");
            }
        }

        sb.append("\nIMPORTANTE: Sé creativo y genera un tema diferente cada vez. ");
        sb.append("Varía los escenarios, las situaciones y los enfoques.\n");

        sb.append("\nAdemás, incluye 5 palabras o expresiones de vocabulario relevantes para este tema ");
        sb.append("que sean apropiadas para el nivel ").append(level).append(".\n\n");

        sb.append("Responde ÚNICAMENTE con un JSON válido en este formato exacto:\n");
        sb.append("{\n");
        sb.append("  \"mainText\": \"El texto principal del tema o pregunta\",\n");

        if (category == TopicCategory.DEBATE) {
            sb.append("  \"positionA\": \"Postura A: descripción breve de la primera postura\",\n");
            sb.append("  \"positionB\": \"Postura B: descripción breve de la postura opuesta\",\n");
        }

        sb.append("  \"vocabulary\": [\"palabra1\", \"palabra2\", \"palabra3\", \"palabra4\", \"palabra5\"]\n");
        sb.append("}\n\n");
        sb.append("No incluyas explicaciones adicionales, solo el JSON.");

        return sb.toString();
    }

    private GeneratedTopicResponseDTO parseResponse(String response, TopicCategory category, String level, String languageCode) {
        try {
            String jsonContent = extractJson(response);
            JsonNode root = objectMapper.readTree(jsonContent);

            String mainText = root.has("mainText") ? root.get("mainText").asText() : "";
            String positionA = root.has("positionA") ? root.get("positionA").asText() : null;
            String positionB = root.has("positionB") ? root.get("positionB").asText() : null;

            List<String> vocabulary = new ArrayList<>();
            if (root.has("vocabulary") && root.get("vocabulary").isArray()) {
                for (JsonNode word : root.get("vocabulary")) {
                    vocabulary.add(word.asText());
                }
            }

            return GeneratedTopicResponseDTO.builder()
                    .category(category.name())
                    .level(level)
                    .mainText(mainText)
                    .positionA(positionA)
                    .positionB(positionB)
                    .suggestedVocabulary(vocabulary)
                    .language(languageCode)
                    .generatedAt(LocalDateTime.now())
                    .isFavorite(false)
                    .build();

        } catch (Exception e) {
            return GeneratedTopicResponseDTO.builder()
                    .category(category.name())
                    .level(level)
                    .mainText(response)
                    .suggestedVocabulary(List.of())
                    .language(languageCode)
                    .generatedAt(LocalDateTime.now())
                    .isFavorite(false)
                    .build();
        }
    }

    private String extractJson(String response) {
        int start = response.indexOf('{');
        int end = response.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return response.substring(start, end + 1);
        }
        return response;
    }

    private String getLanguageName(String code) {
        return switch (code.toLowerCase()) {
            case "es" -> "español";
            case "en" -> "inglés";
            case "fr" -> "francés";
            case "de" -> "alemán";
            case "it" -> "italiano";
            case "pt" -> "portugués";
            case "ja" -> "japonés";
            case "zh" -> "chino";
            case "ko" -> "coreano";
            case "ru" -> "ruso";
            default -> code;
        };
    }

    private String getLevelDescription(String level) {
        return switch (level.toLowerCase()) {
            case "beginner" -> "principiante (vocabulario básico, frases simples)";
            case "intermediate" -> "intermedio (vocabulario variado, estructuras gramaticales estándar)";
            case "advanced" -> "avanzado (vocabulario sofisticado, expresiones idiomáticas, matices)";
            default -> level;
        };
    }
}
