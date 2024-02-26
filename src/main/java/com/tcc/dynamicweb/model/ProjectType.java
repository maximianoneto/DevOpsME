package com.tcc.dynamicweb.model;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ProjectType {
    private String id;
    private String description;
    private String tags;
}
