package com.tcc.dynamicweb.controller;

import com.tcc.dynamicweb.service.CodeService;
import io.github.cdimascio.dotenv.Dotenv;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
// Note: No need to import RequestBody again from Spring if using fully qualified names
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "Code Controller", description = "Controller for code generation and analysis")
public class CodeController {

    @Autowired
    private CodeService codeService;

    private static final Logger logger = LoggerFactory.getLogger(CodeController.class);
    private final Dotenv dotenv = Dotenv.configure().load();

    @CrossOrigin("*")
    @PostMapping("/addCode")
    @Operation(
            summary = "Adiciona código ao projeto",
            description = "Endpoint para adicionar código a um projeto existente.",
            requestBody = @RequestBody(
                    description = "Dados para adicionar código",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Exemplo de adição de código",
                                    value = "{\n" +
                                            "  \"threadId\": \"thread_wudttBmK8bXWy5tNzP4cNIFh\",\n" +
                                            "  \"projectName\": \"Hostel\"\n" +
                                            "}"
                            )
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Código adicionado com sucesso"),
                    @ApiResponse(responseCode = "400", description = "Dados inválidos"),
                    @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
            }
    )
    public ResponseEntity<String> addCode(@org.springframework.web.bind.annotation.RequestBody Map<String, String> request) {
        try {
            String threadId = request.get("threadId");
            String projectName = request.get("projectName");
            if (threadId == null || threadId.isEmpty()) {
                return ResponseEntity.badRequest().body("The 'threadId' is required.");
            }
            String response = String.valueOf(codeService.addCode(threadId, projectName));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error adding code to project.", e);
            return ResponseEntity.internalServerError().body("Internal server error.");
        }
    }

    @Operation(
            summary = "Analyzes an image and a message to generate code for a user interface component.",
            requestBody = @RequestBody(
                    description = "Multipart/form-data request containing an image file and a message.",
                    required = true
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Code successfully generated.", content = @Content),
                    @ApiResponse(responseCode = "400", description = "Invalid request data.", content = @Content),
                    @ApiResponse(responseCode = "500", description = "Internal server error.", content = @Content)
            }
    )
    @CrossOrigin(origins = "*")
    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> analyzeImage(
            @Parameter(description = "The image file to analyze.", required = true)
            @RequestPart("imageFile") MultipartFile imageFile,
            @Parameter(description = "The message describing the user story.", required = true)
            @RequestPart("message") String message) {
        try {
            String apiKey = dotenv.get("OPENAI_API_KEY");
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
}
