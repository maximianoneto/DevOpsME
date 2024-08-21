# README

## Clonando o Projeto
1. Clone o repositório do projeto para sua máquina local usando o seguinte comando:
   ```bash
   git clone https://github.com/maximianoneto/DynamicWeb.git

## Construindo o Projeto
1. Abra um terminal e navegue até o diretório do projeto clonado.
2. Execute o seguinte comando para construir o projeto:
  ```bash 
   ./gradlew build 
   ```  

# Configuração do Projeto DynamicWeb

## Rodando a aplicação utilizando docker
1. Crie um arquivo `.env` na raiz do projeto com a seguinte variável de ambiente:
 ```bash 
  OPENAI_API_KEY=Sua-Chave-De-Api-OpenAI
  ```
   
2. Abra o terminal e navegue até a raiz do projeto e depois execute o comando abaixo:
<pre><code>docker build -t dockerfile .</code></pre>

3. Após isso rode a aplicação utilizando o comando:
<pre><code>docker run -p 8080:80 -e OPENAI_API_KEY=Sua-Chave-De-Api-OpenAI dockerfile</code></pre>

## Pré-Requisitos para Rodar o projeto Localmente
Antes de começar, certifique-se de ter o seguinte software instalado:
- JDK 17
- Gradle 8.3

## Dependências do Projeto
O projeto DynamicWeb utiliza várias dependências chave para seu funcionamento correto em um ambiente local.
Utilize do arquivo dockerfile para instalar as dependencias necessárias na sua máquina.

# Use a imagem base do Java 17
FROM openjdk:17-jdk-slim as build

# Instale o Node.js, npm e outras dependências necessárias
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

## Rotas do Controller
URL: localhost:8080/

SWAGGER: http://localhost:8080/swagger-ui/index.html#/

A aplicação expõe as seguintes rotas através do `ThreadController`:

### 1. Criar Thread
- **Endpoint**: `POST localhost:8080/api/createThread`
- **Descrição**: Cria uma nova thread com uma mensagem inicial.
- **Payload**: `{"initialMessage": "<mensagem_inicial>"}`
- **Exemplo**: `{
  "initialMessage": "nome:Padaria, Linguagem de Programação: Java 17, Framework: Spring Boot, Dependency Manager: gradle, Adicional Dependencies: mockito"
  }`

### 2. Retorna uma lista de Mensagens de uma ThreadId
- **Endpoint**: `GET localhost:8080/api/getThreadMessages?threadId=threadId`
- **Descrição**: Recupera mensagens de uma thread específica.
- **Curl**: `curl --location 'localhost:8080/api/getThreadMessages?threadId=threadId'`

### 3. Adiciona uma Mensagem a uma ThreadId
- **Endpoint**: `POST localhost:8080/api/addMessageToThread`
- **Curl**: `curl --location 'localhost:8080/api/addMessageToThread' \
  --header 'Content-Type: application/json' \
  --data '{
  "threadId": "threadId",
  "message": "User Story 1: Employee Authentication As a hostel employee,I want to securely log in to the hostel management system,So that I can access the customer information and perform my duties.Acceptance Criteria:The login screen must have input fields for the username and password.After entering credentials, an employee should be able to log in by clicking the '\''Sign in'\'' button.The system should handle authentication and display an error message if the login fails."
  }'`

Rotas referente ao `ProjectController`:

### 4. Cria um projeto atrelado a um threadId
- **Endpoint**: `POST /api/createProject`
- **Curl**: `curl --location 'localhost:8080/api/createProject' \
  --header 'Content-Type: application/json' \
  --data '{
  "threadId":"threadId"
  }'`

### 5. Download do Projeto em arquivo .zip
- **Endpoint**: `GET localhost:8080/api/downloadProject?projectName=padaria`
- **Curl**: `curl --location 'localhost:8080/api/downloadProject?projectName=padaria'`

Rotas referente ao `CodeController`:

### 6. Adiciona código a um projeto atrelado a um threadId
- **Endpoint**: `POST /api/createProject`
- **Descrição**: Recupera mensagens de uma thread específica.
- **Curl**: `curl --location 'localhost:8080/api/addCode' \
  --header 'Content-Type: application/json' \
  --data '{
  "threadId":"threadId"
  }'`

### 7. Analisa Protótipo de baixa fidelidade e retorna o código referente
- **Endpoint**: `POST /analyze`
- **Descrição**: Envia uma imagem para análise usando a API da OpenAI.
- **Curl**: `curl --location 'localhost:8080/api/analyze' \
  --form 'imageFile=@"/C:/Users/Max/Documents/prototipo-low-fidelity.PNG"' \
  --form 'message="Gere o código em html, css, javascript"'`


Rotas referente ao `SpringCLIController`:

### 9. Retorna a lista de dependências atualizadas do Spring
- **Endpoint**: `GET localhost:8080/api/spring/dependencies`
- **Curl**: `curl --location 'localhost:8080/api/spring/dependencies'`
- **OBS**: Para o funcionamento correto dessa rota a instalação do Spring CLI é necessária.

Rotas referente ao `GitHubController`:

### 10. Cria um novo repositório no GitHub e faz commit dos arquivos
- **Endpoint**: `POST localhost:8080/github/repository`
- **Curl**: `curl --location 'localhost:8080/github/repository' \
  --header 'Content-Type: application/json' \
  --data '{
  "projectName": "MyNewProject",
  "projectDescription": "This is a new project created via API"
  }'`
- **Payload**: `{
  "projectName": "<nome_do_projeto>",
  "projectDescription": "<descrição_do_projeto>"
  }` 

Rotas referente ao `LocalGitController`:

### 11. Inicializar Repositório Git Local
- **Endpoint**: `POST localhost:8080/git/initialize`
- **Curl**: `curl --location 'localhost:8080/git/initialize?projectName=MyNewProject'`


### 12. Comitar Alterações no Repositório Git Local
- **Endpoint**: `POST localhost:8080/git/commit`
- **Curl**: `curl --location 'localhost:8080/git/commit?projectName=MyNewProject&commitMessage=Initial%20commit`

### 13. Reverter para um Commit Anterior no Repositório Git Local
- **Endpoint**: `POST localhost:8080/git/rollback`
- **Curl**: `curl --location 'localhost:8080/git/rollback?projectName=MyNewProject&commitId=abc123'`
