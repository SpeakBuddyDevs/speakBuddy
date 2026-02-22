-- Migración: Intercambios privados de enlace (shareToken) a contraseña (password)
-- Ejecutar manualmente solo si no usas spring.jpa.hibernate.ddl-auto=update

-- Añadir columna password
ALTER TABLE exchanges ADD COLUMN IF NOT EXISTS password VARCHAR(20);

-- Generar contraseñas para intercambios privados existentes que tenían share_token
UPDATE exchanges 
SET password = UPPER(SUBSTRING(MD5(RANDOM()::text), 1, 6))
WHERE is_public = false AND password IS NULL;

-- Eliminar columna share_token (ya no se usa)
ALTER TABLE exchanges DROP COLUMN IF EXISTS share_token;
