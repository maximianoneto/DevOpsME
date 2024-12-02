package com.tcc.dynamicweb.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.*;
import com.tcc.dynamicweb.model.Assistant;
import com.tcc.dynamicweb.model.Project;
import com.tcc.dynamicweb.model.dto.CreateThreadDTO;
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

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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

    public String createThread(CreateThreadDTO createThreadDTO) {
        HttpHeaders headers = createHeaders();
        JsonObject messageObj = new JsonObject();
        messageObj.addProperty("role", "user");
        messageObj.addProperty("content", String.valueOf(createThreadDTO));

        JsonArray messages = new JsonArray();
        messages.add(messageObj);

        JsonObject threadBody = new JsonObject();
        threadBody.add("messages", messages);

        HttpEntity<String> entity = new HttpEntity<>(threadBody.toString(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(openAiBaseUrl + "/threads", entity, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            return "Erro ao criar thread: " + response.getStatusCode();
        }

        JsonObject responseBody = JsonParser.parseString(Objects.requireNonNull(response.getBody())).getAsJsonObject();
        String threadId = responseBody.get("id").getAsString();

        Map<String, String> assistantConfig = getAssistantConfig(createThreadDTO.getProgrammingLanguage());
        if (assistantConfig != null) {
            String assistantType = assistantConfig.keySet().iterator().next();
            String assistantId = assistantConfig.get(assistantType);
            createAndSaveAssistant(threadId, assistantId, assistantType, createThreadDTO.getProjectName());
            sendRun(threadId, assistantId);
            return threadId;
        }

        return response.getBody();
    }

    private Map<String, String> getAssistantConfig(String programmingLanguage) {
        String codeGenerator = "CODE_GENERATOR";
        String testGenerator = "TEST_GENERATOR";
        Dotenv dotenv = Dotenv.load();
        Map<String, String> assistantIdAndType = new HashMap<>(Map.of());

        // Garantir que a comparação ignore maiúsculas e minúsculas
        String lowerCaseMessage = programmingLanguage.toLowerCase();

        // Verificar se a mensagem contém as palavras-chave relevantes
        if (lowerCaseMessage.contains("testreact")) {
            String assistant = dotenv.get("TEST_REACT_ID");
            assistantIdAndType.put(testGenerator, assistant);
            return assistantIdAndType;
        } else if (lowerCaseMessage.contains("testnode")) {
            String assistant = dotenv.get("TEST_NODE_ID");
            assistantIdAndType.put(testGenerator, assistant);
            return assistantIdAndType;
        } else if (lowerCaseMessage.contains("python")) {
            String assistant = dotenv.get("PYTHON_ID");
            assistantIdAndType.put(codeGenerator, assistant);
            return assistantIdAndType;
        } else if (lowerCaseMessage.contains("react")) {
            String assistant = dotenv.get("REACT_ID");
            assistantIdAndType.put(codeGenerator, assistant);
            return assistantIdAndType;
        } else if (lowerCaseMessage.contains("java")) {
            String assistant = dotenv.get("JAVA_ID");
            assistantIdAndType.put(codeGenerator, assistant);
            return assistantIdAndType;
        } else if (lowerCaseMessage.contains("next")) {
            String assistant = dotenv.get("NEXT_ID");
            assistantIdAndType.put(codeGenerator, assistant);
            return assistantIdAndType;
        } else if (lowerCaseMessage.contains("node")) {
            String assistant = dotenv.get("NODE_ID");
            assistantIdAndType.put(codeGenerator, assistant);
            return assistantIdAndType;
        } else {
            // Log caso nenhuma palavra-chave seja encontrada
            System.out.println("No match found for keywords.");
        }

        return assistantIdAndType;
    }


    private void createAndSaveAssistant(String threadId, String assistantId, String type, String projectName) {
        Assistant assistant = new Assistant();
        assistant.setThreadId(threadId);
        assistant.setAssistantId(assistantId);
        assistant.setType(Assistant.AssistantType.valueOf(type));
        assistantRepository.save(assistant);
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
                String backendProjectName = projectNameBackend;
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
                                rootNode.put("content", textValue + message);
                                String requestBody = mapper.writeValueAsString(rootNode);

                                HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
                                ResponseEntity<String> response1 = restTemplate.postForEntity(url, request, String.class);

                                if (response1.getStatusCode().is2xxSuccessful()) {

                                    String assistantId = assistantMap.get(threadId);
                                    sendRun(threadId, assistantId);
                                    return threadId;
                                } else {
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
                    Optional<Assistant> assistant = assistantRepository.findAssistantByThreadId(threadId);
                    if (assistant.isPresent()){
                        sendRun(threadId, assistant.get().getAssistantId());
                        return threadId;
                    }
                } else {
                    return "Erro ao adicionar mensagem ao Thread: " + response1.getStatusCode();
                }
            }

        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        } catch (JsonSyntaxException e) {
            throw new RuntimeException(e);
        } catch (RestClientException e) {
            throw new RuntimeException(e);
        }
        return threadId;
    }


}
