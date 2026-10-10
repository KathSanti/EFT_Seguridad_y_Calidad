# Semana2-CDY2203 - Backend Clínica Veterinaria

## Ejecución y Despliegue (Docker & SonarQube)

**Levantar contenedores (MySQL, SonarQube, Backend)**
Levanta los servicios en segundo plano y fuerza la reconstrucción de las imágenes:

```bash
docker-compose up -d --build
```

# Ejecutar Análisis Estático (SonarQube)
Envía el reporte de calidad al servidor local (puerto 9000). Reemplaza TU_TOKEN_AQUI con tu Global Analysis Token:

```
./mvnw clean verify sonar:sonar -DskipTests -Dsonar.projectKey=backend -Dsonar.host.url=http://localhost:9000 -Dsonar.login=REEMPLAZAR-TOKEN
```

# Análisis Dinámico (DAST) con OWASP ZAP

Se ejecutó un análisis dinámico sobre la aplicación en ejecución utilizando OWASP ZAP, levantado como contenedor Docker en modo *daemon* (sin interfaz gráfica), controlado mediante su API REST. 

- El escaneo generó un reporte HTML con el detalle de los hallazgos, disponible en: 
[`zap-report/reporte-zap-eft.html`](zap-report/reporte-zap-eft.html) 

- El detalle de cómo se levantó está documentado en:[`zap-report/README.md`](zap-report/README.md)


## Control de Acceso Basado en Roles (RBAC) - POSTMAN

En la raíz del proyecto se incluyen las colecciones de Postman para probar los distintos recursos de la API. Cada colección utiliza la variable `{{baseUrl}}` para facilitar la ejecución de las peticiones.

La API está protegida mediante autenticación *Stateless* con JSON Web Tokens (JWT). Los accesos a los endpoints se dividen según el rol del usuario autenticado:

| Recurso / Endpoint | Permiso Global (Lectura - GET) | Permiso Restringido (POST, PUT, DELETE) |
| :--- | :--- | :--- |
| `/login` | Público | Público |
| `/usuarios`, `/register` | Solo `ROLE_ADMIN` | Solo `ROLE_ADMIN` |
| `/patient/**` | Cualquier usuario autenticado | `ROLE_ADMIN`, `ROLE_ASISTENTE` |
| `/appointment/**` | Cualquier usuario autenticado | `ROLE_ADMIN`, `ROLE_ASISTENTE` |
| `/medication/**` | Cualquier usuario autenticado | `ROLE_ADMIN`, `ROLE_DOCTOR` |
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

