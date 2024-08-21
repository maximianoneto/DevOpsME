package com.tcc.dynamicweb.controller;

import com.tcc.dynamicweb.service.LocalGitService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/git")
public class LocalGitController {

    private final LocalGitService localGitService;

    public LocalGitController(LocalGitService localGitService) {
        this.localGitService = localGitService;
    }

    @PostMapping("/initialize")
    public ResponseEntity<String> initializeRepo(@RequestParam String projectName) {
        try {
            localGitService.initializeGitRepo(projectName);
            return ResponseEntity.ok("Repositório Git inicializado com sucesso.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Falha ao inicializar o repositório: " + e.getMessage());
        }
    }

    @PostMapping("/commit")
    public ResponseEntity<String> commitChanges(@RequestParam String projectName, @RequestParam String commitMessage) {
        try {
            localGitService.commitChanges(projectName, commitMessage);
            return ResponseEntity.ok("Alterações commitadas com sucesso.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Falha ao commitar as alterações: " + e.getMessage());
        }
    }

    @PostMapping("/rollback")
    public ResponseEntity<String> rollbackCommit(@RequestParam String projectName, @RequestParam String commitId) {
        try {
            localGitService.rollbackToCommit(projectName, commitId);
            return ResponseEntity.ok("Rollback para o commit " + commitId + " realizado com sucesso.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Falha ao reverter o commit: " + e.getMessage());
        }
    }
}
