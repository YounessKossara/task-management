package org.example.task_project.controller;

import org.example.task_project.dto.TaskDto;
import org.example.task_project.enums.TaskStatus;
import org.example.task_project.service.TaskService;
import jakarta.validation.Valid;
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
    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONSABLE', 'USER')")
    public ResponseEntity<List<TaskDto>> getTasksByProject(
            @PathVariable Long projectId,
            @RequestParam(required = false) TaskStatus statut,
            @RequestParam(required = false) String assigneeKeycloakId,
            Authentication authentication) {
        String currentUserId = authentication != null ? authentication.getName() : null;
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN"));
        boolean isResponsable = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("ROLE_RESPONSABLE"));

        return ResponseEntity
                .ok(taskService.getTasksByProject(projectId, statut, assigneeKeycloakId, currentUserId, isAdmin,
                        isResponsable));
    }

    @PostMapping("/projects/{projectId}/tasks")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONSABLE')")
    public ResponseEntity<TaskDto> createTask(@PathVariable Long projectId,
            @Valid @RequestBody TaskDto taskDto, Authentication authentication) {
        String currentUserId = authentication != null ? authentication.getName() : null;
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN"));
        TaskDto created = taskService.createTask(projectId, taskDto, currentUserId, isAdmin);
        return ResponseEntity.status(201).body(created);
    }

    @PutMapping("/tasks/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONSABLE')")
    public ResponseEntity<TaskDto> updateTask(@PathVariable Long id,
            @Valid @RequestBody TaskDto taskDto, Authentication authentication) {
        String currentUserId = authentication != null ? authentication.getName() : null;
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN"));
        return ResponseEntity.ok(taskService.updateTask(id, taskDto, currentUserId, isAdmin));
    }

    @PatchMapping("/tasks/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONSABLE', 'USER')")
    public ResponseEntity<TaskDto> updateTaskStatus(@PathVariable Long id,
            @RequestParam TaskStatus statut,
            Authentication authentication) {
        String currentUserId = authentication != null ? authentication.getName() : null;
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN"));
        return ResponseEntity.ok(taskService.updateTaskStatus(id, statut, currentUserId, isAdmin));
    }

    @DeleteMapping("/tasks/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONSABLE')")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id, Authentication authentication) {
        String currentUserId = authentication != null ? authentication.getName() : null;
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN"));
        taskService.deleteTask(id, currentUserId, isAdmin);
        return ResponseEntity.noContent().build();
    }
}
