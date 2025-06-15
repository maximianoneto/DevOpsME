package com.tcc.dynamicweb.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Simple Swing-based desktop client for invoking DevOpsME REST endpoints.
 */
public class DesktopApp {
    private static final String DEFAULT_BASE_URL = "http://localhost:8080";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private JTextArea responseArea;
    private JTextField baseUrlField;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DesktopApp().createAndShowGui());
    }

    private void createAndShowGui() {
        JFrame frame = new JFrame("DevOpsME Desktop Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 500);

        JPanel north = new JPanel(new BorderLayout());
        north.add(new JLabel("Base URL:"), BorderLayout.WEST);
        baseUrlField = new JTextField(DEFAULT_BASE_URL);
        north.add(baseUrlField, BorderLayout.CENTER);
        frame.add(north, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Create Thread", createThreadPanel());
        tabs.addTab("Create Project", createProjectPanel());
        tabs.addTab("Add Message", createAddMessagePanel());
        tabs.addTab("Add Code", createAddCodePanel());
        tabs.addTab("Download", createDownloadPanel());
        frame.add(tabs, BorderLayout.CENTER);

        responseArea = new JTextArea();
        responseArea.setEditable(false);
        frame.add(new JScrollPane(responseArea), BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    private JPanel createThreadPanel() {
        JPanel panel = new JPanel(new GridLayout(0,2));
        JTextField projectName = new JTextField();
        JTextField language = new JTextField();
        JTextField version = new JTextField();
        JTextField framework = new JTextField();
        JTextField dependencyManager = new JTextField();
        JTextField additionalDeps = new JTextField();

        panel.add(new JLabel("Project Name")); panel.add(projectName);
        panel.add(new JLabel("Language")); panel.add(language);
        panel.add(new JLabel("Version")); panel.add(version);
        panel.add(new JLabel("Framework")); panel.add(framework);
        panel.add(new JLabel("Dependency Manager")); panel.add(dependencyManager);
        panel.add(new JLabel("Additional Deps")); panel.add(additionalDeps);

        JButton send = new JButton("Create Thread");
        send.addActionListener(e -> {
            String json = String.format("{\"projectName\":\"%s\",\"programmingLanguage\":\"%s\",\"versionOfProgrammingLanguage\":\"%s\",\"framework\":\"%s\",\"dependencyManager\":\"%s\",\"additionalDependencies\":\"%s\"}",
                    projectName.getText(), language.getText(), version.getText(), framework.getText(), dependencyManager.getText(), additionalDeps.getText());
            post("/thread/createThread", json);
        });
        panel.add(send);
        return panel;
    }

    private JPanel createProjectPanel() {
        JPanel panel = new JPanel(new GridLayout(0,2));
        JTextField threadId = new JTextField();
        JTextField projectName = new JTextField();
        JTextField type = new JTextField();
        JTextField additionalInfo = new JTextField();
        JTextField language = new JTextField();

        panel.add(new JLabel("Thread ID")); panel.add(threadId);
        panel.add(new JLabel("Project Name")); panel.add(projectName);
        panel.add(new JLabel("Type")); panel.add(type);
        panel.add(new JLabel("Additional Info")); panel.add(additionalInfo);
        panel.add(new JLabel("Language")); panel.add(language);

        JButton send = new JButton("Create Project");
        send.addActionListener(e -> {
            String json = String.format("{\"threadId\":\"%s\",\"projectName\":\"%s\",\"type\":\"%s\",\"additionalInformation\":\"%s\",\"programmingLanguage\":\"%s\"}",
                    threadId.getText(), projectName.getText(), type.getText(), additionalInfo.getText(), language.getText());
            post("/project/createProject", json);
        });
        panel.add(send);
        return panel;
    }

    private JPanel createAddMessagePanel() {
        JPanel panel = new JPanel(new GridLayout(0,2));
        JTextField threadId = new JTextField();
        JTextField message = new JTextField();
        JCheckBox backend = new JCheckBox("Depends on Backend");
        JTextField projectName = new JTextField();

        panel.add(new JLabel("Thread ID")); panel.add(threadId);
        panel.add(new JLabel("Message")); panel.add(message);
        panel.add(backend); panel.add(new JLabel());
        panel.add(new JLabel("Project Name")); panel.add(projectName);

        JButton send = new JButton("Add Message");
        send.addActionListener(e -> {
            String json = String.format("{\"threadId\":\"%s\",\"message\":\"%s\",\"featureDependsBackend\":%b,\"projectName\":\"%s\"}",
                    threadId.getText(), message.getText(), backend.isSelected(), projectName.getText());
            post("/thread/addMessage", json);
        });
        panel.add(send);
        return panel;
    }

    private JPanel createAddCodePanel() {
        JPanel panel = new JPanel(new GridLayout(0,2));
        JTextField threadId = new JTextField();
        JTextField projectName = new JTextField();

        panel.add(new JLabel("Thread ID")); panel.add(threadId);
        panel.add(new JLabel("Project Name")); panel.add(projectName);

        JButton send = new JButton("Add Code");
        send.addActionListener(e -> {
            String json = String.format("{\"threadId\":\"%s\",\"projectName\":\"%s\"}",
                    threadId.getText(), projectName.getText());
            post("/api/addCode", json);
        });
        panel.add(send);
        return panel;
    }

    private JPanel createDownloadPanel() {
        JPanel panel = new JPanel(new GridLayout(0,2));
        JTextField projectName = new JTextField();
        panel.add(new JLabel("Project Name")); panel.add(projectName);
        JButton send = new JButton("Download ZIP");
        send.addActionListener(e -> download(projectName.getText()));
        panel.add(send);
        return panel;
    }

    private void post(String path, String json) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrlField.getText().trim() + path))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            responseArea.setText(response.body());
        } catch (IOException | InterruptedException e) {
            responseArea.setText("Request failed: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    private void download(String projectName) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showSaveDialog(null) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        Path dir = chooser.getSelectedFile().toPath();
        Path target = dir.resolve(projectName + ".zip");
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrlField.getText().trim() + "/project/downloadProject?projectName=" + projectName))
                    .build();
            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            Files.write(target, response.body());
            responseArea.setText("Saved to " + target.toString());
        } catch (IOException | InterruptedException e) {
            responseArea.setText("Download failed: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
}

