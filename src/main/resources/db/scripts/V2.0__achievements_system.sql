-- Sistema de logros/achievements

CREATE TABLE achievements (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    type VARCHAR(50) NOT NULL UNIQUE,
    title VARCHAR(100) NOT NULL,
    description VARCHAR(255) NOT NULL,
    target_progress INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_achievements (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    achievement_id BIGINT NOT NULL,
    current_progress INT DEFAULT 0,
    is_unlocked BOOLEAN DEFAULT FALSE,
    unlocked_at TIMESTAMP NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (achievement_id) REFERENCES achievements(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_achievement (user_id, achievement_id)
);

CREATE INDEX idx_user_achievements_user ON user_achievements(user_id);
CREATE INDEX idx_user_achievements_unlocked ON user_achievements(is_unlocked);

-- Insertar logros predefinidos
INSERT INTO achievements (type, title, description, target_progress) VALUES
('POLYGLOT', 'Políglota', '5 idiomas practicados', 5),
('CONVERSATIONALIST', 'Conversador', '50 conversaciones', 50),
('EARLY_BIRD', 'Madrugador', '20 sesiones matutinas', 20),
('STAR', 'Estrella', '100 valoraciones 5★', 100),
('STREAK', 'Racha', '30 días consecutivos', 30),
('SOCIAL', 'Social', '10 eventos asistidos', 10),
('EXPLORER', 'Explorador', '10 países diferentes', 10),
('MENTOR', 'Mentor', '25 principiantes ayudados', 25),
('HOST', 'Anfitrión', '10 intercambios creados', 10);
