# ---- Build stage: compile the jar with Maven ----
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

# Copy just the wrapper + POM first so dependency resolution is cached in its own
# layer — it only re-runs when pom.xml actually changes, not on every source edit.
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B dependency:go-offline

COPY src ./src
RUN ./mvnw -B clean package -DskipTests

# ---- Run stage: lightweight JRE only, no Maven/JDK shipped ----
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Run as a non-root user rather than the image's default root.
RUN addgroup -S spring && adduser -S spring -G spring
COPY --from=build /app/target/*.jar app.jar
USER spring:spring

EXPOSE 8080

# Backed by /actuator/health (permitted anonymously in SecurityConfig) — docker-compose's
# backend->mysql dependency uses this, via the container's own health status, to know the
# app actually finished starting (DB connection included), not just that the JVM launched.
# 127.0.0.1, not localhost: Tomcat only binds the IPv4 wildcard, but "localhost" can resolve
# to ::1 first inside the container — using the loopback IP directly sidesteps that instead of
# depending on getaddrinfo's ordering.
HEALTHCHECK --interval=10s --timeout=5s --start-period=45s --retries=6 \
    CMD wget -qO- http://127.0.0.1:8080/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
