package com.speakBuddy.speackBuddy_backend.service;

import com.speakBuddy.speackBuddy_backend.models.TopicCategory;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Catálogo de tópicos estáticos organizados por categoría.
 * Separado de {@link TopicGeneratorService} para facilitar futuras
 * migraciones a configuración externa (YAML/JSON/BBDD).
 */
public final class TopicCatalog {

    private TopicCatalog() {}

    static final List<String> CONVERSATION = List.of(
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

    static final List<String> DEBATE = List.of(
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

    static final List<String> ROLEPLAY = List.of(
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

    public static String getRandomTopic(TopicCategory category) {
        List<String> topics = switch (category) {
            case CONVERSATION -> CONVERSATION;
            case DEBATE -> DEBATE;
            case ROLEPLAY -> ROLEPLAY;
        };
        return topics.get(ThreadLocalRandom.current().nextInt(topics.size()));
    }
}
