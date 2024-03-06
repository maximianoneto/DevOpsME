# Use a imagem base do Java 17
FROM openjdk:17-jdk-slim as build

RUN mkdir projects

# Define o diretório de trabalho
WORKDIR /app

# Instala Node.js, npm e outras dependências necessárias
RUN apt-get update && \
    apt-get install -y curl gnupg unzip zip git && \
    curl -sL https://deb.nodesource.com/setup_20.x | bash - && \
    apt-get install -y nodejs && \
    npm install -g vercel && \
    npm install --global yarn

# Instalar o Spring Boot CLI
RUN curl -s "https://get.sdkman.io" | bash && \
    bash -c "source $HOME/.sdkman/bin/sdkman-init.sh && sdk install springboot" && \
    ln -s "$HOME/.sdkman/candidates/springboot/current/bin/spring" /usr/local/bin/spring

# Copia o resto do seu projeto
COPY . .

# Define o ponto de entrada da aplicação
ENTRYPOINT ["java", "-jar", "build/libs/DynamicWeb-0.0.1.jar"]

# Expor a porta em que sua aplicação Spring Boot irá rodar
EXPOSE 8080