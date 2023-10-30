package com.tcc.dynamicweb.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
public class CodeService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final Dotenv dotenv = Dotenv.configure().load();

    public String fetchFileContentAndPrint(Map<String, String> payload) throws IOException {
//        String dynamicServiceClass = restTemplate.getForObject(
//                "https://raw.githubusercontent.com/maximianoneto/dynamicweb/master/src/main/java/com/max/dynamicweb/service/DynamicService.java",
//                String.class);
//
//        String userControllerClass = restTemplate.getForObject(
//                "https://raw.githubusercontent.com/maximianoneto/dynamicweb/master/src/main/java/com/max/dynamicweb/controller/UserController.java",
//                String.class);
//
//        String mainClass = restTemplate.getForObject(
//                "https://raw.githubusercontent.com/maximianoneto/dynamicweb/master/src/main/java/com/max/dynamicweb/DynamicWebserviceApplication.java",
//                String.class);
//
//        String pomXmlClass = restTemplate.getForObject(
//                "https://raw.githubusercontent.com/maximianoneto/dynamicweb/master/pom.xml",
//                String.class);

        JsonObject body = new JsonObject();
        body.addProperty("model", "gpt-4-0613");
        body.addProperty("max_tokens",1000);
        body.addProperty("temperature",0);

        JsonArray messages = new JsonArray();
        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", "system");
        systemMessage.addProperty("content", "This is your class repository, you are Java 17 SpringBoot 3.1.0 and your roleplay as" +
                " WEBSERVICE: "
                +"the following class contains the code of your repository and the dependencies of your code(pom.xml), make new changes to the " +
                "code to meet the user's expectations with the webservice application, You must generate only a changed class or a new class and" +
                "add the dependencies you used, as pom.xml and DON'T forget to put @Table and @Data at entity class to create a table at Data storage. " +
                "The project structure will follow the MVC model, according to the example below you provide the class name in this format.\n" +
                "Ex: CustomerEntity, CustomerController, CustomerException, CustomerService, CustomerRepository." +
                "show the path of the new class or changed class. Your code :"
                +"MAIN CLASS:\n "
                + /*mainClass*/ " USER CONTROLLER:\n " + /* userControllerClass*/ "SERVICE CLASS:\n" +/* dynamicServiceClass*/ "DEPENDENCIES:\n" +/* pomXmlClass*/
                "// REQUIREMENTS: " + payload);

        messages.add(systemMessage);
        body.add("messages", messages);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer "+ dotenv.get("OPENAI_API_KEY"));
        HttpEntity<String> request = new HttpEntity<>(body.toString(), headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "https://api.openai.com/v1/chat/completions",
                request,
                String.class
        );

        JsonElement parser = JsonParser.parseString(response.getBody());
        JsonArray choicesArray = parser.getAsJsonObject().get("choices").getAsJsonArray();

        List<String> filePaths = new ArrayList<>();

        for (JsonElement choiceElement : choicesArray) {

            JsonElement messageElement = choiceElement.getAsJsonObject().get("message");

            String content = messageElement.getAsJsonObject().get("content").getAsString();

            String patternString = "(```java\\n)(.*?)(\\n```)";
            String pomPatternString = "(```xml\\n)(.*?)(\\n```)";

            Pattern pattern = Pattern.compile(patternString, Pattern.DOTALL);
            Pattern pomPattern = Pattern.compile(pomPatternString, Pattern.DOTALL);

            Matcher matcher = pattern.matcher(content);
            Matcher pomMatcher = pomPattern.matcher(content);

            while (matcher.find()) {

                String classContent = matcher.group(2).trim();

                System.out.println(classContent);

                //        createNewClass(classContent, classPath); // como resolver classPath, automatizado?
                // nomear classes com terminações referenciando seu package, dái só extrair com regex e fazer switch de possibilidades de classpath possíveis

                // Add the file path to the list
                //      filePaths.add(classPath);
            }

            while (pomMatcher.find()) {
                String pomContent = pomMatcher.group(2).trim();
                String fileName = "dghjkdhgk";

                //createNewClass(pomContent, fileName);

                filePaths.add(fileName);
            }
        }

        //gitCommitPush(filePaths);

        System.out.println("Unexpected response from API: " + response.getBody());

        return ResponseEntity.ok().toString();
    }

    public void processFile(String fileName, String content) throws IOException {
        // Verificar se o arquivo é um arquivo XML
        if (fileName.endsWith(".xml")) {
            // Se for um arquivo XML, copie o conteúdo do pom.xml
            String pomXmlContent = restTemplate.getForObject(
                    "https://raw.githubusercontent.com/maximianoneto/dynamicweb/master/pom.xml",
                    String.class
            );
            //  createNewClass(content + pomXmlContent, fileName);
        } else {
            // Se não for um arquivo XML, encontre o diretório com base na segunda palavra do nome da classe
            String[] parts = fileName.split("[/\\\\]");
            String className = parts[parts.length - 1].replace(".java", "");
            String[] classNameParts = className.split("(?=[A-Z])"); // Divide o nome da classe em partes com base em letras maiúsculas
            if (classNameParts.length >= 2) {
                String secondWord = classNameParts[1].toLowerCase();
                String destinationDirectory = secondWord; // Diretório de destino com base na segunda palavra do nome da classe
                String destinationPath = "path/to/destination/" + destinationDirectory + "/" + fileName;

                // Move o arquivo para o diretório de destino
               // moveFile(new File(fileName), new File(destinationPath));

                // createNewClass(pomXmlContent, "path/to/destination/" + destinationDirectory + "/pom.xml");
            }
        }

//        private void moveFile(String sourcePath, String destinationPath) throws IOException {
//            File sourceFile = new File(sourcePath);
//            File destinationFile = new File(destinationPath);
//
//            if (sourceFile.renameTo(destinationFile)) {
//                System.out.println("Arquivo movido com sucesso para: " + destinationPath);
//            } else {
//                System.err.println("Falha ao mover o arquivo para: " + destinationPath);
//            }
//        }

//    private static void gitCommitPush(@NotNull List<String> filePaths) throws IOException, GitAPIException {
//        String repoPath = "https://github.com/maximianoneto/dynamicweb";
//
//        // Cria uma instância do Git para a manipulação do repositório
//        Git git = Git.open(new File(repoPath));
//
//
//
//        for (String filePath : filePaths) { // ESTUDAR COMO A LIB DO GIT FUNCIONA
//            git.add().addFilepattern(filePath).call();
//        }
//
//        // Comita as alterações
//        git.commit().setMessage("Adicionadas novas classes e alterações no pom.xml").call(); // Geração de commit via GPT-4?
//
//        // Push the changes
//        git.push().call();
//
//        System.out.println("As novas classes e alterações no pom.xml foram adicionadas, comitadas e empurradas com sucesso.");
//    }


    }

//    private void createNewClass(String s, String fileName) {
//        FileWriter writer = new FileWriter(fileName);
//
//
//        writer.write(content);
//        writer.close();
//    }
}
