package org.example.task_project.service;

import org.example.task_project.dto.TaskDto;
import org.example.task_project.entity.Project;
import org.example.task_project.entity.Task;
import org.example.task_project.enums.TaskStatus;
import org.example.task_project.exception.AccessDeniedException;
import org.example.task_project.exception.ResourceNotFoundException;
import org.example.task_project.mapper.TaskMapper;
import org.example.task_project.repository.ProjectRepository;
import org.example.task_project.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskService {

    private static final String TACHE_NON_TROUVEE = "Tâche non trouvée: ";

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final TaskMapper taskMapper;

    public TaskService(TaskRepository taskRepository, ProjectRepository projectRepository, TaskMapper taskMapper) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.taskMapper = taskMapper;
    }

    public List<TaskDto> getTasksByProject(Long projectId, TaskStatus statut, String requestedAssigneeId,
            String currentUserId, boolean isAdmin) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Projet non trouvé: " + projectId));

        String actualAssigneeId = requestedAssigneeId;

        if (!isAdmin) {
            boolean isProjectResponsable = project.getResponsableKeycloakId() != null
                    && project.getResponsableKeycloakId().equals(currentUserId);

            if (!isProjectResponsable) {
                // Si non admin et non responsable du projet, limite la vue à ses tâches
                actualAssigneeId = currentUserId;
            }
        }

        List<Task> tasks;

        if (statut != null && actualAssigneeId != null) {
            tasks = taskRepository.findByProjectIdAndStatutAndAssigneeKeycloakId(projectId, statut, actualAssigneeId);
        } else if (statut != null) {
            tasks = taskRepository.findByProjectIdAndStatut(projectId, statut);
        } else if (actualAssigneeId != null) {
            tasks = taskRepository.findByProjectIdAndAssigneeKeycloakId(projectId, actualAssigneeId);
        } else {
            tasks = taskRepository.findByProjectId(projectId);
        }

        return tasks.stream()
                .map(taskMapper::toDto)
                .toList();
    }

    public TaskDto createTask(Long projectId, TaskDto taskDto, String currentUserId, boolean isAdmin) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Projet non trouvé: " + projectId));

        boolean isProjectResponsable = project.getResponsableKeycloakId() != null
                && project.getResponsableKeycloakId().equals(currentUserId);

        if (!isAdmin && !isProjectResponsable) {
            throw new AccessDeniedException(
                    "Accès refusé : Seul le responsable du projet ou un Admin peut y créer une tâche.");
        }

        if (taskDto.getAssigneeKeycloakId() != null
                && taskDto.getAssigneeKeycloakId().equals(project.getResponsableKeycloakId())) {
            throw new IllegalArgumentException(
                    "Le responsable du projet ne peut pas s'assigner des tâches dans ce projet.");
        }

        Task task = taskMapper.toEntity(taskDto);
        task.setProject(project);
        task.setStatut(taskDto.getStatut() != null ? taskDto.getStatut() : TaskStatus.TODO);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());

        Task saved = taskRepository.save(task);
        return taskMapper.toDto(saved);
    }

    public TaskDto updateTask(Long id, TaskDto taskDto, String currentUserId, boolean isAdmin) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(TACHE_NON_TROUVEE + id));

        boolean isProjectResponsable = task.getProject().getResponsableKeycloakId() != null
                && task.getProject().getResponsableKeycloakId().equals(currentUserId);

        if (!isAdmin && !isProjectResponsable) {
            throw new AccessDeniedException(
                    "Accès refusé : Seul le responsable du projet ou un Admin peut modifier une tâche.");
        }

        if (taskDto.getAssigneeKeycloakId() != null
                && taskDto.getAssigneeKeycloakId().equals(task.getProject().getResponsableKeycloakId())) {
            throw new IllegalArgumentException(
                    "Le responsable du projet ne peut pas s'assigner des tâches dans ce projet.");
        }

        task.setTitre(taskDto.getTitre());
        task.setDescription(taskDto.getDescription());
        task.setStatut(taskDto.getStatut());
        task.setPriorite(taskDto.getPriorite());
        task.setAssigneeKeycloakId(taskDto.getAssigneeKeycloakId());
        task.setUpdatedAt(LocalDateTime.now());

        Task saved = taskRepository.save(task);
        return taskMapper.toDto(saved);
    }

    public TaskDto updateTaskStatus(Long id, TaskStatus newStatus, String currentUserId, boolean isAdmin) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(TACHE_NON_TROUVEE + id));

        // Vérifier l'autorisation : ADMIN, responsable du projet, ou assigné à la tâche
        if (!isAdmin) {
            String assignee = task.getAssigneeKeycloakId();
            String responsable = task.getProject().getResponsableKeycloakId();
            boolean isAssignee = currentUserId != null && currentUserId.equals(assignee);
            boolean isResponsable = currentUserId != null && currentUserId.equals(responsable);

            if (!isAssignee && !isResponsable) {
                throw new AccessDeniedException(
                        "Accès refusé : vous n'êtes ni assigné à cette tâche, ni responsable du projet");
            }
        }

        task.setStatut(newStatus);
        task.setUpdatedAt(LocalDateTime.now());

        Task saved = taskRepository.save(task);
        return taskMapper.toDto(saved);
    }

    public void deleteTask(Long id, String currentUserId, boolean isAdmin) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(TACHE_NON_TROUVEE + id));

        boolean isProjectResponsable = task.getProject().getResponsableKeycloakId() != null
                && task.getProject().getResponsableKeycloakId().equals(currentUserId);

        if (!isAdmin && !isProjectResponsable) {
            throw new AccessDeniedException(
                    "Accès refusé : Seul le responsable du projet ou un Admin peut supprimer une tâche.");
        }
        taskRepository.deleteById(id);
    }
}
