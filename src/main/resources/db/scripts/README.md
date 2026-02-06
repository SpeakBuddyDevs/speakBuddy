# Scripts de migración de base de datos

Estos scripts documentan los cambios de esquema y pueden ejecutarse manualmente cuando:
- Se usa `spring.jpa.hibernate.ddl-auto=validate` o `none` en producción
- Se necesita aplicar cambios en un entorno sin reiniciar la app

**Nota:** Con `ddl-auto=update` (configuración por defecto en desarrollo), Hibernate aplica los cambios automáticamente al arrancar la aplicación.
