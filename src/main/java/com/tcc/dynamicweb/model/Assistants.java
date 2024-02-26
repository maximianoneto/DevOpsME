package com.tcc.dynamicweb.model;

import lombok.Getter;

@Getter
public enum Assistants {

    JAVA_ASSISTANT("asst_P1Mlu6C8nZBevGH0yvX5aK35"),
    REACT_ASSISTANT("asst_OxHBt8GMEc3x4N8QPqi0wrma"),

    NEXT_ASSISTANT("next_assistant");


    private final String id;

    Assistants(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
