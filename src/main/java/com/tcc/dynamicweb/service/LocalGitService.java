package com.tcc.dynamicweb.service;

import java.io.File;
import java.io.IOException;
import org.springframework.stereotype.Service;

@Service
public class LocalGitService {

    private final String currentDirectory = "C:\\Projects";

    public void initializeGitRepo(String projectName) throws IOException, InterruptedException {
        File projectDirectory = new File(getProjectPath(projectName));
        runCommand(projectDirectory, "git", "init");
        runCommand(projectDirectory, "git", "remote", "add", "origin", "<repository-url>");
        runCommand(projectDirectory, "git", "add", ".");
        runCommand(projectDirectory, "git", "commit", "-m", "Initial commit");
    }

    public void commitChanges(String projectName, String commitMessage) throws IOException, InterruptedException {
        File projectDirectory = new File(getProjectPath(projectName));
        runCommand(projectDirectory, "git", "add", ".");
        runCommand(projectDirectory, "git", "commit", "-m", commitMessage);
    }

    public void rollbackToCommit(String projectName, String commitId) throws IOException, InterruptedException {
        File projectDirectory = new File(getProjectPath(projectName));
        runCommand(projectDirectory, "git", "reset", "--hard", commitId);
    }

    private String getProjectPath(String projectName) {
        return currentDirectory + File.separator + projectName;
    }

    private void runCommand(File directory, String... command) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.directory(directory);
        builder.redirectErrorStream(true);
        Process process = builder.start();

        process.waitFor();
    }
}
