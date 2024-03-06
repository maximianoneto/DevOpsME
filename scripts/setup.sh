#!/bin/bash

# Instalar dependências necessárias
apk add --no-cache curl bash nodejs npm

# Instalar SDKMAN!
curl -s "https://get.sdkman.io" | bash

# Instalar Java 17 e Spring Boot CLI usando SDKMAN
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install java 17.0.2-open
sdk install springboot

# Configurar variáveis de ambiente para SDKMAN e Java
export SDKMAN_DIR="$HOME/.sdkman"
export PATH="$SDKMAN_DIR/bin:$SDKMAN_DIR/candidates/java/current/bin:$SDKMAN_DIR/candidates/spring-boot/current/bin:$PATH"
