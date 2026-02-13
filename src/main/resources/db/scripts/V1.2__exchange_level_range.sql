-- Rango de niveles CEFR (A1–C2) para intercambios.
-- level_order en language_levels: 1=A1, 2=A2, 3=B1, 4=B2, 5=C1, 6=C2.

ALTER TABLE exchanges ADD COLUMN required_level_min_order INT NULL;
ALTER TABLE exchanges ADD COLUMN required_level_max_order INT NULL;

-- Migrar datos existentes: required_level (Principiante/Intermedio/Avanzado) -> min/max
UPDATE exchanges
SET required_level_min_order = CASE
    WHEN required_level IS NULL OR LOWER(TRIM(required_level)) = 'principiante' THEN 1
    WHEN LOWER(TRIM(required_level)) = 'intermedio' THEN 4
    WHEN LOWER(TRIM(required_level)) = 'avanzado' THEN 5
    ELSE 1
END,
required_level_max_order = 6
WHERE required_level_min_order IS NULL;
