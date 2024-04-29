package com.tcc.dynamicweb.model;


import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Assistant {

    public enum AssistantType {
        CODE_GENERATOR, TEST_GENERATOR
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "assistant_id")
    private String assistantId;

    @Column(name = "thread_id")
    private String threadId;

    @Enumerated(EnumType.STRING)
    private AssistantType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Assistant)) return false;
        Assistant assistant = (Assistant) o;
        return assistantId != null && assistantId.equals(project.getProjectId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}

