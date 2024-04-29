package com.tcc.dynamicweb.controller;

import com.tcc.dynamicweb.model.dto.RequestThreadDTO;
import com.tcc.dynamicweb.service.CodeService;
import io.github.cdimascio.dotenv.Dotenv;
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
//import org.springframework.ai.chat.ChatResponse;
//import org.springframework.ai.openai.OpenAiChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import reactor.core.publisher.Flux;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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
    private final Dotenv dotenv = Dotenv.configure().load();

    @Operation(summary = "Adiciona código a um projeto atrelado ao threadId",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Código Adicionado com Sucesso.",
                            content = @Content(mediaType = "application/json")),
                    @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos",
                            content = @Content),
                    @ApiResponse(responseCode = "404", description = "threadId não encontrado",
                            content = @Content)
            })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = {
            @ExampleObject(
                    name = "Exemplo de requisição",
                    summary = "Exemplo de corpo da requisição",
                    value = "{\"threadId\": \"thread_1vGxXnYfJwfO7HioNTN1fwMD\"}"
            )
    }))
    @CrossOrigin(origins = "*")
    @PostMapping("/addCode")
    public ResponseEntity<String> addCode(@RequestBody Map<String, String> request) {
        try {
            String threadId = request.get("threadId");
            if (threadId == null || threadId.isEmpty()) {
                return ResponseEntity.badRequest().body("O 'threadId' é obrigatório.");
            }
            String response = String.valueOf(codeService.addCode(threadId));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erro ao criar projeto.", e);
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
