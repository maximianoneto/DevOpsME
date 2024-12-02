package com.tcc.dynamicweb.model.dto;

import lombok.Data;

@Data
public class AddMessageRequestDTO {
    private String threadId;
    private String message;
    private boolean featureDependsBackend;
    private String projectName;

}

