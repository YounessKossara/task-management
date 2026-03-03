package org.example.task_project.repository;

import org.example.task_project.entity.Task;
import org.example.task_project.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByProjectId(Long projectId);

    List<Task> findByProjectIdAndStatut(Long projectId, TaskStatus statut);

    List<Task> findByProjectIdAndAssigneeKeycloakId(Long projectId, String assigneeKeycloakId);

    List<Task> findByProjectIdAndStatutAndAssigneeKeycloakId(Long projectId, TaskStatus statut,
            String assigneeKeycloakId);
}
