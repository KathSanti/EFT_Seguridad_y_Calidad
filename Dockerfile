# ---- Etapa 1: build del jar con Maven ----
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests

# ---- Etapa 2: imagen de ejecución ----
FROM eclipse-temurin:17-jre
WORKDIR /app

RUN addgroup --system spring && adduser --system --ingroup spring spring
COPY --from=build /app/target/*.jar app.jar
RUN chown spring:spring app.jar
USER spring

EXPOSE 8180
ENTRYPOINT ["java", "-jar", "app.jar"]
