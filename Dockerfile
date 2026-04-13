# ── Etapa 1: Build ──────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Copia primero el pom.xml para aprovechar la caché de dependencias
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia el código fuente y compila
COPY src ./src
RUN mvn clean package -DskipTests -B

# ── Etapa 2: Runtime ─────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copia el .jar generado en la etapa de build
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
