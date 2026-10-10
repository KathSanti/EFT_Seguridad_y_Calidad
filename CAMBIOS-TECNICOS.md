# Explicación técnica de los cambios

Este documento resume **qué** se cambió y, sobre todo, **por qué**, para cada modificación hecha sobre el proyecto en esta sesión. No se agregó como comentarios en el código a propósito: queda aquí, aparte, como referencia técnica.

---

## 1. JaCoCo en `pom.xml`

**Qué se hizo:** se agregó la dependencia `org.jacoco:jacoco-maven-plugin:0.8.13` y el plugin correspondiente en `<build><plugins>`, con tres ejecuciones: `prepare-agent` (instrumenta el bytecode antes de correr los tests), `report` (genera el reporte HTML/XML de cobertura) y `check` (falla el build si la cobertura de complejidad ciclomática cubierta es menor a 0.60).

**Por qué:** es el mecanismo estándar para medir cobertura de pruebas en proyectos Maven, y es el que SonarQube consume para mostrar el porcentaje de cobertura en su dashboard. Sin JaCoCo, SonarQube no tiene de dónde leer esa métrica.

---

## 2. Docker y `docker-compose.yaml`

### Problema encontrado
Antes de tocar nada, el flujo descrito en el `README.md` (`docker-compose up -d --build` + `./mvnw spring-boot:run`) tenía tres fallas reales, no solo de pasos:

1. **El `Dockerfile` no se usaba.** Era un Dockerfile que partía de `mysql:8.0` y copiaba `init.sql` a `/docker-entrypoint-initdb.d/` — la forma correcta de precargar datos en MySQL —, pero `docker-compose.yaml` usaba directamente `image: mysql:8.0` sin ningún `build:` que apuntara a ese Dockerfile. Resultado: `init.sql` nunca se ejecutaba y la base subía vacía.
2. **Puertos desalineados.** El compose mapeaba `"3307:3306"` (host:contenedor), pero `application.properties` apuntaba a `jdbc:mysql://localhost:3306/...`. Como el backend corría *fuera* del contenedor (vía `mvnw`), intentaba conectarse a `localhost:3306`, donde no había nada escuchando.
3. **El backend no estaba containerizado.** No existía ningún servicio `backend` en el compose, ni un Dockerfile para la app Java (el único Dockerfile que había era para MySQL).

### Qué se cambió
- Se descartó el Dockerfile custom de MySQL. En su lugar, el servicio `base-de-datos` usa la imagen oficial `mysql:8.0` y monta `init.sql` directamente como volumen de solo lectura: `./init.sql:/docker-entrypoint-initdb.d/init.sql:ro`. Es el enfoque estándar de la imagen oficial de MySQL (ejecuta cualquier `.sql` que encuentre ahí **solo la primera vez** que arranca con el directorio de datos vacío) y evita tener que mantener un build aparte solo para copiar un archivo.
- Se agregó un volumen nombrado `mysql-data` para persistir los datos entre reinicios del contenedor (si no, cada `docker-compose down` sin `-v` perdería el estado, pero cada `down -v` sí lo resetea, que es justo lo que se necesita para forzar que `init.sql` se vuelva a ejecutar tras un cambio de seed data).
- Se alinearon los puertos a `"3306:3306"`.
- Se agregó un `healthcheck` (`mysqladmin ping`) al servicio de MySQL, para que el backend no intente conectarse antes de que la base esté realmente lista (problema clásico de "arranca antes de tiempo" en Compose).
- Se agregó el servicio `backend`, construido desde el nuevo `Dockerfile` multistage, con `depends_on: condition: service_healthy` sobre la base de datos.
- El `Dockerfile` se reescribió por completo como un build multistage real para la app Spring Boot:
  - Etapa 1 (`maven:3.9.9-eclipse-temurin-17`): compila el jar con `mvn clean package -DskipTests`. Se usa la imagen de Maven directamente (en vez de `mvnw`) para no depender de que el wrapper descargue Maven dentro del contenedor.
  - Etapa 2 (`eclipse-temurin:17-jre`): solo copia el jar ya compilado y lo ejecuta. Separar build y runtime mantiene la imagen final liviana (sin el JDK completo ni el repositorio `.m2`).
  - Se agregó un usuario no-root (`spring`) para ejecutar la app, en vez de dejarla correr como `root` (que es el default de la imagen base) — buena práctica de seguridad en contenedores, y además algo que el propio linter de Sonar para Dockerfiles señala como advertencia (`docker:S6471`).
