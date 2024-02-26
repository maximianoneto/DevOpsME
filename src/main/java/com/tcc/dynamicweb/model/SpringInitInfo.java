package com.tcc.dynamicweb.model;


import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Builder
@Data
public class SpringInitInfo {
    private List<Dependency> dependencies = new ArrayList<>();
    private List<ProjectType> projectTypes = new ArrayList<>();
    private List<Parameter> parameters = new ArrayList<>();
}
