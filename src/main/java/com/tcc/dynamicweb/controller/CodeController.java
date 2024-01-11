package com.tcc.dynamicweb.controller;

import com.tcc.dynamicweb.service.CodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@Controller
public class CodeController {

    @Autowired
    CodeService codeService;


    // Endpoint para criar uma Thread com uma mensagem inicial
    @PostMapping("/createThread")
    public ResponseEntity<String> createThread(@RequestBody Map<String, String> payload) {
        try {
            String initialMessage = payload.get("initialMessage");
            if (initialMessage == null) {
                return new ResponseEntity<>("'Mensagen Inicial requerida", HttpStatus.BAD_REQUEST);
            }
            String response = codeService.createThread(initialMessage);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // Endpoint para enviar um Run em uma Thread específica
    @PostMapping("/sendRun")
    public ResponseEntity<String> sendRun(@RequestBody Map<String, String> payload) {
        try {
            String threadId = payload.get("threadId");
            String assistantId = payload.get("assistantId");
            if (threadId == null || assistantId == null) {
                return new ResponseEntity<>("'threadId' e 'assistantId' são requeridos", HttpStatus.BAD_REQUEST);
            }
            String response = codeService.sendRun(threadId, assistantId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }


    @GetMapping("/getThreadMessages")
    public ResponseEntity<String> getThreadMessages(@RequestBody Map<String, String> payload) {
        try {
            String threadId = payload.get("payload");
            String message = codeService.getThreadMessages(threadId);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/analyze")
    public ResponseEntity<String> analyzeImage(@RequestBody Map<String, String> requestBody) {
        try {
            String apiKey = "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"; // Substituir pelo mesma OPENAI_API_KEY
            String imagePath = requestBody.get("payload");
            String message = requestBody.get("message");
            String base64Image = codeService.encodeImageToBase64(imagePath);
            String payload = codeService.createPayload(base64Image, message);
            String response = codeService.callOpenAiApi(payload, apiKey);

           return ResponseEntity.ok(response);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error processing image");
        }
    }
}
