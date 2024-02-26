package com.tcc.dynamicweb.service;


import com.google.gson.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class CodeService {
    private final RestTemplate restTemplate = new RestTemplate();
    private final String openAiBaseUrl = "https://api.openai.com/v1";
    //private final Dotenv dotenv = Dotenv.configure().load();

    private final ConcurrentHashMap<String, String> projectPaths = new ConcurrentHashMap<>();

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

            if(initialMessageContent.contains("react") || initialMessageContent.contains("React")){
                sendRun(threadId, "asst_OxHBt8GMEc3x4N8QPqi0wrma");
            } else if(initialMessageContent.contains("java") || initialMessageContent.contains("Java")){
                sendRun(threadId, "asst_P1Mlu6C8nZBevGH0yvX5aK35");
            }
        } else {
            // Trate o caso de erro conforme necessário
            return "Erro ao criar thread: " + response.getStatusCode();
        }

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
        headers.set("Authorization", "Bearer " + "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"); /*dotenv.get("OPENAI_API_KEY")*/
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

            Thread.sleep(1200);

            regexCmdCode(response);

           //regexJavaCode(response, "hostel");


        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
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
                            command = command.trim(); // Remova espaços em branco desnecessários
                                executeCommand(command);
                        }
                    }
                }
            }
        }
    }

    String currentDirectory = "/";

    private void executeCommand(String command) {

        try {

            // Verifica se o comando é um comando 'cd'
            if (command.startsWith("cd ")) {
                // Atualiza o diretório atual
                String newDirectory = command.substring(3).trim();
                currentDirectory = new File(currentDirectory, newDirectory).getCanonicalPath();
                System.out.println("Diretório mudado para: " + currentDirectory);
                return; // Não precisa executar o comando 'cd' como um processo externo
            }



            // Verifica se o comando contém --name=nome-arquivo nome-arquivo se não contém, adiciona o nome-arquivo um espaço após o fim do comando
            // faça um regex para retirar o nome do projeto da string e vincular ao comando de execução
            if (command.contains("--name=")){
                String projectName = command.substring(command.lastIndexOf("--name=") + 7);
                //verifica se o nome do projeto esta contido duas vezes na string comand
                if(!projectName.contains(" ")){
                    command = command + " " + projectName;
                }

            }
            String command2 = command;

            // Criar um Runtime e executar o comando usando o Bash do Git
            String cmdPrefix = "cmd /c ";
            String windowsCommand = cmdPrefix + command.replace("/", "\\"); // Garantir o uso de separadores de caminho do Windows

            // Executar o comando
            Process process = Runtime.getRuntime().exec(windowsCommand, null, new File(currentDirectory));

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
                    while (matcher.find()) { // Usa if em vez de while para processar apenas a primeira ocorrência
                        String classContent = matcher.group().trim();
                        // Delete a primeira e ultima linha da string classContent
                        classContent = classContent.substring(classContent.indexOf("\n") + 1, classContent.lastIndexOf("\n"));
                        Map<String, String> classesContent = extractJavaClasses(classContent);
                        createJavaFilesInProject(classesContent, projectName);
                    }

                    Matcher matcher2 = patternGr.matcher(textValue);
                    if (matcher2.find()) { // Processa apenas a primeira ocorrência para dependências Gradle
                        String dependenciesBlock = matcher2.group(1).trim();
                        replaceDependenciesInProject(dependenciesBlock, projectName);
                    }

                    Matcher matcher3 = patternProp.matcher(textValue);
                    if (matcher3.find()) { // Processa apenas a primeira ocorrência para dependências application.properties
                        String applicationBlock = matcher3.group(1).trim();
                        addApplicationPropertiesToProject(applicationBlock, projectName);
                    }
                }
            }
        }
    }

    private void replaceDependenciesInProject(String dependenciesBlock, String projectName) throws IOException {
        Path projectFilePath;

        projectFilePath = Paths.get(getProjectPath(projectName), "build.gradle");

        String fileContent = new String(Files.readAllBytes(projectFilePath));
        String updatedContent = fileContent.replaceAll("(?s)dependencies\\s*\\{.*?\\}", dependenciesBlock);

        Files.writeString(projectFilePath, updatedContent, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private void addApplicationPropertiesToProject(String applicationBlock, String projectName) throws IOException {
        Path projectFilePath = Paths.get(getProjectPath(projectName), "src", "main", "resources", "application.properties");

        String fileContent = new String(Files.readAllBytes(projectFilePath));

        // Apenas adicione o applicationBlock no arquivo application.properties
        String updatedContent = fileContent + "\n" + applicationBlock;

        Files.writeString(projectFilePath, updatedContent, StandardOpenOption.TRUNCATE_EXISTING);

    }

    public void createJavaFilesInProject(Map<String, String> classesContent, String projectName) throws IOException {
        // Caminho base onde os arquivos Java devem ser salvos dentro do projeto
        String mainBasePath = getProjectPath(projectName) + File.separator + "src" + File.separator + "main" + File.separator + "java";
        String testBasePath = getProjectPath(projectName) + File.separator + "src" + File.separator + "test" + File.separator + "java";

        for (Map.Entry<String, String> classEntry : classesContent.entrySet()) {
            String className = classEntry.getKey();
            String classContent = classEntry.getValue();
            String packageName = extractPackageName(classContent); // Extrai o nome do pacote

            // Define o caminho base com base no nome da classe
            String basePath = className.contains("Test") ? testBasePath : mainBasePath;

            // Constrói o caminho do diretório baseado no nome do pacote
            String packagePath = packageName.replace('.', File.separatorChar);
            Path fullDirPath = Paths.get(basePath, packagePath);

            // Cria o diretório do pacote, se necessário
            if (!Files.exists(fullDirPath)) {
                Files.createDirectories(fullDirPath);
            }

            // Cria o arquivo Java no diretório do pacote
            Path filePath = fullDirPath.resolve(className + ".java");
            Files.writeString(filePath, classContent, StandardOpenOption.CREATE);
        }
    }

    private String extractPackageName(String classContent) {
        Pattern packagePattern = Pattern.compile("package\\s+([\\w\\.]+);");
        Matcher matcher = packagePattern.matcher(classContent);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null; // Ou retorne um valor padrão, conforme necessário
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

    public String encodeImageToBase64(byte[] imageBytes) {
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


    public String addMessageToThread(String threadId, String message) {
        String url = openAiBaseUrl + "/threads/" + threadId + "/messages";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + "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");
        headers.set("OpenAI-Beta", "assistants=v1");

        // Construir o corpo da requisição
        String requestBody = String.format("{ \"role\": \"user\", \"content\": \"%s\" }", message);

        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                if(message.contains("react") || message.contains("React")){
                    sendRun(threadId, "asst_OxHBt8GMEc3x4N8QPqi0wrma");
                } else if(message.contains("java") || message.contains("Java")){
                    sendRun(threadId, "asst_P1Mlu6C8nZBevGH0yvX5aK35");
                }
                return "Mensagem adicionada com sucesso ao Thread: " + threadId;
            } else {
                // Tratar erros conforme necessário
                return "Erro ao adicionar mensagem ao Thread: " + response.getStatusCode();
            }
        } catch (Exception e) {
            // Tratar exceções conforme necessário
            return "Exceção ao adicionar mensagem ao Thread: " + e.getMessage();
        }
    }

    public void zipFolder(Path sourceFolderPath, Path zipPath) throws IOException {
        // Verifica se a pasta node_modules existe e a deleta
        Path nodeModulesPath = sourceFolderPath.resolve("node_modules");
        if (Files.exists(nodeModulesPath)) {
            deleteFolder(nodeModulesPath);
        }

        // Inicia o processo de zip
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipPath.toFile()))) {
            Files.walkFileTree(sourceFolderPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    zos.putNextEntry(new ZipEntry(sourceFolderPath.relativize(file).toString()));
                    Files.copy(file, zos);
                    zos.closeEntry();
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    // Ignora a adição do diretório node_modules ao zip
                    if (!dir.equals(nodeModulesPath)) {
                        zos.putNextEntry(new ZipEntry(sourceFolderPath.relativize(dir).toString() + "/"));
                        zos.closeEntry();
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    private void deleteFolder(Path folderPath) throws IOException {
        Files.walk(folderPath)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    public String getProjectPath(String projectName) {
        // Utiliza File.separator para garantir compatibilidade entre diferentes sistemas operacionais
        return currentDirectory + File.separator + projectName;
    }
}
