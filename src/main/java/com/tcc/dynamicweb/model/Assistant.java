package com.tcc.dynamicweb.model;


import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.util.Objects;

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
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Assistant assistant = (Assistant) obj;
        return Objects.equals(id, assistant.id);
    }

}

