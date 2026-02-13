-- Horas de intercambios: acumulado en minutos por usuario.
ALTER TABLE users ADD COLUMN total_exchange_minutes INT NOT NULL DEFAULT 0;
