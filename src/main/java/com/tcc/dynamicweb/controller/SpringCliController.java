package com.tcc.dynamicweb.controller;

import com.tcc.dynamicweb.model.SpringInitInfo;
import com.tcc.dynamicweb.service.SpringCliService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/spring")
public class SpringCliController {

    @Autowired
    private SpringCliService springCliService;

    private static final Logger logger = LoggerFactory.getLogger(SpringCliController.class);


    @GetMapping("/dependencies")
    public ResponseEntity<?> getDependencies() {
        try {
            SpringInitInfo dependencies = springCliService.getSpringInitInfo();
            return ResponseEntity.ok(dependencies);
        } catch (Exception e) {
            logger.error("Erro ao obter as dependências: ", e);
            // Inclui a mensagem de erro no corpo da resposta de erro
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Erro ao obter as dependências: " + e.getMessage());
        }
    }
}

