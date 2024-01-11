package com.tcc.dynamicweb.service;


import com.google.gson.*;
import io.github.cdimascio.dotenv.Dotenv;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CodeService {
    private final RestTemplate restTemplate = new RestTemplate();
    private final String openAiBaseUrl = "https://api.openai.com/v1";
    private final Dotenv dotenv = Dotenv.configure().load();

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

        return response.getBody();
    }

    public String sendRun(String threadId, String assistantId) {
        HttpHeaders headers = createHeaders();
        JsonObject runBody = new JsonObject();
        runBody.addProperty("assistant_id", assistantId);

        HttpEntity<String> entity = new HttpEntity<>(runBody.toString(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(openAiBaseUrl + "/threads/" + threadId + "/runs", entity, String.class);

        return response.getBody();
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + dotenv.get("OPENAI_API_KEY"));
        headers.set("OpenAI-Beta", "assistants=v1");
        return headers;
    }

    public String getThreadMessages(String threadId) throws InterruptedException {
        HttpHeaders headers = createHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                openAiBaseUrl + "/threads/" + threadId + "/messages",
                HttpMethod.GET,
                entity,
                String.class
        );

        Thread.sleep(1200);

        StringBuilder allClassesContent = regexJavaCode(response);

        return allClassesContent.toString();
    }

    @NotNull
    public static StringBuilder regexJavaCode(ResponseEntity<String> response) {
        JsonObject responseObject = JsonParser.parseString(response.getBody()).getAsJsonObject();
        JsonArray dataArray = responseObject.getAsJsonArray("data");
        StringBuilder allClassesContent = new StringBuilder();
        String patternString = "(```java\\n)(.*?)(\\n```)";

        for (JsonElement dataElement : dataArray) {
            JsonArray contentArray = dataElement.getAsJsonObject().getAsJsonArray("content");
            for (JsonElement contentElement : contentArray) {
                JsonObject contentObject = contentElement.getAsJsonObject();
                if ("text".equals(contentObject.get("type").getAsString())) {
                    JsonObject textObject = contentObject.getAsJsonObject("text");
                    String textValue = textObject.get("value").getAsString();

                    Pattern pattern = Pattern.compile(patternString, Pattern.DOTALL);
                    Matcher matcher = pattern.matcher(textValue);

                    while (matcher.find()) {
                        String classContent = matcher.group(2).trim();
                        allClassesContent.append(classContent).append("\n\n");
                    }
                }
            }
        }
        return allClassesContent;
    }

    public String encodeImageToBase64(String imagePath) throws IOException {
        byte[] imageBytes = Files.readAllBytes(Path.of(imagePath));
        return Base64.getEncoder().encodeToString(imageBytes);
    }

    public String createPayload(String base64Image, String message) {
        JsonObject payloadObj = new JsonObject();
        payloadObj.addProperty("model", "gpt-4-vision-preview");

        JsonArray messagesArray = new JsonArray();
        JsonObject messageObj = new JsonObject();
        messageObj.addProperty("role", "user");

        JsonArray contentArray = new JsonArray();

        // Adicionando o texto à mensagem
        JsonObject textObj = new JsonObject();
        textObj.addProperty("type", "text");
        textObj.addProperty("text", message);
        contentArray.add(textObj);

        // Adicionando a imagem à mensagem
        JsonObject imageObj = new JsonObject();
        imageObj.addProperty("type", "image_url");

        JsonObject imageUrlObj = new JsonObject();
        imageUrlObj.addProperty("url", "data:image/jpeg;base64," + base64Image);
        imageUrlObj.addProperty("detail", "high");

        imageObj.add("image_url", imageUrlObj);
        contentArray.add(imageObj);

        messageObj.add("content", contentArray);
        messagesArray.add(messageObj);

        payloadObj.add("messages", messagesArray);
        payloadObj.addProperty("max_tokens", 4096);

        return payloadObj.toString();
    }

    private final String apiUrl = "https://api.openai.com/v1/chat/completions";

    public String callOpenAiApi(String payload, String apiKey) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<String> entity = new HttpEntity<>(payload, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);

        return response.getBody();
    }



}
