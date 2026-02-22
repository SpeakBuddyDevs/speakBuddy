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
import java.util.concurrent.ThreadLocalRandom;

@Service
public class TopicGeneratorService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    private static final List<String> CONVERSATION_TOPICS = List.of(
            "viajes y destinos soñados",
            "tu lugar favorito del mundo",
            "recuerdos de la infancia",
            "tradiciones familiares",
            "la mejor comida que has probado",
            "música que te emociona",
            "películas que te marcaron",
            "libros que recomendarías",
            "hobbies y pasatiempos",
            "deportes que practicas o te gustan",
            "mascotas y animales",
            "tu rutina diaria ideal",
            "sueños y metas personales",
            "experiencias que cambiaron tu vida",
            "amistades importantes",
            "momentos embarazosos divertidos",
            "festividades y celebraciones",
            "comidas típicas de tu país",
            "series de televisión favoritas",
            "videojuegos y entretenimiento",
            "redes sociales y su impacto",
            "la naturaleza y el medio ambiente",
            "ciudades que te gustaría visitar",
            "aprender nuevos idiomas",
            "trabajo de tus sueños",
            "inventos que cambiaron el mundo",
            "superpoderes que te gustaría tener",
            "vida en el campo vs ciudad",
            "estaciones del año favoritas",
            "recetas de cocina familiares",
            "conciertos o eventos memorables",
            "arte y museos",
            "fotografía y momentos capturados",
            "moda y estilo personal",
            "fitness y vida saludable",
            "meditación y bienestar mental",
            "voluntariado y ayudar a otros",
            "aprender a tocar instrumentos",
            "colecciones y objetos especiales",
            "historias de tu familia",
            "primeros trabajos y experiencias",
            "mudanzas y cambios de vida",
            "amigos de la infancia",
            "profesores que te inspiraron",
            "momentos de superación personal",
            "cosas que te hacen feliz",
            "planes para el fin de semana",
            "vacaciones memorables",
            "comida casera vs restaurantes",
            "costumbres de otros países"
    );

    private static final List<String> DEBATE_TOPICS = List.of(
            "redes sociales: ¿benefician o perjudican las relaciones?",
            "trabajo remoto vs trabajo presencial",
            "educación online vs educación tradicional",
            "¿deberían los deberes escolares ser obligatorios?",
            "energías renovables vs combustibles fósiles",
            "comida rápida: ¿conveniencia o problema de salud?",
            "¿es mejor alquilar o comprar una vivienda?",
            "transporte público vs coche propio",
            "¿deberían los influencers ser regulados?",
            "inteligencia artificial: ¿oportunidad o amenaza?",
            "¿es necesario ir a la universidad para tener éxito?",
            "libros físicos vs libros electrónicos",
            "¿deberían existir los zoológicos?",
            "turismo masivo: ¿beneficio económico o daño cultural?",
            "efectivo vs pagos digitales",
            "¿es ético comer carne?",
            "videojuegos: ¿entretenimiento sano o adicción?",
            "¿deberían los padres controlar el uso de tecnología de sus hijos?",
            "moda rápida vs moda sostenible",
            "¿es mejor vivir en una ciudad grande o en un pueblo?",
            "¿deberían ser gratuitos los museos?",
            "¿es necesario aprender a conducir hoy en día?",
            "televisión tradicional vs streaming",
            "¿deberían existir los exámenes?",
            "medicina tradicional vs medicina alternativa",
            "¿es importante mantener las tradiciones?",
            "¿deberían los deportes electrónicos considerarse deportes?",
            "privacidad vs seguridad en internet",
            "¿es mejor ser especialista o generalista?",
            "¿deberían las propinas ser obligatorias?",
            "¿es ético usar animales en investigación?",
            "jubilación temprana vs trabajar hasta mayor edad",
            "¿deberían prohibirse los coches en centros urbanos?",
            "educación bilingüe: ¿ventaja o confusión?",
            "¿es mejor comprar local o en grandes superficies?",
            "¿deberían los uniformes escolares ser obligatorios?",
            "teletrabajo: ¿libertad o aislamiento?",
            "¿es importante tener título universitario?",
            "¿deberían las redes sociales verificar la edad?",
            "turismo espacial: ¿avance o frivolidad?"
    );

    private static final List<String> ROLEPLAY_SCENARIOS = List.of(
            "pedir comida en un restaurante",
            "hacer el check-in en un hotel",
            "comprar ropa en una tienda",
            "pedir indicaciones en la calle",
            "ir al médico por un resfriado",
            "comprar billetes de tren",
            "alquilar un coche",
            "hacer una reserva por teléfono",
            "devolver un producto defectuoso",
            "abrir una cuenta en el banco",
            "pedir cita en la peluquería",
            "comprar medicinas en la farmacia",
            "registrarse en el aeropuerto",
            "pedir ayuda en una tienda de electrónica",
            "quejarse del ruido a un vecino",
            "negociar el precio en un mercadillo",
            "pedir recomendaciones en una librería",
            "solicitar información turística",
            "hacer una reclamación en un hotel",
            "presentarse en una entrevista de trabajo",
            "pedir un café con especificaciones",
            "inscribirse en un gimnasio",
            "alquilar un apartamento",
            "llamar a servicio técnico",
            "pedir la cuenta en un restaurante",
            "cambiar dinero en una casa de cambio",
            "solicitar un visado en una embajada",
            "reportar un objeto perdido",
            "comprar entradas para un concierto",
            "pedir información sobre un curso",
            "hacer una cita con el dentista",
            "preguntar por ofertas en un supermercado",
            "enviar un paquete en correos",
            "reservar una mesa para una celebración",
            "pedir disculpas por llegar tarde",
            "solicitar una extensión de plazo",
            "presentar a un amigo a tu familia",
            "organizar una fiesta sorpresa",
            "dar feedback a un compañero de trabajo",
            "negociar un aumento de sueldo",
            "pedir prestado algo a un vecino",
            "explicar un problema técnico por teléfono",
            "hacer una denuncia en comisaría",
            "pedir consejo en una tienda de vinos",
            "contratar un servicio de mudanzas",
            "consultar al veterinario sobre tu mascota",
            "matricularse en una autoescuela",
            "solicitar información en una inmobiliaria",
            "pedir un presupuesto para una reforma",
            "reservar actividades en un hotel resort"
    );

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

    private String getRandomTopic(TopicCategory category) {
        List<String> topics = switch (category) {
            case CONVERSATION -> CONVERSATION_TOPICS;
            case DEBATE -> DEBATE_TOPICS;
            case ROLEPLAY -> ROLEPLAY_SCENARIOS;
        };
        int index = ThreadLocalRandom.current().nextInt(topics.size());
        return topics.get(index);
    }

    private String buildPrompt(TopicCategory category, String level, String languageCode) {
        String languageName = getLanguageName(languageCode);
        String levelDescription = getLevelDescription(level);
        String selectedTopic = getRandomTopic(category);

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
