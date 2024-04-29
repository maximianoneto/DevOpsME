package com.tcc.dynamicweb.repository;

import com.tcc.dynamicweb.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
  String findThreadIdByName(String projectName);


  Optional<Project> findByName(String projectName);
}
