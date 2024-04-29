package com.tcc.dynamicweb.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.*;
import com.tcc.dynamicweb.model.Assistant;
import com.tcc.dynamicweb.repository.AssistantRepository;
import com.tcc.dynamicweb.repository.ProjectRepository;
import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.tcc.dynamicweb.model.Assistant.AssistantType.CODE_GENERATOR;
import static com.tcc.dynamicweb.model.Assistant.AssistantType.TEST_GENERATOR;

@Service
public class ThreadService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String openAiBaseUrl = "https://api.openai.com/v1";
    private final Dotenv dotenv = Dotenv.configure().load();

    private static final Logger logger = LoggerFactory.getLogger(CodeService.class);
    private final ConcurrentHashMap<String, String> projectPaths = new ConcurrentHashMap<>();

    private Map<String, String> threadProjectMap = new HashMap<>();

    private Map<String, String> assistantMap = new HashMap<>();

    private Map<String, String> threadIdMap = new HashMap<>();

    @Autowired
    private AssistantRepository assistantRepository;

    @Autowired
    private ProjectRepository projectRepository;

    public String createThread(String initialMessageContent) {
        HttpHeaders headers = createHeaders();
        JsonObject messageObj = new JsonObject();
        messageObj.addProperty("role", "user");
        messageObj.addProperty("content", initialMessageContent);

        JsonArray messages = new JsonArray();
        messages.add(messageObj);

        JsonObject threadBody = new JsonObject();
        threadBody.add("messages", messages);

        HttpEntity<String> entity = new HttpEntity<>(threadBody.toString(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(openAiBaseUrl + "/threads", entity, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            JsonObject responseBody = JsonParser.parseString(Objects.requireNonNull(response.getBody())).getAsJsonObject();
            String threadId = responseBody.get("id").getAsString();

            String projectName = extractProjectName(initialMessageContent);
            // Armazenamento do threadId e projectName no HashMap
            threadProjectMap.put(threadId, projectName);

            if (initialMessageContent.contains("testReact") || initialMessageContent.contains("TestReact")) {
                assistantMap.put(threadId, "asst_g9KCev8WzHY1zJT9cXR2IS0i");
                sendRun(threadId, "asst_g9KCev8WzHY1zJT9cXR2IS0i");
                Assistant assistant = new Assistant();
                assistant.setAssistantId("asst_g9KCev8WzHY1zJT9cXR2IS0i");
                assistant.setThreadId(threadId);
                assistant.setType(TEST_GENERATOR);
                assistantRepository.save(assistant);
                threadIdMap.put(projectName, threadId);
                return threadId;
            } else if (initialMessageContent.contains("testNode") || initialMessageContent.contains("TestNode")) {
                assistantMap.put(threadId, "asst_0hvUbB6SiNxBQBZEzWSAWJHb");
                sendRun(threadId, "asst_0hvUbB6SiNxBQBZEzWSAWJHb");
                Assistant assistant = new Assistant();
                assistant.setAssistantId("asst_0hvUbB6SiNxBQBZEzWSAWJHb");
                assistant.setThreadId(threadId);
                assistant.setType(TEST_GENERATOR);
                assistantRepository.save(assistant);
                threadIdMap.put(projectName, threadId);
                return threadId;
            } else if (initialMessageContent.contains("python") || initialMessageContent.contains("Python")) {
                assistantMap.put(threadId, "asst_ffpgT4f0i3K7Wt35SqOp6uww");
                sendRun(threadId, "asst_ffpgT4f0i3K7Wt35SqOp6uww");
                Assistant assistant = new Assistant();
                assistant.setAssistantId("asst_ffpgT4f0i3K7Wt35SqOp6uww");
                assistant.setThreadId(threadId);
                assistant.setType(CODE_GENERATOR);
                assistantRepository.save(assistant);
                threadIdMap.put(projectName, threadId);
                return threadId;
            }else if (initialMessageContent.contains("react") || initialMessageContent.contains("React")) {
                assistantMap.put(threadId, "asst_OxHBt8GMEc3x4N8QPqi0wrma");
                sendRun(threadId, "asst_OxHBt8GMEc3x4N8QPqi0wrma");
                Assistant assistant = new Assistant();
                assistant.setAssistantId("asst_OxHBt8GMEc3x4N8QPqi0wrma");
                assistant.setThreadId(threadId);
                assistant.setType(CODE_GENERATOR);
                assistantRepository.save(assistant);
                return threadId;
            } else if (initialMessageContent.contains("java") || initialMessageContent.contains("Java")) {
                assistantMap.put(threadId, "asst_P1Mlu6C8nZBevGH0yvX5aK35");
                sendRun(threadId, "asst_P1Mlu6C8nZBevGH0yvX5aK35");
                Assistant assistant = new Assistant();
                assistant.setAssistantId("asst_P1Mlu6C8nZBevGH0yvX5aK35");
                assistant.setThreadId(threadId);
                assistant.setType(CODE_GENERATOR);
                assistantRepository.save(assistant);
                return threadId;
            } else if (initialMessageContent.contains("next") || initialMessageContent.contains("Next")) {
                assistantMap.put(threadId, "asst_WQoe8Myj09wtB3vYWig0FXJb");
                sendRun(threadId, "asst_WQoe8Myj09wtB3vYWig0FXJb");
                Assistant assistant = new Assistant();
                assistant.setAssistantId("asst_WQoe8Myj09wtB3vYWig0FXJb");
                assistant.setThreadId(threadId);
                assistant.setType(CODE_GENERATOR);
                assistantRepository.save(assistant);
                return threadId;
            } else if (initialMessageContent.contains("node") || initialMessageContent.contains("Node") || initialMessageContent.contains("Node.js") || initialMessageContent.contains("node.js")) {
                assistantMap.put(threadId, "asst_8VMJsRU9b57pgrTVxGMkYb5r");
                sendRun(threadId, "asst_8VMJsRU9b57pgrTVxGMkYb5r");
                Assistant assistant = new Assistant();
                assistant.setAssistantId("asst_8VMJsRU9b57pgrTVxGMkYb5r");
                assistant.setThreadId(threadId);
                assistant.setType(CODE_GENERATOR);
                assistantRepository.save(assistant);
                return threadId;
            }
        } else {
            // Trate o caso de erro conforme necessário
            return "Erro ao criar thread: " + response.getStatusCode();
        }

        return response.getBody();
    }

    // Método auxiliar para extrair o nome do projeto do initialMessageContent
    private String extractProjectName(String initialMessageContent) {
        // Divide a string pelo padrão ", " para lidar com múltiplos elementos
        String[] parts = initialMessageContent.split(",");
        for (String part : parts) {
            // Usa expressão regular para encontrar a parte que começa com "nome:" (ignora maiúsculas/minúsculas)
            if (part.trim().toLowerCase().matches("^nome:.*")) {
                String[] nameSplit = part.split(":", 2); // Divide apenas no primeiro ":", resultando em 2 partes
                if (nameSplit.length > 1) { // Verifica se existe algo após "nome:"
                    return nameSplit[1].trim(); // Retorna o nome do projeto, que é a parte após "nome:"
                }
            }
        }
        return "Nome do Projeto Desconhecido"; // Retorna isso se o nome do projeto não for encontrado
    }

    public String sendRun(String threadId, String assistantId) {
        HttpHeaders headers = createHeaders();
        JsonObject runBody = new JsonObject();
        runBody.addProperty("assistant_id", assistantId);

        HttpEntity<String> entity = new HttpEntity<>(runBody.toString(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(openAiBaseUrl + "/threads/" + threadId + "/runs", entity, String.class);

        return response.getBody();
    }

    HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + dotenv.get("OPENAI_API_KEY"));
        headers.set("OpenAI-Beta", "assistants=v1");
        return headers;
    }


    public ResponseEntity<String> getThreadMessages(String threadId) throws InterruptedException, IOException {

        ResponseEntity<String> response = null;
        try {

            HttpHeaders headers = createHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            response = restTemplate.exchange(
                    openAiBaseUrl + "/threads/" + threadId + "/messages",
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            Thread.sleep(5000);

            if (response.getBody() != null) {
                JsonObject responseBody = JsonParser.parseString(response.getBody()).getAsJsonObject();
                JsonArray messages = responseBody.getAsJsonArray("data");
                if (!messages.isEmpty()) {
                    JsonObject firstMessage = messages.get(0).getAsJsonObject();
                    JsonArray content = firstMessage.getAsJsonArray("content");
                    if (!content.isEmpty()) {
                        JsonObject firstContent = content.get(0).getAsJsonObject();
                        String value = firstContent.getAsJsonObject("text").get("value").getAsString();
                        if (value.isEmpty()) {
                            response = getThreadMessages(threadId);
                            System.out.println("Aguardando API");
                        }
                    } else {
                        System.out.println("Aguardando API");
                        response = getThreadMessages(threadId);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    public String addMessageToThread(String threadId, String message, boolean requestFeatureDependsBackend, String projectNameBackend) throws IOException, InterruptedException {
        try {
            String url = openAiBaseUrl + "/threads/" + threadId + "/messages";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + dotenv.get("OPENAI_API_KEY"));
            headers.set("OpenAI-Beta", "assistants=v1");

            ObjectMapper mapper = new ObjectMapper();

            if (requestFeatureDependsBackend) {
                String backendProjectName = projectNameBackend; // A lógica para determinar o correspondente backend.
                String backendTestThreadId = assistantRepository.findTestThreadIdByProjectName(backendProjectName);

                if (backendTestThreadId != null) {
                    ResponseEntity<String> response = getThreadMessages(backendTestThreadId);
                    JsonObject responseObject = JsonParser.parseString(response.getBody()).getAsJsonObject();
                    JsonArray dataArray = responseObject.getAsJsonArray("data");
                    for (JsonElement dataElement : dataArray) {
                        JsonArray contentArray = dataElement.getAsJsonObject().getAsJsonArray("content");
                        if (contentArray.size() > 0) {
                            JsonObject contentObject = contentArray.get(0).getAsJsonObject();
                            if ("text".equals(contentObject.get("type").getAsString())) {
                                JsonObject textObject = contentObject.getAsJsonObject("text");
                                String textValue = textObject.get("value").getAsString();

                                ObjectNode rootNode = mapper.createObjectNode();
                                rootNode.put("role", "user");
                                rootNode.put("content", textValue + message); // Combinação segura de strings
                                String requestBody = mapper.writeValueAsString(rootNode);

                                HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
                                ResponseEntity<String> response1 = restTemplate.postForEntity(url, request, String.class);

                                if (response1.getStatusCode().is2xxSuccessful()) {

                                    String assistantId = assistantMap.get(threadId);
                                    sendRun(threadId, assistantId);
                                    return threadId;
                                } else {
                                    // Tratar erros conforme necessário
                                    return "Erro ao adicionar mensagem ao Thread: " + response1.getStatusCode();
                                }
                            }
                        }
                    }
                }
            } else {
                ObjectNode rootNode = mapper.createObjectNode();
                rootNode.put("role", "user");
                rootNode.put("content", message); // Conteúdo direto

                String requestBody = mapper.writeValueAsString(rootNode);

                HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
                ResponseEntity<String> response1 = restTemplate.postForEntity(url, request, String.class);

                if (response1.getStatusCode().is2xxSuccessful()) {
                    String assistantId = assistantMap.get(threadId);
                    sendRun(threadId, assistantId);
                    return threadId;
                } else {
                    // Tratar erros conforme necessário
                    return "Erro ao adicionar mensagem ao Thread: " + response1.getStatusCode();
                }
            }

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (JsonSyntaxException e) {
            throw new RuntimeException(e);
        } catch (RestClientException e) {
            throw new RuntimeException(e);
        }
        return threadId;
    }


}