- `application.properties` pasó de tener el host/puerto/usuario/password de la BD hardcodeados a usar variables de entorno con fallback: `${DB_HOST:localhost}`, `${DB_PORT:3306}`, etc. Así el mismo jar funciona tanto corriendo local con `mvnw` (usa los defaults `localhost:3306`) como corriendo dentro del compose (donde se le inyecta `DB_HOST=base-de-datos`, el nombre del servicio en la red interna de Docker).
- Se agregó `.dockerignore` para que el build no copie `.git/`, `target/` ni los `backend.log*` (7.9 MB) al contexto de Docker — esto no cambia el comportamiento, pero evita builds lentos e imágenes infladas.

---

## 3. Contraseñas: de texto plano a BCrypt

**Problema encontrado:** `MyUserDetailsService` tenía configurado `NoOpPasswordEncoder`, es decir, comparaba la contraseña enviada en el login contra la contraseña guardada en la base **tal cual, en texto plano**. Y efectivamente en `init.sql` las contraseñas de los usuarios de prueba estaban en texto plano (`'123'`).

**Por qué es un problema real:** si alguna vez esa base de datos se filtra o alguien con acceso de solo lectura a la BD la ve, tiene las contraseñas de todos los usuarios directamente, sin ningún trabajo adicional. Es la causa #2 más común en brechas de autenticación (OWASP).

**Qué se cambió:**
- El bean `PasswordEncoder` ahora es `BCryptPasswordEncoder` (hash adaptativo con salt por contraseña, el estándar de facto en Spring Security).
- En `init.sql`, las contraseñas de los 4 usuarios de prueba se reemplazaron por su hash BCrypt real (`$2a$10$yU9AaX/Nc4qalt9yeBEjPumRJ06mntrgFAWXudLSy6EOBR9YRDkyG`), generado localmente con la misma versión de `spring-security-crypto` que usa el proyecto — no es un valor inventado ni copiado de internet. El valor en texto plano sigue siendo `123` para los 4 usuarios al momento de loguearse por Postman; lo único que cambió es cómo se guarda y se compara en el servidor.

---

## 4. Rol `ROLE_DOCTOR` y el hueco de seguridad en `/invoice/**`

**Problema encontrado en `WebSecurityConfig`:** había reglas explícitas de autorización por URL para `/patient/**`, `/appointment/**`, `/medication/**` y `/care/**` (GET → cualquier autenticado; el resto → `ROLE_ADMIN`/`ROLE_ASISTENTE`), pero **no existía ninguna regla para `/invoice/**`**. Al no matchear ninguna regla específica, esas peticiones caían al catch-all `.anyRequest().authenticated()`. Como además `InvoiceController.deleteInvoice` no tiene `@PreAuthorize` a nivel de método (ningún método `deleteX` del proyecto lo tiene; todos dependen de la regla de URL), esto significaba que **cualquier usuario autenticado, incluyendo uno con `ROLE_USER`, podía borrar facturas**. Es una falla real de control de acceso (OWASP A01:2021 - Broken Access Control), no una percepción.

**Qué se cambió:**
- Se agregó la regla faltante: `/invoice/**` con GET → autenticado, resto → `ROLE_ADMIN` + `ROLE_ASISTENTE` (mismo patrón que `care`/`appointment`/`patient`).
- Se agregó el rol `ROLE_DOCTOR` (constante `Constants.ROL_DOCTOR`), separando responsabilidades por dominio: `Medication` (qué se prescribe) pasa a ser competencia de `ROLE_ADMIN` + `ROLE_DOCTOR`, mientras que `Invoice` (la facturación) se queda en `ROLE_ADMIN` + `ROLE_ASISTENTE`. Se actualizó tanto la regla de URL de `/medication/**` como el `@PreAuthorize` de `MedicationController.saveMedication` para reflejar esto.
- Se agregó un usuario de prueba `doctor` (contraseña `123`, mismo hash BCrypt que los demás) en `init.sql`.

---

## 5. Datos de ejemplo faltantes en `init.sql` (Patient, Appointment, Invoice)

**Problema encontrado:** `init.sql` solo tenía `CREATE`/`INSERT` para las tablas `user`, `medication` y `care`. Las tablas `patient`, `appointment`, `invoice` (y las tablas intermedias `invoice_cares`/`invoice_medications` que mapean las relaciones `@ManyToMany` de la entidad `Invoice`) no existían en el script en absoluto. Como el proyecto usa `spring.jpa.hibernate.ddl-auto=update`, Hibernate las creaba solas al arrancar — pero sin ninguna fila. Por eso `GET /patient`, `GET /appointment` y `GET /invoice` devolvían `[]` aun cuando la API funcionaba correctamente: no era un bug de la API, era ausencia de datos.

