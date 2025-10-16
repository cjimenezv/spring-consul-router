# Usamos la imagen oficial de OpenJDK 21
FROM openjdk:21-jdk-slim

# Metadata
LABEL maintainer="carlos.jimenez@simonmovilidad.com"
LABEL version="1.0"
LABEL description="Spring Cloud Proxy para Krakend y Consul"

# Directorio de trabajo dentro del contenedor
WORKDIR /app

# Copiamos el JAR construido al contenedor
COPY target/*.jar app.jar

# Exponemos el puerto que Krakend va a usar
EXPOSE 8081

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]