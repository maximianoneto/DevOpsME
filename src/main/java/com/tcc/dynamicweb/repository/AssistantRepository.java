package com.tcc.dynamicweb.repository;

import com.tcc.dynamicweb.model.Assistant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AssistantRepository extends JpaRepository<Assistant, Long> {
     Optional<Assistant> findAssistantByThreadId(String threadId);

     @Query("SELECT a.threadId FROM Assistant a WHERE a.project.name = :projectName AND a.type = 'TEST_GENERATOR'")
     String findTestThreadIdByProjectName(@Param("projectName") String projectName);
}
