package com.tcc.dynamicweb.model;


import jakarta.persistence.*;
import lombok.Data;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Entity
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long projectId;

    @Column(name = "name")
    private String name;

    @Column(name = "type")
    private String type;

    @Column(name = "additional_information")
    private String additionalInformation;

    @Column(name = "path_to_project")
    private String pathToProject;

    @Column(name = "feature_depends_backend")
    private boolean featureDependsBackend;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Assistant> assistants = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Project)) return false;
        Project project = (Project) o;
        return projectId != null && projectId.equals(project.getProjectId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}
