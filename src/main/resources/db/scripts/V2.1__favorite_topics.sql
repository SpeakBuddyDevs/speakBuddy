-- V2.1: Sistema de temas favoritos generados por IA

-- Tabla principal de temas favoritos
CREATE TABLE IF NOT EXISTS favorite_topics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    category VARCHAR(50) NOT NULL,
    level VARCHAR(50) NOT NULL,
    main_text TEXT NOT NULL,
    position_a TEXT NULL,
    position_b TEXT NULL,
    language VARCHAR(10) NOT NULL,
    saved_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    generated_at DATETIME NOT NULL,
    CONSTRAINT fk_favorite_topics_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Tabla para vocabulario sugerido (relación ElementCollection)
CREATE TABLE IF NOT EXISTS favorite_topic_vocabulary (
    topic_id BIGINT NOT NULL,
    word VARCHAR(255) NOT NULL,
    CONSTRAINT fk_vocabulary_topic FOREIGN KEY (topic_id) REFERENCES favorite_topics(id) ON DELETE CASCADE
);

-- Índice para búsquedas por usuario
CREATE INDEX idx_favorite_topics_user ON favorite_topics(user_id);
CREATE INDEX idx_favorite_topics_user_category ON favorite_topics(user_id, category);
