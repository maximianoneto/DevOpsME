package com.tcc.dynamicweb.repository;

import com.tcc.dynamicweb.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
  String findThreadIdByName(String projectName);

  @Query("SELECT p FROM Project p JOIN p.assistants a WHERE p.name = :projectName AND a.threadId = :threadId")
  Optional<Project> findByNameAndThreadId(@Param("projectName") String projectName, @Param("threadId") String threadId);

}
