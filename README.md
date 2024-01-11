# README para o Projeto DynamicWeb

# Configuração do Projeto DynamicWeb

Este guia fornece instruções detalhadas sobre como configurar e executar o projeto DynamicWeb em seu ambiente local.

## Pré-Requisitos
Antes de começar, certifique-se de ter o seguinte software instalado:
- JDK 17
- Gradle 8.3

## Clonando o Projeto
1. Clone o repositório do projeto para sua máquina local usando o seguinte comando:
git clone https://github.com/maximianoneto/DynamicWeb.git

## Configuração do Ambiente
2. Crie um arquivo `.env` na raiz do projeto com a seguinte variável de ambiente:
OPENAI_API_KEY="Sua-Chave-De-Api-OpenAI"

## Construindo o Projeto
1. Abra um terminal e navegue até o diretório do projeto clonado.
2. Execute o seguinte comando para construir o projeto:
./gradlew build

## Executando a Aplicação
1. Após a construção bem-sucedida, execute o seguinte comando para iniciar a aplicação:
./gradlew bootRun

## Dependências do Projeto
O projeto DynamicWeb utiliza várias dependências chave para seu funcionamento correto, incluindo:

- **Spring Boot Starter Web**: Fornece todas as dependências necessárias para construir uma aplicação web, incluindo Spring MVC e Tomcat como servidor embutido.
- **Spring Boot Starter HATEOAS**: Auxilia na criação de APIs RESTful que seguem o princípio HATEOAS (Hypermedia As The Engine Of Application State).
- **GSON**: Biblioteca do Google para a conversão de objetos Java em JSON e vice-versa, facilitando o trabalho com dados no formato JSON.
- **OkHttp3**: Um cliente HTTP eficiente para realizar chamadas de API, oferecendo recursos como conexões persistentes, compressão GZIP e cache de resposta.
- **Java-dotenv**: Biblioteca para gerenciar variáveis de ambiente, tornando mais fácil a configuração de chaves API e outras configurações sensíveis.
- **Lombok**: Uma biblioteca que usa anotações para minimizar o código boilerplate em Java, especialmente para modelos de dados (getters, setters, equals, hashCode, toString).
- **Spring Boot DevTools**: Ferramentas de desenvolvimento para Spring Boot, que incluem recursos como reinicialização automática do servidor e configurações de desenvolvimento padrão.

## Rotas do Controller
URL: localhost:8080

A aplicação expõe as seguintes rotas através do `CodeController`:

### 1. Criar Thread
- **Endpoint**: `POST /createThread`
- **Descrição**: Cria uma nova thread com uma mensagem inicial.
- **Payload**: `{"initialMessage": "<mensagem_inicial>"}`
- **Exemplo**: `{
  "initialMessage": "Crie uma rota para cadastrar produto."
  }`

### 2. Enviar Run em Thread Específica
- **Endpoint**: `POST /sendRun`
- **Descrição**: Envia um 'run' para uma thread específica.
- **Payload**: `{"threadId": "<id_da_thread>", "assistantId": "<id_do_assistente>"}`
- **Exemplo**: `{
  "threadId":"thread_GjGnQ1SzvgwzPuQ2Yb9lOa47",
  "assistantId":"asst_4CNiCruDBEsbpa2TClnC0qyo"
  }`

### 3. Obter Mensagens da Thread
- **Endpoint**: `GET /getThreadMessages`
- **Descrição**: Recupera mensagens de uma thread específica.
- **Payload**: `{"payload": "<id_da_thread>"}`
- **Exemplo**: `{
  "payload":"thread_GjGnQ1SzvgwzPuQ2Yb9lOa47"
  }`


### 4. Analisar Imagem
- **Endpoint**: `POST /analyze`
- **Descrição**: Envia uma imagem para análise usando a API da OpenAI.
- **Payload**: `{"payload": "<caminho_da_imagem>", "message": "<mensagem>"}`
- **Exemplo**: `{
  "payload":"C:\\Users\\User\\IdeaProjects\\DynamicWeb\\src\\main\\resources\\images\\prototipo-low-fidelity.PNG",
  "message": "Gere o código completo referente a imagem usando HTML, CSS, Javascript"   
  }`


## Como Executar
Para executar a aplicação, certifique-se de ter o JDK 17 instalado e configurado corretamente. Após clonar o repositório, você pode executar a aplicação usando o Gradle ou Maven.

## Conclusão
Este README fornece uma visão geral do projeto DynamicWeb. Para detalhes mais específicos sobre a configuração e execução, consulte a documentação interna do projeto e os comentários no código-fonte.
