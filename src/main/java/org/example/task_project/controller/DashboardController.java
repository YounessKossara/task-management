package org.example.task_project.controller;

import org.example.task_project.dto.DashboardDto;
import org.example.task_project.entity.Project;
import org.example.task_project.entity.Task;
import org.example.task_project.repository.ProjectRepository;
import org.example.task_project.repository.TaskRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;

    public DashboardController(ProjectRepository projectRepository, TaskRepository taskRepository) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<DashboardDto>> getDashboard() {
        List<Project> projects = projectRepository.findAll();

        List<DashboardDto> dashboard = projects.stream().map(project -> {
            List<Task> tasks = taskRepository.findByProjectId(project.getId());

            Map<String, Long> tasksByStatus = tasks.stream()
                    .collect(Collectors.groupingBy(
                            task -> task.getStatut().name(),
                            Collectors.counting()));

            return DashboardDto.builder()
                    .projectId(project.getId())
                    .projectNom(project.getNom())
                    .totalTasks(tasks.size())
                    .tasksByStatus(tasksByStatus)
                    .build();
        }).collect(Collectors.toList());

        return ResponseEntity.ok(dashboard);
    }
}
