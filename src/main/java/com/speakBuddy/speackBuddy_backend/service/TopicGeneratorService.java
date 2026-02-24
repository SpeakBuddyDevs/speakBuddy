package com.speakBuddy.speackBuddy_backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.speakBuddy.speackBuddy_backend.dto.GeneratedTopicResponseDTO;
import com.speakBuddy.speackBuddy_backend.models.TopicCategory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
        String selectedTopic = TopicCatalog.getRandomTopic(category);

        StringBuilder sb = new StringBuilder();
        sb.append("Genera contenido para practicar idiomas. ");
        sb.append("El contenido debe estar escrito en ").append(languageName).append(". ");
        sb.append("El nivel de dificultad es: ").append(levelDescription).append(".\n\n");

        switch (category) {
            case CONVERSATION -> {
                sb.append("TEMA ASIGNADO: ").append(selectedTopic.toUpperCase()).append("\n\n");
                sb.append("Genera UNA pregunta abierta o tema de conversación sobre: \"").append(selectedTopic).append("\". ");
                sb.append("La pregunta debe invitar a la reflexión y al intercambio de opiniones. ");
                sb.append("Sé creativo y original con tu enfoque del tema.\n");
            }
            case DEBATE -> {
                sb.append("TEMA DE DEBATE ASIGNADO: ").append(selectedTopic.toUpperCase()).append("\n\n");
                sb.append("Genera un tema de debate sobre: \"").append(selectedTopic).append("\". ");
                sb.append("Debe tener dos posturas claramente opuestas. ");
                sb.append("Una persona defenderá la Postura A y otra la Postura B. ");
                sb.append("El planteamiento debe ser respetuoso. ");
                sb.append("Incluye una breve descripción de cada postura.\n");
            }
            case ROLEPLAY -> {
                sb.append("ESCENARIO ASIGNADO: ").append(selectedTopic.toUpperCase()).append("\n\n");
                sb.append("Genera un escenario de roleplay sobre: \"").append(selectedTopic).append("\". ");
                sb.append("Describe la situación específica y los roles de cada participante. ");
                sb.append("Incluye contexto suficiente para que los participantes puedan improvisar el diálogo.\n");
            }
        }

        sb.append("\nIncluye 5 palabras o expresiones de vocabulario útiles para esta situación, ");
        sb.append("apropiadas para el nivel ").append(level).append(".\n\n");

        sb.append("Responde ÚNICAMENTE con un JSON válido en este formato exacto:\n");
        sb.append("{\n");
        sb.append("  \"mainText\": \"El texto principal del tema, pregunta o situación\",\n");

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
