-- Migración: Sistema de experiencia, niveles y rachas
-- Ejecutar manualmente solo si no usas spring.jpa.hibernate.ddl-auto=update

-- Añadir columna para última fecha de actividad (para calcular rachas)
ALTER TABLE users ADD COLUMN IF NOT EXISTS last_activity_date DATE;

-- Añadir columna para racha actual en días
ALTER TABLE users ADD COLUMN IF NOT EXISTS current_streak_days INT DEFAULT 0;

-- Añadir columna para mejor racha histórica
ALTER TABLE users ADD COLUMN IF NOT EXISTS best_streak_days INT DEFAULT 0;

-- Añadir columna para última fecha de bonus diario reclamado
ALTER TABLE users ADD COLUMN IF NOT EXISTS last_daily_bonus_date DATE;
