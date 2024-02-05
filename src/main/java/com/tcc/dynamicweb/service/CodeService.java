package com.tcc.dynamicweb.service;


import com.google.gson.*;
import io.github.cdimascio.dotenv.Dotenv;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Base64;

import java.util.HashMap;
import java.util.Map;
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

    public ResponseEntity<String> getThreadMessages(String threadId) throws InterruptedException, IOException {
        HttpHeaders headers = createHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                openAiBaseUrl + "/threads/" + threadId + "/messages",
                HttpMethod.GET,
                entity,
                String.class
        );

         Thread.sleep(1200);

         regexJavaCode(response);

         return response;
    }

    String retorneClasses(Map<String, String> classesContent){
        StringBuilder result = new StringBuilder();

        for (String key : classesContent.keySet()) {
            result.append(key);
            result.append("\n");
        }
        return result.toString();
    }

    public String getFrontMessage(String threadId) throws InterruptedException, IOException {
        HttpHeaders headers = createHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                openAiBaseUrl + "/threads/" + threadId + "/messages",
                HttpMethod.GET,
                entity,
                String.class
        );

        Thread.sleep(1200);

        regexReactCode(response);

        return "Arquivos criados com sucesso.";
    }

    @NotNull
    public void regexReactCode(ResponseEntity<String> response) throws IOException {
        JsonObject responseObject = JsonParser.parseString(response.getBody()).getAsJsonObject();
        JsonArray dataArray = responseObject.getAsJsonArray("data");

        // Regex atualizado para capturar apenas comandos válidos
        String patternString = "(?s)```cmd\\n([\\s\\S]+?)\\n```";
        Pattern pattern = Pattern.compile(patternString);

        for (JsonElement dataElement : dataArray) {
            JsonArray contentArray = dataElement.getAsJsonObject().getAsJsonArray("content");
            for (JsonElement contentElement : contentArray) {
                JsonObject contentObject = contentElement.getAsJsonObject();
                if ("text".equals(contentObject.get("type").getAsString())) {
                    JsonObject textObject = contentObject.getAsJsonObject("text");
                    String textValue = textObject.get("value").getAsString();

                    Matcher matcher = pattern.matcher(textValue);
                    while (matcher.find()) {
                        String commandBlock = matcher.group(1).trim();
                        String[] commands = commandBlock.split("\\n"); // Divide em linhas separadas se houver mais de um comando
                        for (String command : commands) {
                            command = command.trim(); // Remova espaços em branco desnecessários
                                executeCommand(command);
                        }
                    }
                }
            }
        }
    }

    String currentDirectory = "C:\\Códigos\\Dynamic Projects";

    private void executeCommand(String command) {

        try {

           // String springCommand = "C:/Users/User/.sdkman/candidates/springboot/current/bin/spring --version";
            String gitBashPath = "C:\\Program Files\\Git\\bin\\bash.exe";

            // Verifica se o comando é um comando 'cd'
            if (command.startsWith("cd ")) {
                // Atualiza o diretório atual
                String newDirectory = command.substring(3).trim();
                currentDirectory = new File(currentDirectory, newDirectory).getCanonicalPath();
                System.out.println("Diretório mudado para: " + currentDirectory);
                return; // Não precisa executar o comando 'cd' como um processo externo
            }

            // Criar um Runtime e executar o comando usando o Bash do Git
            String fullCommand = gitBashPath + " -c \"" + command + "\"";
            Process process = Runtime.getRuntime().exec(fullCommand, null, new File(currentDirectory));

            // Redirecionar a saída de erro e a saída padrão para o console
            InputStream stdInput = process.getInputStream();
            InputStream stdError = process.getErrorStream();

            // Ler a saída do comando
            BufferedReader reader = new BufferedReader(new InputStreamReader(stdInput));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            // Ler erros do comando, se houver
            BufferedReader readerErr = new BufferedReader(new InputStreamReader(stdError));
            while ((line = readerErr.readLine()) != null) {
                System.err.println(line);
            }

            // Aguardar até que o processo seja finalizado e verificar o código de saída
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("Comando executado com sucesso: " + command);
            } else {
                System.err.println("O comando terminou com erros. Código de saída: " + exitCode);
            }
        } catch (IOException e) {
            System.err.println("Erro ao executar o comando. Erro de IO: " + e.getMessage());
        } catch (InterruptedException e) {
            System.err.println("Processo interrompido: " + e.getMessage());
            // Preserve o status de interrupção
            Thread.currentThread().interrupt();
        }
    }






    @NotNull
    public void regexJavaCode(ResponseEntity<String> response) throws IOException {
        JsonObject responseObject = JsonParser.parseString(response.getBody()).getAsJsonObject();
        JsonArray dataArray = responseObject.getAsJsonArray("data");
        String patternString = "(package\\s+.*?;[\\s\\S]*?public\\s+(class|interface|enum)\\s+.*?\\})";
        Pattern pattern = Pattern.compile(patternString, Pattern.DOTALL);

        for (JsonElement dataElement : dataArray) {
            JsonArray contentArray = dataElement.getAsJsonObject().getAsJsonArray("content");
            for (JsonElement contentElement : contentArray) {
                JsonObject contentObject = contentElement.getAsJsonObject();
                if ("text".equals(contentObject.get("type").getAsString())) {
                    JsonObject textObject = contentObject.getAsJsonObject("text");
                    String textValue = textObject.get("value").getAsString();

                    Matcher matcher = pattern.matcher(textValue);
                    while (matcher.find()) {
                        String classContent = matcher.group().trim();
                        Map<String, String> classesContent = extractJavaClasses(classContent);
                        createJavaFiles(classesContent, "C:\\Códigos\\Dynamic Projects");
                    }
                }
            }
        }
    }

    public Map<String, String> extractJavaClasses(String classContent) {
        Map<String, String> classesContent = new HashMap<>();
        String className = extractClassName(classContent);

        if (className != null && !className.isEmpty()) {
            classesContent.put(className, classContent);
        }

        return classesContent;
    }

    private String extractClassName(String classContent) {
        Pattern classNamePattern = Pattern.compile("(class|interface|enum)\\s+([\\w$]+)\\s");
        Matcher classNameMatcher = classNamePattern.matcher(classContent);
        if (classNameMatcher.find()) {
            return classNameMatcher.group(2);
        }
        return null;
    }

    public void createJavaFiles(Map<String, String> classesContent, String directoryPath) throws IOException {
        Path directory = Paths.get(directoryPath);
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
        }

        for (Map.Entry<String, String> classEntry : classesContent.entrySet()) {
            String className = classEntry.getKey();
            String classContent = classEntry.getValue();
            Path filePath = directory.resolve(className + ".java");
            Files.writeString(filePath, classContent, StandardOpenOption.CREATE);
        }
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
