package com.tcc.dynamicweb.controller;

import com.tcc.dynamicweb.model.dto.RequestThreadDTO;
import com.tcc.dynamicweb.service.CodeService;
import com.tcc.dynamicweb.service.ThreadService;
import io.github.cdimascio.dotenv.Dotenv;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/thread")
@Tag(name = "Code Controller", description = "Controller for Creation and Retrieve Thread Messages")
public class ThreadController {

    private static final Logger logger = LoggerFactory.getLogger(CodeController.class);
    private final Dotenv dotenv = Dotenv.configure().load();

    @Autowired
    ThreadService threadService;

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
                    value = "{\"initialMessage\": \"nome:Hostel, Linguagem de programação: Java 17, Framework: Spring Boot, Gerenciador de Dependencia: gradle, Dependencias adicionais: lombok\"}"
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
            String response = threadService.createThread(initialMessage);
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
    @GetMapping(value = "/getThreadMessages", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<String> getThreadMessages(@RequestParam String threadId) throws IOException, InterruptedException {
        try {

            if (threadId == null || threadId.trim().isEmpty()) {
                return ResponseEntity.noContent().build();
            }

            return threadService.getThreadMessages(threadId);
        }catch (Exception ex){
            return ResponseEntity.badRequest().body("Erro");
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
            boolean featureDependsBackend = Boolean.parseBoolean(request.get("type"));
            String projectNameBackend = request.get("projectNameBackend");
            if (threadId == null || threadId.isEmpty()) {
                return ResponseEntity.badRequest().body("O 'threadId' é obrigatório.");
            }
            if (message == null || message.isEmpty()) {
                return ResponseEntity.badRequest().body("A 'message' é obrigatória.");
            }
            if (featureDependsBackend) {
                String response = threadService.addMessageToThread(threadId, message, featureDependsBackend,projectNameBackend);
                return ResponseEntity.ok(response);
            }else {
                String response = threadService.addMessageToThread(threadId, message, featureDependsBackend,projectNameBackend);
                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            logger.error("Erro ao adicionar mensagem ao thread", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
