
## Arquitectura del Proyecto

El proyecto sigue una arquitectura cliente-servidor clásica:

* **Backend (API REST):** Desarrollado en Java con Spring Boot. Gestiona la autenticación (JWT), usuarios, intercambios y persistencia de datos.
* **Frontend (App Móvil):** Desarrollado en Flutter. Consume la API REST para mostrar la interfaz de usuario.
* **Base de Datos:** MySQL para el almacenamiento relacional.

---

## Guía de Despliegue (Deployment)

### 1. Backend (Spring Boot + Docker)

Existen dos formas de levantar el backend: usando Docker (recomendado) o ejecutando el JAR manualmente.

#### A. Usando Docker Compose (Recomendado)
Asegúrate de tener Docker Desktop instalado y corriendo.

1. Navega a la carpeta del backend.
2. Ejecuta el siguiente comando para levantar la BBDD y el servicio:
   `docker-compose up --build`
3. El servidor estará disponible en: `http://localhost:8080`.

### 2. Frontend (Flutter)

Para desplegar la aplicación móvil o probarla en un dispositivo físico/emulador.

#### A. Configuración de la IP

Antes de compilar, debes apuntar la app a la dirección correcta del backend. Edita el archivo: `lib/constants/api_endpoints.dart`

- **Para Emulador Android:** Usa `http://10.0.2.2:8080` (esta IP apunta al localhost de tu PC desde el emulador).
    
- **Para Dispositivo Físico / Red Local:** Usa la IP local de tu PC (ej: `http://192.168.1.35:8080`). _Asegúrate de que el firewall de tu PC permita conexiones al puerto 8080._


#### B. Ejecutar en Desarrollo

Para probar cambios rápidamente en el emulador:

Bash

```
flutter run
```

---

## 🛠️ Solución de Problemas Comunes

### Error 403 Forbidden al acceder a `localhost:8080`

Es el comportamiento normal. Spring Security protege los endpoints. Para probar que el servidor funciona, usa **Postman**:

- **POST** a `http://localhost:8080/api/auth/login`
    
- Body (JSON): `{ "email": "...", "password": "..." }`
    

### La Base de Datos está vacía al cambiar de PC

Si despliegas en un ordenador nuevo, la BBDD Dockerizada empieza de cero.

1. Cuando el proyecto Spring se ejecute por primera vez, inicializará solamente las tablas de lenguajes.
2. Usa Postman o la App para hacer un **Registro (Sign Up)** nuevo.
3. Esto creará el usuario en la tabla `users` y podrás hacer login.
