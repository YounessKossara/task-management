package org.example.task_project.service;

import org.example.task_project.dto.DashboardDto;
import org.example.task_project.entity.Project;
import org.example.task_project.entity.Task;
import org.example.task_project.repository.ProjectRepository;
import org.example.task_project.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;

    public DashboardService(ProjectRepository projectRepository, TaskRepository taskRepository) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
    }

    public List<DashboardDto> getDashboard() {
        List<Project> projects = projectRepository.findAll();

        return projects.stream().map(project -> {
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
    }
}