**Qué se cambió:** se agregaron los `CREATE TABLE` y los `INSERT` para los tres módulos, con nombres de tabla/columna que coinciden exactamente con lo que Hibernate genera por convención (nombres de entidad y de campo en `camelCase` se traducen a `snake_case` en minúsculas: `patientName` → `patient_name`, `totalCost` → `total_cost`, etc.), para que no haya conflicto entre lo que crea `init.sql` y lo que esperaría Hibernate si tuviera que crear esas tablas él mismo. Se insertaron 3 pacientes, 3 citas y 3 facturas de ejemplo, estas últimas ya enlazadas (vía las tablas intermedias) a `Care`/`Medication` existentes, con su `total_cost` calculado a mano para que coincida con lo que `InvoiceService.saveInvoice` habría calculado.

**Nota importante:** `init.sql` solo se ejecuta la **primera vez** que el contenedor de MySQL arranca con su volumen de datos vacío. Si ya se había levantado el stack antes de este cambio, hay que correr `docker-compose down -v` una vez para forzar que MySQL reinicialice el volumen y vuelva a correr el script con los datos nuevos.

---

## 6. Manejo global de errores (`ResourceNotFoundException` + `@RestControllerAdvice`)

**Problema encontrado:** ninguno de los servicios devolvía códigos HTTP consistentes cuando algo salía mal:
- Los 5 métodos `getXById` (`Medication`, `Invoice`, `Appointment`, `Patient`, `Care`) usaban `.orElse(null)`: si el id no existía, la respuesta era **200 OK con el body vacío**, no un 404.
- Los métodos `deleteX` llamaban `repository.deleteById(id)` directo. Si el id no existía, Spring Data lanza `EmptyResultDataAccessException`, que al no estar capturada en ningún lado terminaba como un **500 Internal Server Error** genérico — un error de infraestructura por algo que en realidad es un simple "no encontrado".
- `InvoiceService.saveInvoice` lanzaba `IllegalArgumentException` en texto plano si algún `careId`/`medicationId` no existía en la base, lo que también acababa en **500** en vez de un **400 Bad Request** (es un error del cliente, no del servidor).
- `PatientService.disablePatient`, si el id no existía, simplemente no hacía nada y respondía 200 igual, sin indicar que el paciente no existía.
- `LoginController` lanza `BadCredentialsException`/`DisabledException` directo desde el método del controller, sin ningún manejo explícito que garantice una respuesta JSON consistente.

**Qué se cambió:** se creó el paquete `com.duoc.backend.exception` con:
- `ResourceNotFoundException`: excepción custom para "no encontrado", para no depender de excepciones genéricas.
- `ErrorResponse`: un `record` simple con `timestamp`, `status`, `error`, `message` y `path`, para que todas las respuestas de error tengan la misma forma.
- `GlobalExceptionHandler` (`@RestControllerAdvice`): centraliza el mapeo de excepciones a códigos HTTP:
  - `ResourceNotFoundException` y `EmptyResultDataAccessException` → **404**
  - `IllegalArgumentException` → **400**
  - `BadCredentialsException` → **401**
  - `DisabledException` → **403**
  - Cualquier otra excepción no prevista → **500**, con un mensaje genérico (para no filtrar detalles internos / stack traces al cliente).

Los 5 `getXById` se cambiaron de `.orElse(null)` a `.orElseThrow(() -> new ResourceNotFoundException(...))`. `PatientService.disablePatient` ahora también lanza `ResourceNotFoundException` si el id no existe, en vez de fallar en silencio. Los métodos `deleteX` **no se tocaron**: la excepción que ya lanzan (`EmptyResultDataAccessException`) quedó directamente mapeada en el handler, así que no hacía falta duplicar lógica de "verificar que existe" antes de borrar en cada servicio.

---

## 7. `CrudRepository` → `JpaRepository`

**La pregunta que lo originó:** ¿afecta en algo al manejo de errores que los repositorios extiendan `CrudRepository` en vez de `JpaRepository`?

**Respuesta (y por qué se hizo el cambio igual):** no afecta en nada al manejo de errores. En un proyecto con `spring-boot-starter-data-jpa` y entidades `@Entity`, Spring Boot siempre instancia en runtime la misma implementación concreta (`SimpleJpaRepository`) sin importar si la interfaz que uno declara es `CrudRepository` o `JpaRepository` — son solo interfaces marcador en una cadena de herencia (`JpaRepository` → `PagingAndSortingRepository` → `CrudRepository`). Por eso `deleteById()` lanza la misma `EmptyResultDataAccessException` en ambos casos.

