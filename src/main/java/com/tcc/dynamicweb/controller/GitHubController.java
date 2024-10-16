package com.tcc.dynamicweb.controller;

import com.tcc.dynamicweb.model.ProjectRequest;
import com.tcc.dynamicweb.service.GitHubService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/github")
@CrossOrigin(origins = "*")
public class GitHubController {

    private final GitHubService gitHubService;

    public GitHubController(GitHubService gitHubService) {
        this.gitHubService = gitHubService;
    }

    @Operation(
            summary = "Creates a GitHub repository and commits initial files.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Project information including name and description.",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Project Request",
                                    value = "{\n  \"projectName\": \"MyRepo\",\n  \"projectDescription\": \"Description of my repo\"\n}"
                            )
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Repository created and files committed successfully."),
                    @ApiResponse(responseCode = "400", description = "Invalid project data provided."),
                    @ApiResponse(responseCode = "500", description = "Failed to create repository.")
            }
    )
    @PostMapping("/repository")
    public ResponseEntity<String> createRepo(@RequestBody ProjectRequest projectRequest) {
        try {
            gitHubService.createAndCommitRepo(projectRequest.getProjectName(), projectRequest.getProjectDescription());
            return ResponseEntity.ok("Repository created and files committed successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create repository: " + e.getMessage());
        }
    }
}
