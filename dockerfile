# Use a imagem base do Java 17
FROM openjdk:17-jdk-slim as build

# Instalar dependências necessárias
WORKDIR /app

# Instala o Node.js e o npm
RUN apt-get update && \
    apt-get install -y curl && \
    curl -sL https://deb.nodesource.com/setup_14.x | bash - && \
    apt-get install -y nodejs


# Copia o resto do seu projeto
COPY . .

ENTRYPOINT  ["java", "-jar", "build/libs/DynamicWeb-0.0.1.jar"]

# Expor a porta em que sua aplicação Spring Boot irá rodar
EXPOSE 8080