La razón real para cambiar fue otra, que se detectó de paso: varios controllers y servicios hacían un **cast no verificado** de `Iterable` a `List`, como `return (List<Appointment>) appointmentService.getAllAppointments();`. Ese cast "funcionaba" en runtime solo porque `SimpleJpaRepository.findAll()` efectivamente devuelve una `List` por debajo, pero la firma de `CrudRepository.findAll()` declara `Iterable<T>`, no `List<T>` — el compilador no puede garantizar ese cast, es responsabilidad del programador que puede romperse si cambia la implementación. `JpaRepository.findAll()` sí declara `List<T>` directamente en su firma, así que al cambiar las 6 interfaces de repositorio (`User`, `Appointment`, `Care`, `Patient`, `Medication`, `Invoice`) se pudieron eliminar todos esos casts de los controllers/servicios correspondientes. Es el tipo de hallazgo que SonarQube también suele marcar como code smell.

---

## 8. Colección de Postman

Se creó la carpeta `postman/` con una colección (`EFT-Seguridad-y-Calidad.postman_collection.json`) y un environment (`EFT-Seguridad-y-Calidad.postman_environment.json`), organizada en 7 carpetas (`Auth`, `Usuarios`, `Patient`, `Appointment`, `Medication`, `Care`, `Invoice`) que reflejan exactamente las reglas de `WebSecurityConfig` después de los cambios de este documento (incluye login para los 4 roles: admin, asistente, doctor, cliente). El login guarda el JWT en una variable de colección que el resto de las carpetas usa automáticamente vía Bearer Auth heredado, y los `POST` de creación guardan el id del recurso creado para encadenar los `GET`/`PUT`/`DELETE` siguientes sin tener que copiar valores a mano.

---

## 9. Secreto JWT hardcodeado → variable de entorno

**Problema encontrado:** un análisis de SonarQube marcó `Constants.java` con un *Security Hotspot* de probabilidad **HIGH** (regla `java:S6418`, categoría `auth`): la constante `SUPER_SECRET_KEY`, usada para firmar y verificar los tokens JWT, estaba escrita directamente en el código fuente. Cualquiera con acceso al repositorio (o a un `git log` antiguo) podía leer la clave y firmar tokens JWT válidos para cualquier rol, sin necesitar credenciales.

**Qué se cambió:**
- Se eliminó `Constants.SUPER_SECRET_KEY` y se agregó `jwt.secret=${JWT_SECRET:<mismo-valor-por-defecto>}` en `application.properties` (mismo patrón ya usado para `DB_HOST`/`DB_PORT`: funciona igual sin configurar nada en local, pero es sobreescribible por variable de entorno en cualquier despliegue real).
- `JWTAuthenticationConfig` y `JWTAuthorizationFilter` (las dos únicas clases que usaban la clave) pasaron a recibirla por inyección de constructor con `@Value("${jwt.secret}")`, en vez de importarla como constante estática.
- Se reconstruyó y redesplegó el contenedor del backend, y se verificó con `curl` que login + verificación de token seguían funcionando igual.
- Se volvió a correr el análisis de Sonar: el hotspot de `Constants.java` ya no aparece (de 3 hotspots totales bajó a 2, los 2 restantes son de CSRF, ya justificados en el `README.md`).

---

## 10. Bugs reales encontrados al escribir los tests (no hipotéticos)

Al verificar manualmente con `curl` los endpoints después de agregar el `GlobalExceptionHandler`, aparecieron **2 regresiones reales** que no se habían detectado antes:

1. **El catch-all `@ExceptionHandler(Exception.class)` interceptaba excepciones que Spring ya manejaba bien por defecto.** Antes del `GlobalExceptionHandler`, un parámetro faltante en `/login` (`MissingServletRequestParameterException`) o una validación `@Valid` fallida (`MethodArgumentNotValidException`) ya devolvían 400 automáticamente. Al agregar el catch-all genérico, ambas pasaron a responder **500** en vez de 400, porque el catch-all las capturaba primero. **Corrección:** se agregaron handlers específicos para `MissingServletRequestParameterException` y `MethodArgumentNotValidException` (este último devuelve el campo y mensaje de la validación que falló), que por ser más específicos que `Exception.class` tienen prioridad en la resolución de Spring.
2. **`repository.deleteById(id)` no lanza excepción si el id no existe**, en la versión de Spring Data JPA que usa este proyecto (3.4.4, la que trae `spring-boot-starter-parent:3.4.4`). La suposición original (que `SimpleJpaRepository.deleteById()` lanza `EmptyResultDataAccessException` cuando no encuentra el registro) resultó ser incorrecta para esta versión: simplemente no hace nada y el endpoint respondía **200 OK** aunque no se hubiera borrado nada. Se verificó con los logs de Hibernate (un `SELECT` sin `DELETE` posterior). **Corrección:** los 4 métodos `deleteX` (`Care`, `Medication`, `Appointment`, `Invoice`) ahora verifican `existsById(id)` antes de borrar, y lanzan `ResourceNotFoundException` si no existe.

