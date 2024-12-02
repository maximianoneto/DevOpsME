package com.tcc.dynamicweb.controller;

import com.tcc.dynamicweb.service.LocalGitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/git")
@CrossOrigin(origins = "*")
public class LocalGitController {

    private final LocalGitService localGitService;

    public LocalGitController(LocalGitService localGitService) {
        this.localGitService = localGitService;
    }

    @Operation(
            summary = "Initializes a local Git repository for a given project.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Git repository initialized successfully."),
                    @ApiResponse(responseCode = "500", description = "Failed to initialize the repository.")
            }
    )
    @PostMapping("/initialize")
    public ResponseEntity<String> initializeRepo(@RequestParam String projectName) {
        try {
            localGitService.initializeGitRepo(projectName);
            return ResponseEntity.ok("Git repository initialized successfully.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to initialize the repository: " + e.getMessage());
        }
    }

    @Operation(
            summary = "Commits changes to a local Git repository.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Changes committed successfully."),
                    @ApiResponse(responseCode = "500", description = "Failed to commit changes.")
            }
    )
    @PostMapping("/commit")
    public ResponseEntity<String> commitChanges(@RequestParam String projectName, @RequestParam String commitMessage) {
        try {
            localGitService.commitChanges(projectName, commitMessage);
            return ResponseEntity.ok("Changes committed successfully.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to commit changes: " + e.getMessage());
        }
    }

    @Operation(
            summary = "Rolls back to a specific commit in a local Git repository.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Rollback to the specified commit was successful."),
                    @ApiResponse(responseCode = "500", description = "Failed to revert to the specified commit.")
            }
    )
    @PostMapping("/rollback")
    public ResponseEntity<String> rollbackCommit(@RequestParam String projectName, @RequestParam String commitId) {
        try {
            localGitService.rollbackToCommit(projectName, commitId);
            return ResponseEntity.ok("Rollback to commit " + commitId + " was successful.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to revert the commit: " + e.getMessage());
        }
    }
}
