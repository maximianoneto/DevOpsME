package com.tcc.dynamicweb.service;


import com.google.gson.*;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CompletableFuture;
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

        return allClassesContent.toString();
    }







}