Ambos se confirmaron corrigiendo el código, reconstruyendo el contenedor y volviendo a probar con `curl` contra el backend real corriendo en Docker (no solo en tests).

---

## 11. Suite de pruebas automatizadas (JUnit 5 + Mockito)

**Motivo:** el primer análisis de Sonar después de los cambios mostró el Quality Gate en **ERROR**, exclusivamente por la condición `new_coverage ≥ 80%` del gate por defecto ("Sonar Way") — el proyecto no tenía tests reales, solo un `contextLoads()` vacío.

**Qué se agregó:** 77 tests en 15 clases, usando JUnit 5 + Mockito + Spring `MockMvc` (ya incluidos en `spring-boot-starter-test`, sin dependencias nuevas):

- **Tests de Service** (`PatientServiceTest`, `AppointmentServiceTest`, `MedicationServiceTest`, `InvoiceServiceTest`): mockean el repositorio y verifican casos de éxito, `ResourceNotFoundException` cuando el id no existe, y en `InvoiceServiceTest` específicamente el cálculo de `totalCost` y las validaciones de ids de `Care`/`Medication` inexistentes.
- **Tests de Controller** (`PatientControllerTest`, `AppointmentControllerTest`, `MedicationControllerTest`, `CareControllerTest`, `InvoiceControllerTest`, `UserControllerTest`): `@WebMvcTest` + `@AutoConfigureMockMvc(addFilters = false)` (desactiva los filtros de seguridad para aislar la lógica del controller) + `@MockitoBean` para el service/repository. Verifican código HTTP, serialización JSON y que las validaciones `@Valid` de los DTO devuelvan 400.
- **`GlobalExceptionHandlerTest`**: invoca directamente cada uno de los 7 métodos del handler (sin contexto de Spring) y verifica el código HTTP que produce cada uno.
- **Autenticación**: `MyUserDetailsServiceTest` (carga de usuario + que el `BCryptPasswordEncoder` realmente verifique bien/mal una contraseña), `JWTAuthenticationConfigTest` (genera un token y lo verifica con la misma clave), `LoginControllerTest` (login correcto, credenciales malas → `BadCredentialsException`, cuenta deshabilitada → `DisabledException`).
- Se usó `@MockitoBean` (no `@MockBean`, deprecado desde Spring Boot 3.4) en los tests de Controller.
- Se le agregó una aserción real a `BackendApplicationTests.contextLoads()` (antes no tenía ninguna — era el único *Code Smell* de severidad Blocker que marcaba Sonar).

**Resultado medido:**
- JaCoCo (el `check` de 60% ya configurado en el `pom.xml`): **88% de cobertura por complejidad** → `mvn verify` pasa.
- SonarQube: cobertura total del proyecto **86.6%**, cobertura de código nuevo **96.6%** (umbral del gate: 80%) → **Quality Gate pasó de ERROR a OK**.

---

## 12. Diagnóstico: por qué Sonar a veces muestra 0% de cobertura

Después de lograr que el Quality Gate pasara, una corrida posterior de Sonar volvió a mostrar **Coverage 0.0%** y Quality Gate **Failed**. Se verificó que `target/jacoco.exec` y `target/site/jacoco/jacoco.xml` no existían en el momento del análisis.

**Causa:** JaCoCo solo genera `jacoco.exec` cuando los tests realmente se ejecutan (fase `test` de Maven), y `sonar:sonar` solo encuentra el reporte de cobertura si `target/site/jacoco/jacoco.xml` ya existe **en el mismo build**. Si se corre `mvn clean` y luego, en un comando aparte, `mvn sonar:sonar` (sin volver a correr los tests en medio), el `clean` borra el `.exec` de la corrida anterior y Sonar no tiene de dónde leer cobertura — reporta 0% aunque el código sí tenga tests.

**Regla a seguir siempre:** correr `clean`, `verify` (o `test`) y `sonar:sonar` **juntos, en una sola invocación**:
```
./mvnw clean verify sonar:sonar -Dsonar.projectKey=backend -Dsonar.host.url=http://localhost:9000 -Dsonar.login=<TOKEN>
```
