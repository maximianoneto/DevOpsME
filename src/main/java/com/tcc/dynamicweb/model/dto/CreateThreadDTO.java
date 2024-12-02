package com.tcc.dynamicweb.model.dto;

import lombok.Data;

@Data
public class CreateThreadDTO {
    String projectName;
    String programmingLanguage;
    String versionOfProgrammingLanguage;
    String framework;
    String dependencyManager;
    String additionalDependencies;
}
