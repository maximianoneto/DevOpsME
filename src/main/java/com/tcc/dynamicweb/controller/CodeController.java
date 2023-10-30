package com.tcc.dynamicweb.controller;


import com.tcc.dynamicweb.service.CodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


import java.util.Map;

@Controller
public class CodeController {

    @Autowired
    CodeService codeService;

    private final String url = "https://github.com/maximianoneto/dynamicweb/blob/master/src/main/java/com/max/dynamicweb/service/DynamicService.java";

    @PostMapping("/modify")
    public ResponseEntity<String> modifyClass(@RequestBody Map<String, String> payload) {
        String actualClass = null;
        try {

            String classContent = payload.get("payload");
            if (classContent == null) {
                return new ResponseEntity<>("classContent is required", HttpStatus.BAD_REQUEST);
            }
            actualClass = codeService.fetchFileContentAndPrint(payload);

            return ResponseEntity.ok(actualClass);

        } catch (Exception e) {
            e.printStackTrace();
            return (ResponseEntity<String>) ResponseEntity.badRequest();
        }

    }
}