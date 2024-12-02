package com.tcc.dynamicweb.model;


import jakarta.persistence.*;
import lombok.Data;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
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

    @Column(name = "programming_language")
    private String programmingLanguague;

    @Column(name = "additional_information")
    private String additionalInformation;

    @Column(name = "path_to_project")
    private String pathToProject;

    @Column(name = "feature_depends_backend")
    private boolean featureDependsBackend;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Assistant> assistants = new HashSet<>();

    @Override
    public int hashCode() {
        return Objects.hash(projectId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Project project = (Project) obj;
        return Objects.equals(projectId, project.projectId);
    }
}
