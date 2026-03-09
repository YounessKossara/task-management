package org.example.task_project.service;

import org.example.task_project.dto.TaskDto;
import org.example.task_project.entity.Project;
import org.example.task_project.entity.Task;
import org.example.task_project.enums.TaskPriority;
import org.example.task_project.enums.TaskStatus;
import org.example.task_project.exception.ResourceNotFoundException;
import org.example.task_project.mapper.TaskMapper;
import org.example.task_project.repository.ProjectRepository;
import org.example.task_project.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private TaskMapper taskMapper;

    @InjectMocks
    private TaskService taskService;

    @Test
    void getTasksByProject_noFilters_shouldReturnAll() {
        Task task1 = Task.builder().id(1L).titre("Tâche 1").statut(TaskStatus.TODO).build();
        Task task2 = Task.builder().id(2L).titre("Tâche 2").statut(TaskStatus.DONE).build();
        TaskDto dto1 = TaskDto.builder().id(1L).titre("Tâche 1").statut(TaskStatus.TODO).build();
        TaskDto dto2 = TaskDto.builder().id(2L).titre("Tâche 2").statut(TaskStatus.DONE).build();

        Project project = Project.builder().id(1L).responsableKeycloakId("admin-id").build();
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(taskRepository.findByProjectId(1L)).thenReturn(Arrays.asList(task1, task2));
        when(taskMapper.toDto(task1)).thenReturn(dto1);
        when(taskMapper.toDto(task2)).thenReturn(dto2);

        // Corrigé : 5 paramètres
        List<TaskDto> result = taskService.getTasksByProject(1L, null, null, "admin-id", true);

        assertEquals(2, result.size());
    }

    @Test
    void getTasksByProject_withStatusFilter_shouldFilter() {
        Task task = Task.builder().id(1L).titre("Tâche 1").statut(TaskStatus.TODO).build();
        TaskDto dto = TaskDto.builder().id(1L).titre("Tâche 1").statut(TaskStatus.TODO).build();

        Project project = Project.builder().id(1L).responsableKeycloakId("admin-id").build();
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(taskRepository.findByProjectIdAndStatut(1L, TaskStatus.TODO)).thenReturn(List.of(task));
        when(taskMapper.toDto(task)).thenReturn(dto);

        // Corrigé : 5 paramètres
        List<TaskDto> result = taskService.getTasksByProject(1L, TaskStatus.TODO, null, "admin-id", true);

        assertEquals(1, result.size());
        assertEquals(TaskStatus.TODO, result.get(0).getStatut());
    }

    @Test
    void createTask_shouldSetDefaultStatus() {
        Project project = Project.builder().id(1L).nom("Projet A").build();
        TaskDto inputDto = TaskDto.builder().titre("Nouvelle tâche").priorite(TaskPriority.HAUTE).build();
        Task entity = Task.builder().titre("Nouvelle tâche").priorite(TaskPriority.HAUTE).build();
        Task saved = Task.builder().id(1L).titre("Nouvelle tâche").statut(TaskStatus.TODO).priorite(TaskPriority.HAUTE)
                .build();
        TaskDto outputDto = TaskDto.builder().id(1L).titre("Nouvelle tâche").statut(TaskStatus.TODO)
                .priorite(TaskPriority.HAUTE).build();

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(taskMapper.toEntity(inputDto)).thenReturn(entity);
        when(taskRepository.save(entity)).thenReturn(saved);
        when(taskMapper.toDto(saved)).thenReturn(outputDto);

        TaskDto result = taskService.createTask(1L, inputDto, "admin-id", true);

        assertEquals("Nouvelle tâche", result.getTitre());
        assertEquals(TaskStatus.TODO, result.getStatut());
    }

    @Test
    void createTask_shouldThrowException_whenProjectNotFound() {
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> taskService.createTask(999L, new TaskDto(), "admin-id", true));
    }

    @Test
    void updateTaskStatus_shouldChangeStatus() {
        Task task = Task.builder().id(1L).titre("Tâche").statut(TaskStatus.TODO).build();
        Task updated = Task.builder().id(1L).titre("Tâche").statut(TaskStatus.DONE).build();
        TaskDto dto = TaskDto.builder().id(1L).titre("Tâche").statut(TaskStatus.DONE).build();

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(updated);
        when(taskMapper.toDto(updated)).thenReturn(dto);

        TaskDto result = taskService.updateTaskStatus(1L, TaskStatus.DONE, "admin-id", true);
        assertEquals(TaskStatus.DONE, result.getStatut());
    }

    @Test
    void deleteTask_shouldThrowException_whenNotFound() {
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> taskService.deleteTask(999L, "admin-id", true));
    }

    @Test
    void updateTaskStatus_shouldDeny_whenNotAuthorized() {
        Project project = Project.builder().id(1L).responsableKeycloakId("resp-id").build();
        Task task = Task.builder().id(1L).titre("Tâche").statut(TaskStatus.TODO).project(project)
                .assigneeKeycloakId("other-user").build();
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        assertThrows(RuntimeException.class,
                () -> taskService.updateTaskStatus(1L, TaskStatus.DONE, "random-user", false));
    }

    @Test
    void updateTaskStatus_shouldAllow_whenAssignee() {
        Project project = Project.builder().id(1L).responsableKeycloakId("resp-id").build();
        Task task = Task.builder().id(1L).titre("Tâche").statut(TaskStatus.TODO).project(project)
                .assigneeKeycloakId("assignee-id").build();
        Task updated = Task.builder().id(1L).titre("Tâche").statut(TaskStatus.DONE).build();
        TaskDto dto = TaskDto.builder().id(1L).statut(TaskStatus.DONE).build();

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(updated);
        when(taskMapper.toDto(updated)).thenReturn(dto);

        TaskDto result = taskService.updateTaskStatus(1L, TaskStatus.DONE, "assignee-id", false);
        assertEquals(TaskStatus.DONE, result.getStatut());
    }

    @Test
    void deleteTask_shouldDelete_whenExists() {
        Project project = Project.builder().id(1L).responsableKeycloakId("admin-id").build();
        Task task = Task.builder().id(1L).project(project).build();
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        taskService.deleteTask(1L, "admin-id", true);
        verify(taskRepository, times(1)).deleteById(1L);
    }

    @Test
    void updateTask_shouldUpdateAndReturn() {
        Project project = Project.builder().id(1L).responsableKeycloakId("resp-id").build();
        Task existing = Task.builder().id(1L).titre("Ancienne").project(project).build();
        TaskDto updateDto = TaskDto.builder().titre("Nouvelle").description("Desc").priorite(TaskPriority.HAUTE)
                .assigneeKeycloakId("user-id").build();
        Task saved = Task.builder().id(1L).titre("Nouvelle").build();
        TaskDto outputDto = TaskDto.builder().id(1L).titre("Nouvelle").build();

        when(taskRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(taskRepository.save(existing)).thenReturn(saved);
        when(taskMapper.toDto(saved)).thenReturn(outputDto);

        TaskDto result = taskService.updateTask(1L, updateDto, "admin-id", true);
        assertEquals("Nouvelle", result.getTitre());
    }

    @Test
    void getTasksByProject_notAdminNotResponsable_shouldLimitToOwnTasks() {
        Project project = Project.builder().id(1L).responsableKeycloakId("resp-id").build();
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        Task task = Task.builder().id(1L).titre("Tâche").assigneeKeycloakId("user-id").build();
        when(taskRepository.findByProjectIdAndAssigneeKeycloakId(1L, "user-id")).thenReturn(List.of(task));

        taskService.getTasksByProject(1L, null, null, "user-id", false);
        verify(taskRepository).findByProjectIdAndAssigneeKeycloakId(1L, "user-id");
    }

    @Test
    void getTasksByProject_withStatusAndAssigneeFilters() {
        Project project = Project.builder().id(1L).responsableKeycloakId("admin-id").build();
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        taskService.getTasksByProject(1L, TaskStatus.DONE, "user-id", "admin-id", true);
        verify(taskRepository).findByProjectIdAndStatutAndAssigneeKeycloakId(1L, TaskStatus.DONE, "user-id");
    }

    @Test
    void getTasksByProject_withAssigneeFilterOnly() {
        Project project = Project.builder().id(1L).responsableKeycloakId("admin-id").build();
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        taskService.getTasksByProject(1L, null, "user-id", "admin-id", true);
        verify(taskRepository).findByProjectIdAndAssigneeKeycloakId(1L, "user-id");
    }

    @Test
    void createTask_shouldThrowAccessDenied_whenNotAuthorized() {
        Project project = Project.builder().id(1L).responsableKeycloakId("resp-id").build();
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        TaskDto dto = new TaskDto();
        assertThrows(org.example.task_project.exception.AccessDeniedException.class,
                () -> taskService.createTask(1L, dto, "other-user", false));
    }

    @Test
    void createTask_shouldThrowIllegalArgument_whenAssigningToResponsable() {
        Project project = Project.builder().id(1L).responsableKeycloakId("resp-id").build();
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        TaskDto dto = new TaskDto();
        dto.setAssigneeKeycloakId("resp-id");
        assertThrows(IllegalArgumentException.class,
                () -> taskService.createTask(1L, dto, "resp-id", false));
    }

    @Test
    void updateTask_shouldThrowAccessDenied_whenNotAuthorized() {
        Project project = Project.builder().id(1L).responsableKeycloakId("resp-id").build();
        Task existing = Task.builder().id(1L).project(project).build();
        when(taskRepository.findById(1L)).thenReturn(Optional.of(existing));

        TaskDto dto = new TaskDto();
        assertThrows(org.example.task_project.exception.AccessDeniedException.class,
                () -> taskService.updateTask(1L, dto, "other-user", false));
    }

    @Test
    void updateTask_shouldThrowIllegalArgument_whenAssigningToResponsable() {
        Project project = Project.builder().id(1L).responsableKeycloakId("resp-id").build();
        Task existing = Task.builder().id(1L).project(project).build();
        when(taskRepository.findById(1L)).thenReturn(Optional.of(existing));

        TaskDto dto = new TaskDto();
        dto.setAssigneeKeycloakId("resp-id");
        assertThrows(IllegalArgumentException.class,
                () -> taskService.updateTask(1L, dto, "resp-id", false));
    }

    @Test
    void updateTaskStatus_shouldAllow_whenResponsable() {
        Project project = Project.builder().id(1L).responsableKeycloakId("resp-id").build();
        Task task = Task.builder().id(1L).project(project).assigneeKeycloakId("user-id").build();
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        Task updated = Task.builder().id(1L).project(project).statut(TaskStatus.DONE).build();
        when(taskRepository.save(task)).thenReturn(updated);

        taskService.updateTaskStatus(1L, TaskStatus.DONE, "resp-id", false);
        verify(taskRepository).save(task);
    }

    @Test
    void deleteTask_shouldThrowAccessDenied_whenNotAuthorized() {
        Project project = Project.builder().id(1L).responsableKeycloakId("resp-id").build();
        Task task = Task.builder().id(1L).project(project).build();
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        assertThrows(org.example.task_project.exception.AccessDeniedException.class,
                () -> taskService.deleteTask(1L, "other-user", false));
    }
}