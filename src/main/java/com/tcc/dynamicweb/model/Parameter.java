package com.tcc.dynamicweb.model;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class Parameter {
    private String id;
    private String description;
    private String defaultValue;
}
