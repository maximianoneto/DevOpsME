package com.tcc.dynamicweb.controller;

import com.tcc.dynamicweb.service.ThreadService;
import io.github.cdimascio.dotenv.Dotenv;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/thread")
@Tag(name = "Thread Controller", description = "Controller for creating and retrieving thread messages")
@CrossOrigin(origins = "*")
public class ThreadController {

    private static final Logger logger = LoggerFactory.getLogger(ThreadController.class);
    private final Dotenv dotenv = Dotenv.configure().load();

    @Autowired
    private ThreadService threadService;

    @CrossOrigin("*")
    @PostMapping("/thread/createThread")
    @Operation(
            summary = "Cria uma nova thread",
            description = "Endpoint para criar uma nova thread.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Mensagem inicial para a thread",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Exemplo de criação de thread",
                                    value = "{\n" +
                                            "  \"initialMessage\": \"nome:Hostel, Linguagem de programação: Java 17, Framework: Spring Boot, Gerenciador de Dependencia: gradle, Dependencias adicionais: lombok\"\n" +
                                            "}"
                            )
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Thread criada com sucesso"),
                    @ApiResponse(responseCode = "400", description = "Dados inválidos"),
                    @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
            }
    )
    public ResponseEntity<String> createThread(@RequestBody Map<String, String> payload) {
        try {
            String initialMessage = payload.get("initialMessage");
            if (initialMessage == null) {
                return ResponseEntity.badRequest().body("Initial message is required.");
            }
            String response = threadService.createThread(initialMessage);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error creating thread", e);
            return ResponseEntity.badRequest().body("Error creating thread: " + e.getMessage());
        }
    }

    @Operation(
            summary = "Retrieves messages from a specific thread.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Messages retrieved successfully.", content = @Content),
                    @ApiResponse(responseCode = "204", description = "No content found for the thread.", content = @Content),
                    @ApiResponse(responseCode = "400", description = "Invalid thread ID provided.", content = @Content)
            }
    )
    @GetMapping(value = "/getThreadMessages", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<String> getThreadMessages(@RequestParam String threadId) {
        try {
            if (threadId == null || threadId.trim().isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return threadService.getThreadMessages(threadId);
        } catch (Exception ex) {
            logger.error("Error retrieving thread messages", ex);
            return ResponseEntity.badRequest().body("Error retrieving thread messages: " + ex.getMessage());
        }
    }

    @CrossOrigin("*")
    @PostMapping("/thread/addMessageToThread")
    @Operation(
            summary = "Adiciona uma mensagem à thread",
            description = "Endpoint para adicionar uma mensagem a uma thread existente.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados para adicionar mensagem à thread",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Exemplo de adição de mensagem à thread",
                                    value = "{\n" +
                                            "  \"threadId\": \"thread_wudttBmK8bXWy5tNzP4cNIFh\",\n" +
                                            "  \"message\": \"user story - Eu como administrador, eu quero incluir novas mensagens de boas vindas para disponibilizar o maior número possivel de linguagens. Critério de Aceitação: O sistema deve permitir que apenas usuário com a role admin possam incluir novas mensagens de boas vindas. Relatório técninco: o sistema deve possuir 3 endpoint, sendo um deles para Registro de usuário, o outro endpoint para autentição de usuário e o terceiro endpoint deve incluir novas mensagens de boas vindas se o usuário for da role admin\"\n" +
                                            "}"
                            )
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Mensagem adicionada com sucesso"),
                    @ApiResponse(responseCode = "400", description = "Dados inválidos"),
                    @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
            }
    )
    public ResponseEntity<String> addMessageToThread(@RequestBody Map<String, String> request) {
        try {
            String threadId = request.get("threadId");
            String message = request.get("message");
            boolean featureDependsBackend = Boolean.parseBoolean(request.get("type"));
            String projectNameBackend = request.get("projectNameBackend");
            if (threadId == null || threadId.isEmpty()) {
                return ResponseEntity.badRequest().body("The 'threadId' is required.");
            }
            if (message == null || message.isEmpty()) {
                return ResponseEntity.badRequest().body("The 'message' is required.");
            }
            String response = threadService.addMessageToThread(threadId, message, featureDependsBackend, projectNameBackend);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error adding message to thread", e);
            return ResponseEntity.internalServerError().body("Internal server error.");
        }
    }
}
