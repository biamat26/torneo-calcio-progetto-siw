# ─── Stage 1: build ───────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Copia prima solo pom.xml e scarica le dipendenze.
# Docker mette in cache questo layer — se pom.xml non cambia,
# non riscaricare le dipendenze ad ogni build
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
RUN ./mvnw dependency:go-offline -B

# Ora copia i sorgenti e compila
COPY src ./src
RUN ./mvnw package -DskipTests -B


# ─── Stage 2: runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copia solo il JAR dallo stage precedente
COPY --from=builder /app/target/*.jar app.jar

# Porta su cui gira SpringBoot
EXPOSE 8080

# Comando di avvio — attiva il profilo "prod"
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]