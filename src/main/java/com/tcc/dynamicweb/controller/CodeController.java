package com.tcc.dynamicweb.controller;

import com.tcc.dynamicweb.model.dto.RequestThreadDTO;
import com.tcc.dynamicweb.service.CodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "Code Controller", description = "Controller for External API Requests and Responses")
public class CodeController {

    @Autowired
    CodeService codeService;

    private static final Logger logger = LoggerFactory.getLogger(CodeController.class);
    //private final Dotenv dotenv = Dotenv.configure().load();


    @Operation(summary = "Creates a Thread with an Initial Message for the AI Assistants",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful Run",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = RequestThreadDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Erro na requisição",
                            content = @Content)
            })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = {
            @ExampleObject(
                    name = "Request sample",
                    summary = "Request example",
                    value = "{\"initialMessage\": \"Project Name: Central Park Hotel, Programming Language: Javascript, Framework: React 18, Dependency Manager: NPM, Terminal to run the command: shell, Library for Unit Testing: Jest.\"}"
            )
    }))
    @CrossOrigin(origins = "*")
    @PostMapping("/createThread")
    public ResponseEntity<String> createThread(@RequestBody Map<String, String> payload) {
        try {
            String initialMessage = payload.get("initialMessage");
            if (initialMessage == null) {
                return ResponseEntity.badRequest().body("Initial Message Required");
            }
            String response = codeService.createThread(initialMessage);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error creating thread", e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "Retrieves messages from a Specific Thread",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Messages successfully retrieved",
                            content = @Content),
                    @ApiResponse(responseCode = "500", description = "Error retrieving response",
                            content = @Content)
            })
    @CrossOrigin(origins = "*")
    @GetMapping("/getThreadMessages")
    public ResponseEntity<String> getThreadMessages(@RequestParam String threadId) {
        try {
            if (threadId == null || threadId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("The threadId is required and cannot be empty.");
            }
            return codeService.getThreadMessages(threadId);
        } catch (Exception e) {
            logger.error("Error getting thread messages", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Adiciona uma nova mensagem a um Thread específico",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Mensagem adicionada com sucesso",
                            content = @Content(mediaType = "application/json")),
                    @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos",
                            content = @Content),
                    @ApiResponse(responseCode = "404", description = "Thread não encontrado",
                            content = @Content)
            })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = {
            @ExampleObject(
                    name = "Exemplo de requisição",
                    summary = "Exemplo de corpo da requisição",
                    value = "{\"threadId\": \"thread_exemplo123\", \"message\": \"Nova mensagem para o thread.\"}"
            )
    }))
    @CrossOrigin(origins = "*")
    @PostMapping("/addMessageToThread")
    public ResponseEntity<String> addMessageToThread(@RequestBody Map<String, String> request) {
        try {
            String threadId = request.get("threadId");
            String message = request.get("message");
            if (threadId == null || threadId.isEmpty()) {
                return ResponseEntity.badRequest().body("O 'threadId' é obrigatório.");
            }
            if (message == null || message.isEmpty()) {
                return ResponseEntity.badRequest().body("A 'message' é obrigatória.");
            }
            String response = codeService.addMessageToThread(threadId, message);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erro ao adicionar mensagem ao thread", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    @Operation(summary = "Receives an Image and a Message and returns the code for a User Story VIEW.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Code successfully generated",
                            content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)),
                    @ApiResponse(responseCode = "500", description = "Error processing image or message",
                            content = @Content)
            })
    @CrossOrigin(origins = "*")
    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> analyzeImage(@RequestPart("imageFile") MultipartFile imageFile,
                                               @RequestPart("message") String message) {
        try {
            String apiKey = "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"; /*dotenv.get("OPENAI_API_KEY")*/
            if (apiKey == null || apiKey.isEmpty()) {
                logger.error("API key not found in environment variables.");
                return ResponseEntity.internalServerError().body("API key is missing or not configured correctly.");
            }

            if (message == null || message.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Message is required and cannot be empty.");
            }

            if (imageFile.isEmpty()) {
                return ResponseEntity.badRequest().body("Image file is required and cannot be empty.");
            }

            byte[] imageBytes = imageFile.getBytes();
            String base64Image = codeService.encodeImageToBase64(imageBytes);
            String payload = codeService.createPayload(base64Image, message);
            String response = codeService.callOpenAiApi(payload, apiKey);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            logger.error("Error processing image", e);
            return ResponseEntity.internalServerError().body("Error processing image: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error occurred", e);
            return ResponseEntity.internalServerError().body("An unexpected error occurred: " + e.getMessage());
        }
    }

    @GetMapping("/downloadProject")
    public ResponseEntity<StreamingResponseBody> downloadProject(@RequestParam String projectName, HttpServletResponse response) {
        try {
            String projectPathString = codeService.getProjectPath(projectName);
            Path projectPath = Paths.get(projectPathString);
            Path zipPath = Paths.get(projectPathString + ".zip");

            if (!Files.exists(zipPath)) {
                if (!Files.exists(projectPath)) {
                    throw new RuntimeException("Projeto não encontrado: " + projectName);
                }
                codeService.zipFolder(projectPath, zipPath);
            }

            // Define o tipo de conteúdo e o cabeçalho de disposição de conteúdo para o arquivo
            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + zipPath.getFileName().toString() + "\"");

            StreamingResponseBody stream = outputStream -> {
                try (InputStream is = new FileInputStream(zipPath.toFile())) {
                    IOUtils.copy(is, outputStream);
                } catch (Exception e) {
                    throw new RuntimeException("Erro ao streamar o arquivo: " + e.getMessage());
                }
            };

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/zip"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + zipPath.getFileName().toString() + "\"")
                    .body(stream);

        } catch (Exception ex) {
            logger.error("Erro ao baixar o projeto", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
