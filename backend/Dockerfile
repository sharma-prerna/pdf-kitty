# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
# Cache dependencies first
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
# Build (skip tests in the image; tests run in CI / locally with Docker available)
COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre
WORKDIR /app
# wget: docker-compose healthcheck. libreoffice-writer + fonts: headless Word -> PDF
# conversion (JODConverter drives the bundled soffice binary).
RUN apt-get update && apt-get install -y --no-install-recommends \
        wget \
        libreoffice-writer \
        libreoffice-java-common \
        fonts-liberation \
        fontconfig \
    && rm -rf /var/lib/apt/lists/* \
    && useradd -r -u 1001 -m -d /home/appuser appuser \
    && mkdir -p /data/storage && chown -R appuser:appuser /data
COPY --from=build /app/target/pdf-toolkit-backend.jar app.jar
USER appuser
# LibreOffice needs a writable HOME for its per-user profile.
ENV HOME=/home/appuser
EXPOSE 8080
ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
