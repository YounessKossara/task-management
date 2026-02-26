package org.example.task_project.service;

import org.example.task_project.dto.DashboardDto;
import org.example.task_project.entity.Project;
import org.example.task_project.entity.Task;
import org.example.task_project.enums.TaskStatus;
import org.example.task_project.repository.ProjectRepository;
import org.example.task_project.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    void getDashboard_shouldReturnAllProjectsWithTaskCounts() {
        // Given
        Project project1 = Project.builder().id(1L).nom("Projet A").build();
        Project project2 = Project.builder().id(2L).nom("Projet B").build();

        Task task1 = Task.builder().id(1L).statut(TaskStatus.TODO).build();
        Task task2 = Task.builder().id(2L).statut(TaskStatus.TODO).build();
        Task task3 = Task.builder().id(3L).statut(TaskStatus.DONE).build();

        when(projectRepository.findAll()).thenReturn(Arrays.asList(project1, project2));
        when(taskRepository.findByProjectId(1L)).thenReturn(Arrays.asList(task1, task2, task3));
        when(taskRepository.findByProjectId(2L)).thenReturn(Collections.emptyList());

        // When
        List<DashboardDto> result = dashboardService.getDashboard();

        // Then
        assertEquals(2, result.size());

        DashboardDto dashboard1 = result.get(0);
        assertEquals("Projet A", dashboard1.getProjectNom());
        assertEquals(3, dashboard1.getTotalTasks());
        assertEquals(2L, dashboard1.getTasksByStatus().get("TODO"));
        assertEquals(1L, dashboard1.getTasksByStatus().get("DONE"));

        DashboardDto dashboard2 = result.get(1);
        assertEquals("Projet B", dashboard2.getProjectNom());
        assertEquals(0, dashboard2.getTotalTasks());
    }

    @Test
    void getDashboard_noProjects_shouldReturnEmptyList() {
        // Given
        when(projectRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<DashboardDto> result = dashboardService.getDashboard();

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void getDashboard_projectWithAllStatuses() {
        // Given
        Project project = Project.builder().id(1L).nom("Projet C").build();

        Task t1 = Task.builder().id(1L).statut(TaskStatus.TODO).build();
        Task t2 = Task.builder().id(2L).statut(TaskStatus.IN_PROGRESS).build();
        Task t3 = Task.builder().id(3L).statut(TaskStatus.DONE).build();

        when(projectRepository.findAll()).thenReturn(List.of(project));
        when(taskRepository.findByProjectId(1L)).thenReturn(Arrays.asList(t1, t2, t3));

        // When
        List<DashboardDto> result = dashboardService.getDashboard();

        // Then
        assertEquals(1, result.size());
        assertEquals(3, result.get(0).getTotalTasks());
        assertEquals(1L, result.get(0).getTasksByStatus().get("TODO"));
        assertEquals(1L, result.get(0).getTasksByStatus().get("IN_PROGRESS"));
        assertEquals(1L, result.get(0).getTasksByStatus().get("DONE"));
    }
}
