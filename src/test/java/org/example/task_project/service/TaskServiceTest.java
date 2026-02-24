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
        // Given
        Task task1 = Task.builder().id(1L).titre("Tâche 1").statut(TaskStatus.TODO).build();
        Task task2 = Task.builder().id(2L).titre("Tâche 2").statut(TaskStatus.DONE).build();
        TaskDto dto1 = TaskDto.builder().id(1L).titre("Tâche 1").statut(TaskStatus.TODO).build();
        TaskDto dto2 = TaskDto.builder().id(2L).titre("Tâche 2").statut(TaskStatus.DONE).build();

        when(taskRepository.findByProjectId(1L)).thenReturn(Arrays.asList(task1, task2));
        when(taskMapper.toDto(task1)).thenReturn(dto1);
        when(taskMapper.toDto(task2)).thenReturn(dto2);

        // When
        List<TaskDto> result = taskService.getTasksByProject(1L, null, null);

        // Then
        assertEquals(2, result.size());
    }

    @Test
    void getTasksByProject_withStatusFilter_shouldFilter() {
        // Given
        Task task = Task.builder().id(1L).titre("Tâche 1").statut(TaskStatus.TODO).build();
        TaskDto dto = TaskDto.builder().id(1L).titre("Tâche 1").statut(TaskStatus.TODO).build();

        when(taskRepository.findByProjectIdAndStatut(1L, TaskStatus.TODO)).thenReturn(List.of(task));
        when(taskMapper.toDto(task)).thenReturn(dto);

        // When
        List<TaskDto> result = taskService.getTasksByProject(1L, TaskStatus.TODO, null);

        // Then
        assertEquals(1, result.size());
        assertEquals(TaskStatus.TODO, result.get(0).getStatut());
    }

    @Test
    void createTask_shouldSetDefaultStatus() {
        // Given
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

        // When
        TaskDto result = taskService.createTask(1L, inputDto);

        // Then
        assertEquals("Nouvelle tâche", result.getTitre());
        assertEquals(TaskStatus.TODO, result.getStatut());
    }

    @Test
    void createTask_shouldThrowException_whenProjectNotFound() {
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            taskService.createTask(999L, new TaskDto());
        });
    }

    @Test
    void updateTaskStatus_shouldChangeStatus() {
        // Given
        Task task = Task.builder().id(1L).titre("Tâche").statut(TaskStatus.TODO).build();
        Task updated = Task.builder().id(1L).titre("Tâche").statut(TaskStatus.DONE).build();
        TaskDto dto = TaskDto.builder().id(1L).titre("Tâche").statut(TaskStatus.DONE).build();

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(updated);
        when(taskMapper.toDto(updated)).thenReturn(dto);

        // When
        TaskDto result = taskService.updateTaskStatus(1L, TaskStatus.DONE);

        // Then
        assertEquals(TaskStatus.DONE, result.getStatut());
    }

    @Test
    void deleteTask_shouldThrowException_whenNotFound() {
        when(taskRepository.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> {
            taskService.deleteTask(999L);
        });
    }
}
