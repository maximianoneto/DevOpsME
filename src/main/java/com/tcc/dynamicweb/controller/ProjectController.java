package com.tcc.dynamicweb.controller;

import com.tcc.dynamicweb.service.CodeService;
import com.tcc.dynamicweb.service.ProjectService;
import io.github.cdimascio.dotenv.Dotenv;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@RestController
@RequestMapping("/project")
@Tag(name = "Project Controller", description = "Controller for Projects")
public class ProjectController {

    @Autowired
    CodeService codeService;

    @Autowired
    private ProjectService projectService;

    private static final Logger logger = LoggerFactory.getLogger(CodeController.class);
    private final Dotenv dotenv = Dotenv.configure().load();



    @Operation(summary = "Cria um projeto atrelado um threadId",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Projeto Criado com Sucesso.",
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
    @PostMapping("/createProject")
    public ResponseEntity<String> createProject(@RequestBody Map<String, String> request) {
        try {
            String threadId = request.get("threadId");
            String projectName = request.get("projectName");
            String type = request.get("type");
            String additionalInformation = request.get("additionalInformation");

            if (threadId == null || threadId.isEmpty()) {
                return ResponseEntity.badRequest().body("O 'projectName' é obrigatório.");
            }
            String response = String.valueOf(projectService.createProject(projectName,type,threadId,additionalInformation));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erro ao criar projeto.", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/downloadProject")
    public ResponseEntity<StreamingResponseBody> downloadProject(@RequestParam String projectName, HttpServletResponse response) {
        try {
            logger.info("Iniciando download do projeto: {}", projectName);
            String projectPathString = projectService.getProjectPath(projectName);

            logger.info("Caminho do projeto obtido: {}", projectPathString);
            Path projectPath = Paths.get(projectPathString);

            logger.info("Path do projeto: {}", projectPath);
            Path zipPath = Paths.get(projectPathString + ".zip");

            logger.info("Caminho do arquivo ZIP: {}", zipPath);

            projectService.zipFolder(projectPath, zipPath);
            logger.info("ZIP criado com sucesso. Preparando para download: {}", zipPath.getFileName().toString());


            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + zipPath.getFileName().toString() + "\"");
            System.out.println("caminho do arquivo com o . zip"+zipPath.getFileName().toString());

            StreamingResponseBody stream = outputStream -> {
                try (InputStream is = new FileInputStream(zipPath.toFile())) {
                    IOUtils.copy(is, outputStream);
                } catch (FileNotFoundException fnfe) {
                    logger.error("Arquivo não encontrado: " + zipPath, fnfe);
                    throw new RuntimeException("Arquivo não encontrado: " + fnfe.getMessage());
                } catch (IOException ioe) {
                    logger.error("Erro de IO ao streamar o arquivo: " + zipPath, ioe);
                    throw new RuntimeException("Erro de IO ao streamar o arquivo: " + ioe.getMessage());
                } catch (Exception e) {
                    logger.error("Erro geral ao streamar o arquivo: " + zipPath, e);
                    throw new RuntimeException("Erro ao streamar o arquivo: " + e.getMessage());
                }
            };

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/zip"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + zipPath.getFileName().toString() + "\"")
                    .body(stream);


        } catch (Exception ex) {
            logger.error("Erro ao baixar o projeto", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
