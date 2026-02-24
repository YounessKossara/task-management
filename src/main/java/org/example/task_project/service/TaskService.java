package org.example.task_project.service;

import org.example.task_project.dto.TaskDto;
import org.example.task_project.entity.Project;
import org.example.task_project.entity.Task;
import org.example.task_project.enums.TaskStatus;
import org.example.task_project.exception.ResourceNotFoundException;
import org.example.task_project.mapper.TaskMapper;
import org.example.task_project.repository.ProjectRepository;
import org.example.task_project.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final TaskMapper taskMapper;

    public TaskService(TaskRepository taskRepository, ProjectRepository projectRepository, TaskMapper taskMapper) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.taskMapper = taskMapper;
    }

    public List<TaskDto> getTasksByProject(Long projectId, TaskStatus statut, String assigneeKeycloakId) {
        List<Task> tasks;

        if (statut != null && assigneeKeycloakId != null) {
            tasks = taskRepository.findByProjectIdAndStatutAndAssigneeKeycloakId(projectId, statut, assigneeKeycloakId);
        } else if (statut != null) {
            tasks = taskRepository.findByProjectIdAndStatut(projectId, statut);
        } else if (assigneeKeycloakId != null) {
            tasks = taskRepository.findByProjectIdAndAssigneeKeycloakId(projectId, assigneeKeycloakId);
        } else {
            tasks = taskRepository.findByProjectId(projectId);
        }

        return tasks.stream()
                .map(taskMapper::toDto)
                .collect(Collectors.toList());
    }

    public TaskDto createTask(Long projectId, TaskDto taskDto) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Projet non trouvé: " + projectId));

        Task task = taskMapper.toEntity(taskDto);
        task.setProject(project);
        task.setStatut(taskDto.getStatut() != null ? taskDto.getStatut() : TaskStatus.TODO);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());

        Task saved = taskRepository.save(task);
        return taskMapper.toDto(saved);
    }

    public TaskDto updateTask(Long id, TaskDto taskDto) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tâche non trouvée: " + id));

        task.setTitre(taskDto.getTitre());
        task.setDescription(taskDto.getDescription());
        task.setStatut(taskDto.getStatut());
        task.setPriorite(taskDto.getPriorite());
        task.setAssigneeKeycloakId(taskDto.getAssigneeKeycloakId());
        task.setUpdatedAt(LocalDateTime.now());

        Task saved = taskRepository.save(task);
        return taskMapper.toDto(saved);
    }

    public TaskDto updateTaskStatus(Long id, TaskStatus newStatus) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tâche non trouvée: " + id));

        task.setStatut(newStatus);
        task.setUpdatedAt(LocalDateTime.now());

        Task saved = taskRepository.save(task);
        return taskMapper.toDto(saved);
    }

    public void deleteTask(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new ResourceNotFoundException("Tâche non trouvée: " + id);
        }
        taskRepository.deleteById(id);
    }
}
