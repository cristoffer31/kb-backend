# --- ETAPA 1: COMPILAR EL PROYECTO ---
# Usamos una imagen que ya tiene Maven instalado para no depender de tu PC
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copiamos los archivos de configuración
COPY pom.xml .
COPY src ./src

# Ejecutamos la compilación (esto creará la carpeta /target dentro de Railway)
RUN mvn package -DskipTests

# --- ETAPA 2: EJECUTAR LA APP ---
# Usamos una imagen ligera de Java para correr lo que compilamos arriba
FROM registry.access.redhat.com/ubi9/openjdk-21-runtime:1.20
ENV LANGUAGE='en_US:en'

# Copiamos los archivos compilados desde la Etapa 1
COPY --from=build --chown=185 /app/target/quarkus-app/lib/ /deployments/lib/
COPY --from=build --chown=185 /app/target/quarkus-app/*.jar /deployments/
COPY --from=build --chown=185 /app/target/quarkus-app/app/ /deployments/app/
COPY --from=build --chown=185 /app/target/quarkus-app/quarkus/ /deployments/quarkus/

EXPOSE 8080
USER 185
ENV JAVA_OPTS_APPEND="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENV JAVA_APP_JAR="/deployments/quarkus-run.jar"

ENTRYPOINT [ "/opt/jboss/container/java/run/run-java.sh" ]