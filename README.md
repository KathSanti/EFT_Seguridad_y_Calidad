# Semana2-CDY2203 - Backend Clínica Veterinaria

## Ejecución y Despliegue (Docker & SonarQube)

**Levantar contenedores (MySQL, SonarQube, Backend)**
Levanta los servicios en segundo plano y fuerza la reconstrucción de las imágenes:
```bash
docker-compose up -d --build
```
Lanzar Proyecto

# Ejecutar Análisis Estático (SonarQube)
Envía el reporte de calidad al servidor local (puerto 9000). Reemplaza TU_TOKEN_AQUI con tu Global Analysis Token:

```
./mvnw clean verify sonar:sonar -DskipTests -Dsonar.projectKey=backend -Dsonar.host.url=http://localhost:9000 -Dsonar.login=REEMPLAZAR-TOKEN
```

## Control de Acceso Basado en Roles (RBAC)

La API está protegida mediante autenticación *Stateless* con JSON Web Tokens (JWT). Los accesos a los endpoints se dividen según el rol del usuario autenticado:

| Recurso / Endpoint | Permiso Global (Lectura - GET) | Permiso Restringido (POST, PUT, DELETE) |
| :--- | :--- | :--- |
| `/login` | Público | Público |
| `/usuarios`, `/register` | Solo `ROLE_ADMIN` | Solo `ROLE_ADMIN` |
| `/patient/**` | Cualquier usuario autenticado | `ROLE_ADMIN`, `ROLE_ASISTENTE` |
| `/appointment/**` | Cualquier usuario autenticado | `ROLE_ADMIN`, `ROLE_ASISTENTE` |
| `/medication/**` | Cualquier usuario autenticado | `ROLE_ADMIN`, `ROLE_ASISTENTE` |
| `/care/**` | Cualquier usuario autenticado | `ROLE_ADMIN`, `ROLE_ASISTENTE` |
| `/invoice/**` | Cualquier usuario autenticado | `ROLE_ADMIN`, `ROLE_ASISTENTE` |

---

## Auditoría de Calidad y Seguridad (Mejoras SonarQube)

Durante el desarrollo, se refactorizó el código basándose en el análisis estático de SonarQube para elevar los estándares de mantenibilidad y seguridad:

### Mantenibilidad

* **Inyección de dependencias por constructor:** Se eliminó la inyección por atributos (`@Autowired`). Ahora las dependencias se inyectan a través del constructor utilizando `final`, garantizando la inmutabilidad y facilitando las pruebas unitarias.
* **Excepciones Tipificadas:** Se reemplazó el uso de `RuntimeException` genéricas por excepciones nativas de Spring Security (`BadCredentialsException` y `DisabledException`). Esto permite que el framework maneje las respuestas HTTP de forma segura y evita que se enmascaren errores críticos.
* **Listas Inmutables (`Stream.toList()`):** Se actualizó la recolección de Streams en la generación del token JWT. Reemplazar `Collectors.toList()` por `.toList()` genera listas de solo lectura, protegiendo los roles del usuario contra alteraciones accidentales en memoria.

### Seguridad

* **Protección contra asignación masiva (Patrón DTO):** Se implementaron clases `record` (Ej: `InvoiceCreateDto`) para desacoplar las entidades JPA de los controladores REST. Esto evita la exposición directa de la base de datos y previene que un atacante sobrescriba campos críticos (como `id` o estados lógicos).
* **Falso Positivo CSRF Documentado:** SonarQube alertó sobre la desactivación de la protección CSRF (`.csrf.disable()`). Esto se documenta como un riesgo aceptado (falso positivo) ya que la arquitectura es una API REST *Stateless* basada en tokens JWT. Al no utilizar cookies de sesión manejadas automáticamente por el navegador web, el vector de ataque CSRF no es viable en esta fase.

## Usuarios de Prueba (Postman)

Para probar los diferentes niveles de acceso de la API (RBAC), la base de datos se inicializa automáticamente con los siguientes usuarios. 

| Usuario (`user`) | Contraseña (`encryptedPass`) | Rol Asignado | Descripción de Permisos |
| :--- | :--- | :--- | :--- |
| `admin` | `123` | `ROLE_ADMIN` | **Acceso Total.** Puede crear usuarios, registrar medicamentos y gestionar todos los módulos. |
| `asistente` | `123` | `ROLE_ASISTENTE` | **Gestión Clínica.** Puede crear, modificar y eliminar pacientes, citas y facturas. No administra usuarios. |
| `cliente` | `123` | `ROLE_USER` | **Solo Lectura.** Puede consultar información (GET) en rutas permitidas, pero será bloqueado si intenta modificar o crear datos. |

### ¿Cómo probar en Postman?
1. Realiza una petición `POST` a `http://localhost:8180/login`.
2. En la pestaña **Params** o **Body (x-www-form-urlencoded)**, envía las credenciales:
   - `user`: (elige uno de la tabla, ej. `admin`)
   - `encryptedPass`: `123`
3. Copia el token JWT devuelto en la respuesta (el texto que empieza después de "Bearer ").
4. Para consumir los demás endpoints (ej. `/patient`), ve a la pestaña **Authorization**, selecciona el tipo **Bearer Token** y pega tu token.