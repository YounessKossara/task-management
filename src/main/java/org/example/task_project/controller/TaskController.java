package org.example.task_project.controller;

import org.example.task_project.dto.TaskDto;
import org.example.task_project.enums.TaskStatus;
import org.example.task_project.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/projects/{projectId}/tasks")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<TaskDto>> getTasksByProject(
            @PathVariable Long projectId,
            @RequestParam(required = false) TaskStatus statut,
            @RequestParam(required = false) String assigneeKeycloakId) {
        return ResponseEntity.ok(taskService.getTasksByProject(projectId, statut, assigneeKeycloakId));
    }

    @PostMapping("/projects/{projectId}/tasks")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TaskDto> createTask(@PathVariable Long projectId,
            @RequestBody TaskDto taskDto) {
        TaskDto created = taskService.createTask(projectId, taskDto);
        return ResponseEntity.status(201).body(created);
    }

    @PutMapping("/tasks/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TaskDto> updateTask(@PathVariable Long id,
            @RequestBody TaskDto taskDto) {
        return ResponseEntity.ok(taskService.updateTask(id, taskDto));
    }

    @PatchMapping("/tasks/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<TaskDto> updateTaskStatus(@PathVariable Long id,
            @RequestParam TaskStatus statut,
            Authentication authentication) {
        String currentUserId = authentication != null ? authentication.getName() : null;
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ADMIN"));
        return ResponseEntity.ok(taskService.updateTaskStatus(id, statut, currentUserId, isAdmin));
    }

    @DeleteMapping("/tasks/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}
