# Use a imagem base do Java 17
FROM openjdk:17-jdk-slim

# Instala dependências necessárias
RUN apt-get update && \
    apt-get install -y curl gnupg unzip zip git && \
    curl -sL https://deb.nodesource.com/setup_20.x | bash - && \
    apt-get install -y nodejs && \
    npm install -g vercel && \
    npm install --global yarn && \
    apt-get install -y mariadb-server && \
    rm -rf /var/lib/apt/lists/*

# Instala o Spring Boot CLI
RUN curl -s https://get.sdkman.io | bash && \
    bash -c "source /root/.sdkman/bin/sdkman-init.sh && sdk install springboot"

# Adiciona o Spring Boot CLI ao PATH
ENV PATH="/root/.sdkman/candidates/springboot/current/bin:${PATH}"

# Cria o diretório /Projects
RUN mkdir /Projects

# Define o diretório de trabalho
WORKDIR /app

# Copia o código da sua aplicação
COPY . /app

# Expor as portas
EXPOSE 8081 3306

# Copia o script de entrada
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

# Define o ponto de entrada
ENTRYPOINT ["/entrypoint.sh"]
