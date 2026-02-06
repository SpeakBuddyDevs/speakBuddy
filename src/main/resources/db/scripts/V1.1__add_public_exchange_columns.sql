-- Migración: Campos para intercambios públicos (Plan Intercambios Públicos, paso 1.4)
-- Ejecutar manualmente solo si no usas spring.jpa.hibernate.ddl-auto=update
-- Con ddl-auto=update, Hibernate aplica estos cambios automáticamente al arrancar.

ALTER TABLE exchanges ADD COLUMN is_public TINYINT(1) NOT NULL DEFAULT 0;
ALTER TABLE exchanges ADD COLUMN max_participants INT NULL;
ALTER TABLE exchanges ADD COLUMN description VARCHAR(2000) NULL;
ALTER TABLE exchanges ADD COLUMN native_language_code VARCHAR(10) NULL;
ALTER TABLE exchanges ADD COLUMN target_language_code VARCHAR(10) NULL;
ALTER TABLE exchanges ADD COLUMN required_level VARCHAR(50) NULL;
