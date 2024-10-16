package com.tcc.dynamicweb.controller;

import com.tcc.dynamicweb.model.SpringInitInfo;
import com.tcc.dynamicweb.service.SpringCliService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/spring")
@CrossOrigin(origins = "*")
public class SpringCliController {

    @Autowired
    private SpringCliService springCliService;

    private static final Logger logger = LoggerFactory.getLogger(SpringCliController.class);

    @Operation(
            summary = "Retrieves available Spring Boot dependencies.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Dependencies retrieved successfully.", content = @Content),
                    @ApiResponse(responseCode = "400", description = "Error retrieving dependencies.", content = @Content)
            }
    )
    @GetMapping("/dependencies")
    public ResponseEntity<?> getDependencies() {
        try {
            SpringInitInfo dependencies = springCliService.getSpringInitInfo();
            return ResponseEntity.ok(dependencies);
        } catch (Exception e) {
            logger.error("Error retrieving dependencies: ", e);
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Error retrieving dependencies: " + e.getMessage());
        }
    }
}
