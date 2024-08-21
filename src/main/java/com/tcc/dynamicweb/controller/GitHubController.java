package com.tcc.dynamicweb.controller;


import com.tcc.dynamicweb.model.ProjectRequest;
import com.tcc.dynamicweb.service.GitHubService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
@RequestMapping("/github")
@RestController
public class GitHubController {
    private GitHubService gitHubService;

    public GitHubController(GitHubService gitHubService) {
        this.gitHubService = gitHubService;
    }

    @PostMapping("/repository")
    public String createRepo(
            @RequestBody ProjectRequest projectRequest) {
        try {
            gitHubService.createAndCommitRepo(projectRequest.getProjectName(), projectRequest.getProjectDescription());
            return String.valueOf(ResponseEntity.ok("Repository created and files committed successfully."));
        } catch (IllegalArgumentException e) {
            return String.valueOf(ResponseEntity.badRequest().body(e.getMessage()));
        } catch (Exception e) {
            return String.valueOf(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create repository: " + e.getMessage()));
        }
    }
}
