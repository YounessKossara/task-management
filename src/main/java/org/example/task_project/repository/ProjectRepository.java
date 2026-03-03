package org.example.task_project.repository;

import org.example.task_project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByResponsableKeycloakId(String responsableKeycloakId);

    @Query("SELECT DISTINCT p FROM Project p INNER JOIN Task t ON t.project.id = p.id WHERE t.assigneeKeycloakId = :assigneeKeycloakId")
    List<Project> findProjectsByAssigneeKeycloakId(@Param("assigneeKeycloakId") String assigneeKeycloakId);
}
