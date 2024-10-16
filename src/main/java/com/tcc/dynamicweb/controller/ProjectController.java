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
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@RestController
@RequestMapping("/project")
@Tag(name = "Project Controller", description = "Controller for managing projects")
public class ProjectController {

    @Autowired
    private CodeService codeService;

    @Autowired
    private ProjectService projectService;

    private static final Logger logger = LoggerFactory.getLogger(ProjectController.class);
    private final Dotenv dotenv = Dotenv.configure().load();

    @CrossOrigin("*")
    @PostMapping("/createProject")
    @Operation(
            summary = "Cria um novo projeto",
            description = "Endpoint para criar um novo projeto.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados para criação do projeto",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Exemplo de criação de projeto",
                                            value = "{\n" +
                                                    "  \"threadId\": \"thread_wudttBmK8bXWy5tNzP4cNIFh\",\n" +
                                                    "  \"projectName\": \"Hostel\",\n" +
                                                    "  \"type\": \"web\",\n" +
                                                    "  \"additionalInformation\": \"Some info\",\n" +
                                                    "  \"programmingLanguage\": \"java\"\n" +
                                                    "}"
                                    )
                            }
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Projeto criado com sucesso"),
                    @ApiResponse(responseCode = "400", description = "Dados inválidos"),
                    @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
            }
    )
    public ResponseEntity<String> createProject(@RequestBody Map<String, String> request) {
        try {
            String threadId = request.get("threadId");
            String projectName = request.get("projectName");
            String type = request.get("type");
            String additionalInformation = request.get("additionalInformation");
            String programmingLanguage = request.get("programmingLanguage");
            if (threadId == null || threadId.isEmpty()) {
                return ResponseEntity.badRequest().body("The 'threadId' is required.");
            }
            String response = String.valueOf(projectService.createProject(projectName, type, threadId, additionalInformation, programmingLanguage));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error creating project.", e);
            return ResponseEntity.internalServerError().body("Internal server error.");
        }
    }

    @Operation(
            summary = "Downloads the specified project as a ZIP file.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Project downloaded successfully.", content = @Content),
                    @ApiResponse(responseCode = "500", description = "Error downloading the project.", content = @Content)
            }
    )
    @CrossOrigin(origins = "*")
    @GetMapping("/downloadProject")
    public ResponseEntity<StreamingResponseBody> downloadProject(@RequestParam String projectName, HttpServletResponse response) {
        try {
            logger.info("Starting download for project: {}", projectName);
            Path projectPath = projectService.getProjectPath(projectName);

            if (!Files.exists(projectPath)) {
                logger.error("Project directory does not exist: {}", projectPath);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            Path zipPath = projectPath.resolveSibling(projectName + ".zip");

            projectService.zipFolder(projectPath, zipPath);
            logger.info("ZIP created successfully: {}", zipPath.getFileName().toString());

            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + zipPath.getFileName().toString() + "\"");

            StreamingResponseBody stream = outputStream -> {
                try (InputStream is = Files.newInputStream(zipPath)) {
                    IOUtils.copy(is, outputStream);
                } catch (Exception e) {
                    logger.error("Error streaming the ZIP file: " + zipPath, e);
                    throw new RuntimeException("Error streaming the file: " + e.getMessage());
                }
            };

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + zipPath.getFileName().toString() + "\"")
                    .body(stream);

        } catch (Exception ex) {
            logger.error("Error downloading the project", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
