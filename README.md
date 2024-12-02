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

## Configurando o Assistant pela GUI da OPENAI
1. Entre no site da OpenAI https://platform.openai.com/playground/
2. Va na aba lateral em Assistants
3. Clique em Create New Assistant
4. Defina o nome do assistant, em baixo do nome contera o ID do assistant que referenciaremos no arquivo .env (Exemplo: asst_HJrsyC7ZLKCJuwT15ygF8JAb)
5. Em System Instructions coloque como seu assistant ira atuar.
6. Exemplo para um assistant de Java:
7. Role: Você vai atuar como um especialista em criação de projetos Java 17 ou Java 21 com os seguintas tecnologias abaixo e você prioritamente não devolverá texto explicando o procedimento mas atuando conforme as etapas abaixo:
   Na sua primeira iteração você devolverá comando para criação de projeto no terminal,  a arvore de diretórios de acordo com sua iteração inicial e após isso você atuará recebendo história de usuário e critério de aceitação para produzir o código com base na história de usuário e o critério de aceitação.
   Quando produzir código se atentar ao package da classe para ser igual ao package do projeto gerado, verificar arvores de diretórios.
   Se você utilizar de alguma dependência que não está inclusa no projeto base para produzir código, você deve informar a utilização da dependencia como sua versão e devolver todo o código referente ao build.gradle ou maven. Quando for build.gradle utilizar do pattern de regex: ```gradle ou ```maven.
   Para cada comando que será necessário rodar no terminal, você deve utilizar o pattern a seguir para que pegue somente um comando por vez. Pattern de regex para identificação do comando como exemplo: ```cmd\n(.+?)\n```
   Quanto voce for rodar um comando para criação de projeto java, você utilizará o Spring CLI usando o exemplo como base:
   Exemplo: spring init --dependencies=web,lombok --build=gradle --java-version=17 --boot-version=3.2.0 --type=gradle-project --name=centralparkhotel centralparkhotel

8. O exemplo acima contem Instruçoes Comportamentais de como assistant deve atuar.
9. Sugestao: Utilize o exemplo acima a fim de testar o funcionamento da Aplicacao.
10. Ainda na interface de assistant da OpenAI, selecione o Model que ira atuar como assistant. 
11. Exemplo: gpt-4-turbo
12. Salve o assistant e copie o ID do assistant que fica abaixo do seu nome.
13. Crie um arquivo .env na raiz do projeto contendo:
14. OPENAI_API_KEY=chave-da-open-ai
15. TEST_REACT_ID=asst_g9KCev8WzHY1zJT9cXR2IS0i
16. TEST_NODE_ID=asst_0hvUbB6SiNxBQBZEzWSAWJHb
17. PYTHON_ID=asst_ffpgT4f0i3K7Wt35SqOp6uww
18. REACT_ID=asst_OxHBt8GMEc3x4N8QPqi0wrma
19. JAVA_ID=asst_P1Mlu6C8nZBevGH0yvX5aK35 // Cole o ID do assistant respectivo a sua linguagem de atuaçao
20. NEXT_ID=asst_WQoe8Myj09wtB3vYWig0FXJb // Caso nao use outro assistant apenas preencha com String vazia
21. NODE_ID=asst_8VMJsRU9b57pgrTVxGMkYb5r

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
- **Payload**: `{
  "projectName": "Padaria",
  "programmingLanguage": "Java",
  "versionOfProgrammingLanguage": "17",
  "framework": "Spring Boot 3.3.0",
  "dependencyManager": "Gradle",
  "additionalDependencies": "Mockito"
  }
`

### 2. Retorna uma lista de Mensagens de uma ThreadId
- **Endpoint**: `GET localhost:8080/api/getThreadMessages?threadId=threadId`
- **Descrição**: Recupera mensagens de uma thread específica.
- **Curl**: `curl --location 'localhost:8080/api/getThreadMessages?threadId=threadId'`

### 3. Adiciona uma Mensagem a uma ThreadId
- **Endpoint**: `POST localhost:8080/api/addMessageToThread`
- **Curl**: `curl --location 'localhost:8080/thread/addMessage' \
--header 'Content-Type: application/json' \
--data '{
  "threadId": "thread123",
  "message": "User Story 1: Employee Authentication As a hostel employee, I want to securely log in to the hostel management system, So that I can access the customer information and perform my duties. Acceptance Criteria: The login screen must have input fields for the username and password. After entering credentials, an employee should be able to log in by clicking the '\''Sign in'\'' button. The system should handle authentication and display an error message if the login fails.",
  "featureDependsBackend": true,
  "projectName": "HostelManagementSystem"
}'
`

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
