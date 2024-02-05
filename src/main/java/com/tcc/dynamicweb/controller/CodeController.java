package com.tcc.dynamicweb.controller;

import com.tcc.dynamicweb.service.CodeService;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
public class CodeController {

    @Autowired
    CodeService codeService;

    //private final Dotenv dotenv = Dotenv.configure().load();


    // Endpoint para criar uma Thread com uma mensagem inicial
    @CrossOrigin(origins = "*")
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
    @CrossOrigin(origins = "*")
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


    @CrossOrigin(origins = "*")
    @RequestMapping("/getThreadMessages")
    public ResponseEntity<String> getThreadMessages(@RequestParam String payload) {
        try {
            return codeService.getThreadMessages(payload);
        } catch (Exception e) {
            e.printStackTrace();
           return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/getFrontMessages")
    public ResponseEntity<String> getFrontMessage(@RequestBody Map<String, String> payload) {
        try {
            String threadId = payload.get("payload");
            return ResponseEntity.ok(codeService.getFrontMessage(threadId));
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }


    @CrossOrigin(origins = "*")
    @PostMapping("/analyze")
    public ResponseEntity<String> analyzeImage(@RequestBody Map<String, String> requestBody) {
        try {
            String apiKey = "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"; /*dotenv.get("OPENAI_API_KEY");*/
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
