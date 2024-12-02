package com.tcc.dynamicweb.controller;


import com.tcc.dynamicweb.model.dto.AddMessageRequestDTO;
import com.tcc.dynamicweb.model.dto.CreateThreadDTO;
import com.tcc.dynamicweb.model.dto.RequestThreadDTO;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/thread") // Use a specific base path
@Tag(name = "Thread Controller", description = "Controller for Creation and Retrieve Thread Messages")
public class ThreadController {

    private static final Logger logger = LoggerFactory.getLogger(ThreadController.class);
    private final Dotenv dotenv = Dotenv.configure().load();

    @Autowired
    private ThreadService threadService;

    @Operation(
            summary = "Creates a Thread with an Initial Message for the AI Assistants",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Thread created successfully",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid request",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Server error",
                            content = @Content
                    )
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Details for creating a new thread",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CreateThreadDTO.class),
                            examples = @ExampleObject(
                                    name = "Create Thread Example",
                                    summary = "Example for creating a thread",
                                    value = "{\n  \"projectName\": \"MyProject\",\n  \"programmingLanguage\": \"Java\",\n  \"versionOfProgrammingLanguage\": \"17\",\n  \"framework\": \"Spring Boot 3.3.0\",\n  \"dependencyManager\": \"Maven\",\n  \"additionalDependencies\": \"Lombok, MapStruct\"\n}"
                            )
                    )
            )
    )
    @CrossOrigin(origins = "*")
    @PostMapping("/createThread")
    public ResponseEntity<?> createThread(@RequestBody CreateThreadDTO createThreadDTO) {
        try {
            if (createThreadDTO.getProgrammingLanguage() == null || createThreadDTO.getProgrammingLanguage().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Mensagem inicial é obrigatória.");
            }
            if (createThreadDTO.getProjectName() == null || createThreadDTO.getProjectName().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Nome do projeto é obrigatório.");
            }
            String threadId = threadService.createThread(createThreadDTO);
            return ResponseEntity.ok(Map.of("threadId", threadId));
        } catch (Exception e) {
            logger.error("Falha ao criar thread.", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Falha ao criar thread.");
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
    @GetMapping("/thread/{threadId}")
    public ResponseEntity<?> getThreadMessages(@PathVariable String threadId) {
        try {
            String messages = threadService.getThreadMessages(threadId).getBody();
            return ResponseEntity.ok(Map.of(
                    "threadId", threadId,
                    "messages", messages != null ? messages.split("\n") : new String[]{}
            ));
        } catch (Exception ex) {
            logger.error("Falha ao carregar o thread.", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Falha ao carregar o thread.");
        }
    }

    @Operation(
            summary = "Adds a new message to a specific thread",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Message added successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(type = "string"),
                                    examples = @ExampleObject(
                                            name = "Success Response",
                                            summary = "Example success response",
                                            value = "\"Mensagem adicionada com sucesso.\""
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid request",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Server error",
                            content = @Content
                    )
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Details for adding a message to a thread",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AddMessageRequestDTO.class),
                            examples = @ExampleObject(
                                    name = "Add Message Example",
                                    summary = "Example request for adding a message",
                                    value = "{\n  \"threadId\": \"thread123\",\n  \"message\": \"As a booking manager, I want to make a reservation for a customer in a specific period of time so that I do not run the risk of running out of rooms or the desired reservation period\",\n  \"featureDependsBackend\": false,\n  \"projectName\": \"BackendProject\"\n}"
                            )
                    )
            )
    )
    @CrossOrigin(origins = "*")
    @PostMapping("/addMessage")
    public ResponseEntity<?> addMessageToThread(@RequestBody AddMessageRequestDTO requestDTO) {
        try {
            if (requestDTO.getThreadId() == null || requestDTO.getThreadId().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("O 'threadId' é obrigatório.");
            }
            if (requestDTO.getMessage() == null || requestDTO.getMessage().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("A 'mensagem' é obrigatória.");
            }

            threadService.addMessageToThread(
                    requestDTO.getThreadId(),
                    requestDTO.getMessage(),
                    requestDTO.isFeatureDependsBackend(),
                    requestDTO.getProjectName()
            );
            return ResponseEntity.ok("Mensagem adicionada com sucesso.");
        } catch (Exception e) {
            logger.error("Falha ao adicionar mensagem.", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Falha ao adicionar mensagem.");
        }
    }

}
