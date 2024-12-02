package com.tcc.dynamicweb.service;

import org.kohsuke.github.*;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class GitHubService {

    private final String githubToken = "";
    private final String currentDirectory = "C:\\Projects"; // Local

    public String getProjectPath(String projectName) {
        return currentDirectory + File.separator + projectName;
    }

    public void createAndCommitRepo(String projectName, String repoDescription) throws IOException, InterruptedException {
        GitHub github = GitHub.connectUsingOAuth(githubToken);

        String formattedRepoName = formatRepoName(projectName);

        GHRepository repository;
        try {
            repository = github.getRepository(formattedRepoName);
        } catch (GHFileNotFoundException e) {
            GHCreateRepositoryBuilder builder = github.createRepository(formattedRepoName);
            repository = builder
                    .description(repoDescription)
                    .private_(false)
                    .create();
        }

        File projectDirectory = new File(getProjectPath(projectName));
        if (!projectDirectory.isDirectory()) {
            throw new IllegalArgumentException("O caminho não é um diretório válido");
        }

        initializeGitRepo(projectDirectory, repository.getHttpTransportUrl());
    }

    private String formatRepoName(String repoName) {
        if (!repoName.contains("/")) {
            String owner = "maximianoneto";
            return owner + "/" + repoName;
        }
        return repoName;
    }

    private void initializeGitRepo(File directory, String repoUrl) throws IOException, InterruptedException {
        runCommand(directory, "git", "init");

        // Verifica se o remote 'origin' já existe
        if (remoteExists(directory, "origin")) {
            runCommand(directory, "git", "remote", "set-url", "origin", repoUrl);
        } else {
            runCommand(directory, "git", "remote", "add", "origin", repoUrl);
        }

        // Realizar o pull para sincronizar com o repositório remoto
        try {
            runCommand(directory, "git", "pull", "origin", "main");
        } catch (IOException e) {
            System.out.println("Falha ao realizar o pull. Pode ser que a branch 'main' ainda não exista.");
        }

        runCommand(directory, "git", "add", ".");

        // Verifica se há mudanças para comitar
        if (isWorkingTreeClean(directory)) {
            System.out.println("Nada para commitar, working tree clean");
        } else {
            runCommand(directory, "git", "commit", "-m", "Commit de alterações");
        }

        runCommand(directory, "git", "push", "-u", "origin", "main");
    }

    private boolean isWorkingTreeClean(File directory) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder("git", "status", "--porcelain");
        builder.directory(directory);
        Process process = builder.start();
        String output = new String(process.getInputStream().readAllBytes());
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("Falha ao executar comando com código de saída " + exitCode + ": git status --porcelain");
        }
        return output.isEmpty();
    }

    private boolean remoteExists(File directory, String remoteName) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder("git", "remote", "get-url", remoteName);
        builder.directory(directory);
        Process process = builder.start();
        int exitCode = process.waitFor();
        return exitCode == 0;
    }

    private void runCommand(File directory, String... command) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.directory(directory);
        builder.redirectErrorStream(true);
        Process process = builder.start();

        StringBuilder output = new StringBuilder();
        new Thread(() -> {
            try (var reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append(System.lineSeparator());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("Falha ao executar comando com código de saída " + exitCode + ": " + String.join(" ", command) + "\nSaída: " + output);
        }

        System.out.println("Saída do comando: " + output);
    }
}
