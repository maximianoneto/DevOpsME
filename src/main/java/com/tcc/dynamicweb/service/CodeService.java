package com.tcc.dynamicweb.service;


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tcc.dynamicweb.model.Assistant;
import com.tcc.dynamicweb.model.Project;
import com.tcc.dynamicweb.repository.AssistantRepository;
import com.tcc.dynamicweb.repository.ProjectRepository;
import io.github.cdimascio.dotenv.Dotenv;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CodeService {
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

    @Autowired
    private ThreadService threadService;

    @Autowired
    private ProjectService projectService;

    boolean containsPattern(String text, String pattern) {
        Pattern p = Pattern.compile(pattern);
        return p.matcher(text).find();
    }

    @NotNull
    public void regexCmdCode(ResponseEntity<String> response) throws IOException {

        JsonObject responseObject = JsonParser.parseString(response.getBody()).getAsJsonObject();
        JsonArray dataArray = responseObject.getAsJsonArray("data");
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
                            command = command.trim();
                            executeCommand(command);
                        }
                    }
                }
            }
        }
    }

    //String currentDirectory = "/projects"; Prod
    String currentDirectory = "C:\\Projects"; // Local

    private void executeCommand(String command) {

        try {

            Map<String, String> env = new HashMap<>(System.getenv());
            String nodePath = "C:\\Program Files\\nodejs";
            env.put("PATH", nodePath + ";" + env.get("PATH"));

            String springPath = "D:\\spring-3.2.2\\bin";
            env.put("PATH", springPath + ";" + env.get("PATH"));

            if (command.startsWith("cd ")) {
                String commandPart = command.substring(3).trim();
                String[] parts = commandPart.split("&&");
                String newDirectory = parts[0].trim();

                currentDirectory = new File(currentDirectory, newDirectory).getCanonicalPath();
                System.out.println("Diretório mudado para: " + currentDirectory);
                return;
            }

            // Verifica se o comando contém --name=nome-arquivo nome-arquivo se não contém, adiciona o nome-arquivo um espaço após o fim do comando
            // regex para retirar o nome do projeto da string e vincular ao comando de execução
            if (command.contains("--name=")) {
                String projectName = command.substring(command.lastIndexOf("--name=") + 7);
                //verifica se o nome do projeto esta contido duas vezes na string comand
                if (!projectName.contains(" ")) {
                    command = command + " " + projectName;
                }

            }

            if (command.contains("npm start") || command.contentEquals("npm start") || command.contentEquals("npm test") || command.contentEquals("npm test")) {
                return;
            }


            String cmdPrefix = "cmd /c "; // Deixar essa linha descomentada para rodar localmente

            String windowsCommand = command.replace("/", "\\"); // Garantir o uso de separadores de caminho do Windows

            ProcessBuilder processBuilder = new ProcessBuilder("cmd", "/c", windowsCommand);
            processBuilder.directory(new File(currentDirectory));
            processBuilder.environment().putAll(env); // Adiciona o PATH atualizado
            Process process = processBuilder.start();

            InputStream stdInput = process.getInputStream();
            InputStream stdError = process.getErrorStream();


            BufferedReader reader = new BufferedReader(new InputStreamReader(stdInput));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }


            BufferedReader readerErr = new BufferedReader(new InputStreamReader(stdError));
            while ((line = readerErr.readLine()) != null) {
                System.err.println(line);
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("Comando executado com sucesso: " + command);

            } else {
                System.err.println("O comando terminou com erros. Código de saída: " + exitCode);
            }
            //currentDirectory = "/projects"; // Prod
            //currentDirectory = "C:\\Projects"; // Local
        } catch (IOException e) {
            System.err.println("Erro ao executar o comando. Erro de IO: " + e.getMessage());
        } catch (InterruptedException e) {
            System.err.println("Processo interrompido: " + e.getMessage());
            // Preserve o status de interrupção
            Thread.currentThread().interrupt();
        }
    }


    public void updateProjectPath(String projectName, String path) {
        projectPaths.put(projectName, path);
    }

    @NotNull
    public void regexJavaCode(ResponseEntity<String> response, String projectName) throws IOException {
        JsonObject responseObject = JsonParser.parseString(response.getBody()).getAsJsonObject();
        JsonArray dataArray = responseObject.getAsJsonArray("data");
        String patternString = "```java\\n(.*?)```";
        String patternGradle = "```gradle\\n(.*?)```";
        String patternAppProp = "```properties\\n(.*?)```";

        Pattern pattern = Pattern.compile(patternString, Pattern.DOTALL);
        Pattern patternGr = Pattern.compile(patternGradle, Pattern.DOTALL);
        Pattern patternProp = Pattern.compile(patternAppProp, Pattern.DOTALL);

        for (JsonElement dataElement : dataArray) {
            JsonArray contentArray = dataElement.getAsJsonObject().getAsJsonArray("content");
            if (contentArray.size() > 0) { // Verifica se há pelo menos um elemento
                JsonObject contentObject = contentArray.get(0).getAsJsonObject(); // Acessa o primeiro elemento diretamente
                if ("text".equals(contentObject.get("type").getAsString())) {
                    JsonObject textObject = contentObject.getAsJsonObject("text");
                    String textValue = textObject.get("value").getAsString();

                    Matcher matcher = pattern.matcher(textValue);
                    while (matcher.find()) {
                        String classContent = matcher.group().trim();
                        // Delete a primeira e ultima linha da string classContent
                        classContent = classContent.substring(classContent.indexOf("\n") + 1, classContent.lastIndexOf("\n"));
                        Map<String, String> classesContent = projectService.extractJavaClasses(classContent);
                        projectService.createJavaFilesInProject(classesContent, projectName);
                    }

                    Matcher matcher2 = patternGr.matcher(textValue);
                    if (matcher2.find()) { // Processa apenas a primeira ocorrência para dependências Gradle
                        String dependenciesBlock = matcher2.group(1).trim();
                        projectService.addDependencyToProject(dependenciesBlock, projectName);
                    }

                    Matcher matcher3 = patternProp.matcher(textValue);
                    if (matcher3.find()) { // Processa apenas a primeira ocorrência para dependências application.properties
                        String applicationBlock = matcher3.group(1).trim();
                        projectService.addApplicationPropertiesToProject(applicationBlock, projectName);
                    }
                }
            }
        }
    }

    @NotNull
    public void regexNodeCode(ResponseEntity<String> response, String projectName) throws IOException, InterruptedException {
        JsonObject responseObject = JsonParser.parseString(Objects.requireNonNull(response.getBody())).getAsJsonObject();
        JsonArray dataArray = responseObject.getAsJsonArray("data");

        // Expressões regulares para capturar o caminho e o conteúdo
        String patternNodeString = "```javascript\\n(.*?)```";
        Pattern patternNode = Pattern.compile(patternNodeString, Pattern.DOTALL);
        String patternPathString = "```path\\n(.*?)```";
        Pattern patternPath = Pattern.compile(patternPathString, Pattern.DOTALL);

        for (JsonElement dataElement : dataArray) {
            JsonArray contentArray = dataElement.getAsJsonObject().getAsJsonArray("content");
            if (contentArray.size() > 0) {
                JsonObject contentObject = contentArray.get(0).getAsJsonObject();
                if ("text".equals(contentObject.get("type").getAsString())) {
                    JsonObject textObject = contentObject.getAsJsonObject("text");

                    String textValue = textObject.get("value").getAsString();

                    Matcher matcherNode = patternNode.matcher(textValue);
                    Matcher matcherPath = patternPath.matcher(textValue);

                    // Encontra primeiro o path e depois o node, ambos devem existir para chamar o método
                    while (matcherPath.find() && matcherNode.find()) {
                        String path = matcherPath.group(1).trim();
                        String content = matcherNode.group(1).trim();
                        projectService.createNodeFilesInProject(path, content, projectName);
                        System.out.println("Criando Arquivos Javascript");
                    }
                }
            }
        }
        sendCodeToAssistantTest(response, projectName);
    }

    private void sendCodeToAssistantTest(ResponseEntity<String> response, String projectName) throws IOException, InterruptedException {

        if (response.getBody() != null) {
            JsonObject responseBody = JsonParser.parseString(response.getBody()).getAsJsonObject();
            JsonArray messages = responseBody.getAsJsonArray("data");
            if (!messages.isEmpty()) {
                JsonObject firstMessage = messages.get(0).getAsJsonObject();
                JsonArray content = firstMessage.getAsJsonArray("content");
                if (!content.isEmpty()) {
                    JsonObject firstContent = content.get(0).getAsJsonObject();
                    String value = firstContent.getAsJsonObject("text").get("value").getAsString();

                    String threadIdTest = assistantRepository.findTestThreadIdByProjectName(projectName);

                    if(threadIdTest != null){
                        String threadId = threadService.addMessageToThread(threadIdTest,value, false, "");

                        regexTestNode(threadService.getThreadMessages(threadId), projectName);
                    }else {
                      //  String threadId = threadService.createThread("nome:" + projectName + "," + "\n" + "testNode" + value);

                     //   regexTestNode(threadService.getThreadMessages(threadId), projectName);

                        // Supondo que seu projectRepository tenha um método chamado findByName que retorna um Optional<Project>
//                        Optional<Project> existingProject = projectRepository.findByName(projectName);
//
//                        Project project = existingProject.orElseGet(() -> {
//                            Project newProject = new Project();
//                            newProject.setName(projectName);
//                            newProject.setPathToProject("C:\\Projects\\" + projectName);
//                            return newProject;
//                        });

                        // Supondo que o assistantRepository tenha um método findAssistantByThreadId que retorne Optional<Assistant>
                   //     Optional<Assistant> existingAssistant = assistantRepository.findAssistantByThreadId(threadId);

                  //      Assistant assistant = existingAssistant.orElseGet(() -> new Assistant());
                  //      assistant.setThreadId(threadId);
                //        assistant.setProject(project);
                        // Ajuste o tipo conforme sua implementação
                   //     assistant.setType(Assistant.AssistantType.TEST_GENERATOR);
                  //     project.getAssistants().add(assistant);

                     //   projectRepository.save(project);
                    }
                }
            }
        }
    }

    private void regexTestNode(ResponseEntity<String> response, String projectName) throws IOException {
        JsonObject responseObject = JsonParser.parseString(Objects.requireNonNull(response.getBody())).getAsJsonObject();
        JsonArray dataArray = responseObject.getAsJsonArray("data");

        // Expressões regulares para capturar o caminho e o conteúdo
        String patternNodeString = "```javascript\\n(.*?)```";
        Pattern patternNode = Pattern.compile(patternNodeString, Pattern.DOTALL);
        String patternPathString = "```path\\n(.*?)```";
        Pattern patternPath = Pattern.compile(patternPathString, Pattern.DOTALL);

        String patternCmdString = "(?s)```cmd\\n([\\s\\S]+?)\\n```";
        Pattern patternCmd = Pattern.compile(patternCmdString, Pattern.DOTALL);

        for (JsonElement dataElement : dataArray) {
            JsonArray contentArray = dataElement.getAsJsonObject().getAsJsonArray("content");
            if (contentArray.size() > 0) {
                JsonObject contentObject = contentArray.get(0).getAsJsonObject();
                if ("text".equals(contentObject.get("type").getAsString())) {
                    JsonObject textObject = contentObject.getAsJsonObject("text");
                    String textValue = textObject.get("value").getAsString();

                    Matcher matcherNode = patternNode.matcher(textValue);
                    Matcher matcherPath = patternPath.matcher(textValue);
                    Matcher matcherCmd = patternCmd.matcher(textValue);

                    while (matcherCmd.find()) {
                        String commandBlock = matcherCmd.group(1).trim();
                        String[] commands = commandBlock.split("\\n"); // Divide em linhas separadas se houver mais de um comando
                        for (String command : commands) {
                            command = command.trim(); // Remova espaços em branco desnecessários
                            executeCommand(command);
                        }
                    }

                    // Encontra primeiro o path e depois o node, ambos devem existir para chamar o método
                    while (matcherPath.find() && matcherNode.find()) {
                        String path = matcherPath.group(1).trim();
                        String content = matcherNode.group(1).trim();
                        projectService.createNodeFilesInProject(path, content, projectName);
                        System.out.println("Criando Arquivos de Teste");
                    }
                }
            }
        }
    }


    public String encodeImageToBase64(byte[] imageBytes) {
        return Base64.getEncoder().encodeToString(imageBytes);
    }

    public String createPayload(String base64Image, String message) {
        JsonObject payloadObj = new JsonObject();
        payloadObj.addProperty("model", "gpt-4o");

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


    public ResponseEntity<String> addCode(String threadId, String projectName) {
        ResponseEntity<String> response = null;
        try {

            HttpHeaders headers = threadService.createHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            response = restTemplate.exchange(
                    openAiBaseUrl + "/threads/" + threadId + "/messages",
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            String patternString = "(?s)```cmd\\n([\\s\\S]+?)\\n```";
            Pattern pattern = Pattern.compile(patternString);
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
                            response = threadService.getThreadMessages(threadId);
                            System.out.println("Aguardando API");
                        } else {
                            // Verificação de padrões no primeiro item de content

                            Matcher matcher = pattern.matcher(value);
                            while (matcher.find()) {
                                String commandBlock = matcher.group(1).trim();
                                String[] commands = commandBlock.split("\\n"); // Divide em linhas separadas se houver mais de um comando
                                for (String command : commands) {
                                    command = command.trim(); // Remova espaços em branco desnecessários
                                    executeCommand(command);
                                }
                            }
                            Optional<Project> project = projectRepository.findByNameAndThreadId(projectName, threadId);
                            String language = project.get().getProgrammingLanguague();
                            switch (language) {
                                case "java":
                                    regexJavaCode(response, projectName);
                                    break;
                                case "python":
                                    regexPythonCode(response, projectName);
                                    break;
                                case "node":
                                    regexNodeCode(response, projectName);
                                    break;
                                case "next":
                                    regexNextCode(response, projectName);
                                    break;
                                case "react":
                                    regexReactCode(response, projectName);
                                    break;
                                default:
                                    System.out.println("Tipo de projeto desconhecido: " + projectName);
                                    break;
                            }
                        }
                    } else {
                        System.out.println("Aguardando API");
                        response = threadService.getThreadMessages(threadId);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    private void regexPythonCode(ResponseEntity<String> response, String project) throws IOException {
        JsonObject responseObject = JsonParser.parseString(response.getBody()).getAsJsonObject();
        JsonArray dataArray = responseObject.getAsJsonArray("data");

        // Expressões regulares para capturar o caminho e o conteúdo
        String patternNodeString = "```python\\n(.*?)```";
        Pattern patternNode = Pattern.compile(patternNodeString, Pattern.DOTALL);

        for (JsonElement dataElement : dataArray) {
            JsonArray contentArray = dataElement.getAsJsonObject().getAsJsonArray("content");
            if (contentArray.size() > 0) {
                JsonObject contentObject = contentArray.get(0).getAsJsonObject();
                if ("text".equals(contentObject.get("type").getAsString())) {
                    JsonObject textObject = contentObject.getAsJsonObject("text");
                    String textValue = textObject.get("value").getAsString();

                    Matcher matcherNode = patternNode.matcher(textValue);

                    // Encontra primeiro o path e depois o node, ambos devem existir para chamar o método
                    while (matcherNode.find()) {
                        String content = matcherNode.group(1).trim();
                        projectService.createPythonFilesInProject(content, project);
                    }
                }
            }
        }
    }



    private void regexNextCode(ResponseEntity<String> response, String project) throws IOException {
        JsonObject responseObject = JsonParser.parseString(response.getBody()).getAsJsonObject();
        JsonArray dataArray = responseObject.getAsJsonArray("data");

        // Expressões regulares para capturar o caminho e o conteúdo
        String patternNodeString = "```js\\n(.*?)```";
        Pattern patternNode = Pattern.compile(patternNodeString, Pattern.DOTALL);
        String patternPathString = "```path\\n(.*?)```";
        Pattern patternPath = Pattern.compile(patternPathString, Pattern.DOTALL);

        for (JsonElement dataElement : dataArray) {
            JsonArray contentArray = dataElement.getAsJsonObject().getAsJsonArray("content");
            if (contentArray.size() > 0) {
                JsonObject contentObject = contentArray.get(0).getAsJsonObject();
                if ("text".equals(contentObject.get("type").getAsString())) {
                    JsonObject textObject = contentObject.getAsJsonObject("text");
                    String textValue = textObject.get("value").getAsString();

                    Matcher matcherNode = patternNode.matcher(textValue);
                    Matcher matcherPath = patternPath.matcher(textValue);

                    // Encontra primeiro o path e depois o node, ambos devem existir para chamar o método
                    while (matcherPath.find() && matcherNode.find()) {
                        String path = matcherPath.group(1).trim();
                        String content = matcherNode.group(1).trim();
                        projectService.createNextFilesInProject(path, content, project);
                    }
                }
            }
        }
    }



    private void regexReactCode(ResponseEntity<String> response, String project) throws IOException, InterruptedException {
        JsonObject responseObject = JsonParser.parseString(response.getBody()).getAsJsonObject();
        JsonArray dataArray = responseObject.getAsJsonArray("data");


        String patternPathString = "```path\\n(.*?)```";
        Pattern patternPath = Pattern.compile(patternPathString, Pattern.DOTALL);

        String patternCssString = "```css\\n(.*?)```";
        Pattern patternCss = Pattern.compile(patternCssString, Pattern.DOTALL);

        String patternJsString = "```javascript\\n(.*?)```";
        Pattern patternJs = Pattern.compile(patternJsString, Pattern.DOTALL);

        // Expressões regulares para capturar o caminho e o conteúdo
        String patternHtmlString = "```html\\n(.*?)```";
        Pattern patternHtml = Pattern.compile(patternHtmlString, Pattern.DOTALL);

        for (JsonElement dataElement : dataArray) {
            JsonArray contentArray = dataElement.getAsJsonObject().getAsJsonArray("content");
            if (contentArray.size() > 0) {
                JsonObject contentObject = contentArray.get(0).getAsJsonObject();
                if ("text".equals(contentObject.get("type").getAsString())) {
                    JsonObject textObject = contentObject.getAsJsonObject("text");
                    String textValue = textObject.get("value").getAsString();

                    Matcher matcherHtml = patternHtml.matcher(textValue);
                    Matcher matcherPath = patternPath.matcher(textValue);

                    Matcher matcherCss = patternCss.matcher(textValue);
                    Matcher matcherJs = patternJs.matcher(textValue);

                    // Encontra primeiro o path e depois o node, ambos devem existir para chamar o método
                    while (matcherPath.find() && matcherHtml.find()) {
                        String path = matcherPath.group(1).trim();
                        String content = matcherHtml.group(1).trim();
                        projectService.createReactFilesInProject(path, content, project);
                    }

                    while (matcherPath.find() && matcherJs.find()) {
                        String path = matcherPath.group(1).trim();
                        String content = matcherJs.group(1).trim();
                        projectService.createReactFilesInProject(path, content, project);
                    }

                    while (matcherPath.find() && matcherCss.find()) {
                        String path = matcherPath.group(1).trim();
                        String content = matcherCss.group(1).trim();
                        projectService.createReactFilesInProject(path, content, project);
                    }
                }
            }
        }
        sendCodeToAssistantTestReact(response, project);
    }



    private void sendCodeToAssistantTestReact(ResponseEntity<String> response, String projectName) throws IOException, InterruptedException {

        if (response.getBody() != null) {
            JsonObject responseBody = JsonParser.parseString(response.getBody()).getAsJsonObject();
            JsonArray messages = responseBody.getAsJsonArray("data");
            if (!messages.isEmpty()) {
                JsonObject firstMessage = messages.get(0).getAsJsonObject();
                JsonArray content = firstMessage.getAsJsonArray("content");
                if (!content.isEmpty()) {
                    JsonObject firstContent = content.get(0).getAsJsonObject();
                    String value = firstContent.getAsJsonObject("text").get("value").getAsString();

                    String threadIdTest = assistantRepository.findTestThreadIdByProjectName(projectName);

                    if(threadIdTest != null){
                        String threadId = threadService.addMessageToThread(threadIdTest,value, false, "");

                        regexTestNode(threadService.getThreadMessages(threadId), projectName);
                    }else {

                    //    String threadId = threadService.createThread("nome:" + projectName + "," + "testReact" + "\n" + value);

                   //     regexTestNode(threadService.getThreadMessages(threadId), projectName);

                   //     Optional<Project> existingProject = projectRepository.findByName(projectName);

//                        Project project = existingProject.orElseGet(() -> {
//                            Project newProject = new Project();
//                            newProject.setName(projectName);
//                            newProject.setPathToProject("C:\\Projects\\" + projectName);
//                            return newProject;
//                        });

                    //   Optional<Assistant> existingAssistant = assistantRepository.findAssistantByThreadId(threadId);

                    //    Assistant assistant = existingAssistant.orElseGet(() -> new Assistant());
                    //    assistant.setThreadId(threadId);
                    //    assistant.setProject(project);

                    //    assistant.setType(Assistant.AssistantType.TEST_GENERATOR);
                   //     project.getAssistants().add(assistant);

                 //       projectRepository.save(project);
                    }
                }
            }
        }
    }
}


