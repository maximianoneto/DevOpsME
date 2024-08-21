package com.tcc.dynamicweb.service;

import com.tcc.dynamicweb.model.Dependency;
import com.tcc.dynamicweb.model.Parameter;
import com.tcc.dynamicweb.model.ProjectType;
import com.tcc.dynamicweb.model.SpringInitInfo;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SpringCliService {

    String currentDirectory = "/projects";

    public List<String> executeSpringInitListCommand() {
        List<String> commandOutput = new ArrayList<>();
        String command = "spring init --list";
        //String cmdPrefix = "cmd /c ";
        String windowsCommand = command.replace("/", "\\"); // Ajuste para o sistema operacional Windows

        try {
            Process process = Runtime.getRuntime().exec(windowsCommand, null, new File(currentDirectory));

            try (InputStream stdInput = process.getInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(stdInput))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    commandOutput.add(line);
                }
            }

            try (InputStream stdError = process.getErrorStream();
                 BufferedReader readerErr = new BufferedReader(new InputStreamReader(stdError))) {
                String line;
                while ((line = readerErr.readLine()) != null) {
                    System.err.println(line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.err.println("O comando terminou com erros. Código de saída: " + exitCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return commandOutput;
    }

    public SpringInitInfo getSpringInitInfo() {
        List<String> lines = executeSpringInitListCommand();
        List<Dependency> dependencies = new ArrayList<>();
        List<ProjectType> projectTypes = new ArrayList<>();
        List<Parameter> parameters = new ArrayList<>();

        boolean readingDependencies = false, readingTypes = false, readingParameters = false;
        Pattern dependencyPattern = Pattern.compile("\\|\\s+([\\w-]+)\\s+\\|\\s+([^\\|]+)\\s+\\|\\s+([^\\|]*)\\s+\\|");
        Pattern typePattern = Pattern.compile("\\|\\s+([\\w-]+)\\s+\\|\\s+([^\\|]+)\\s+\\|\\s+([^\\|]+)\\s+\\|");
        Pattern parameterPattern = Pattern.compile("\\|\\s+([\\w-]+)\\s+\\|\\s+([^\\|]+)\\s+\\|\\s+([^\\|]+)\\s+\\|");

        for (String line : lines) {
            if (line.contains("Supported dependencies")) {
                readingDependencies = true;
                continue;
            } else if (line.contains("Project types")) {
                readingDependencies = false;
                readingTypes = true;
                continue;
            } else if (line.contains("Parameters")) {
                readingTypes = false;
                readingParameters = true;
                continue;
            } else if (line.trim().isEmpty() || line.startsWith("+---")) {
                continue;
            }

            if (readingDependencies) {
                Matcher matcher = dependencyPattern.matcher(line);
                if (matcher.find()) {
                    Dependency dep = Dependency.builder()
                            .id(matcher.group(1).trim())
                            .description(matcher.group(2).trim())
                            .reqVersion(matcher.group(3).trim().isEmpty() ? null : matcher.group(3).trim())
                            .build();
                    dependencies.add(dep);
                }
            } else if (readingTypes) {
                Matcher matcher = typePattern.matcher(line);
                if (matcher.find()) {
                    ProjectType type = ProjectType.builder()
                            .id(matcher.group(1).trim())
                            .description(matcher.group(2).trim())
                            .tags(matcher.group(3).trim())
                            .build();
                    projectTypes.add(type);
                }
            } else if (readingParameters) {
                Matcher matcher = parameterPattern.matcher(line);
                if (matcher.find()) {
                    Parameter param = Parameter.builder()
                            .id(matcher.group(1).trim())
                            .description(matcher.group(2).trim())
                            .defaultValue(matcher.group(3).trim())
                            .build();
                    parameters.add(param);
                }
            }
        }

        return SpringInitInfo.builder()
                .dependencies(dependencies)
                .projectTypes(projectTypes)
                .parameters(parameters)
                .build();
    }



}

