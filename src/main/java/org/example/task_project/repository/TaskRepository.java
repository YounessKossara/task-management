package org.example.task_project.repository;

import org.example.task_project.entity.Task;
import org.example.task_project.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByProjectId(Long projectId);

    // Filtrer par projet + statut
    List<Task> findByProjectIdAndStatut(Long projectId, TaskStatus statut);

    // Filtrer par projet + utilisateur assigné
    List<Task> findByProjectIdAndAssigneeKeycloakId(Long projectId, String assigneeKeycloakId);

    // Filtrer par projet + statut + utilisateur
    List<Task> findByProjectIdAndStatutAndAssigneeKeycloakId(Long projectId, TaskStatus statut,
            String assigneeKeycloakId);
}
