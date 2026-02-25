package org.example.task_project.controller;

import org.example.task_project.dto.ProjectDto;
import org.example.task_project.service.ProjectService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectControllerTest {

    @Mock
    private ProjectService projectService;

    @InjectMocks
    private ProjectController projectController;

    @Test
    void getAllProjects_shouldReturn200() {
        List<ProjectDto> projects = Arrays.asList(
                ProjectDto.builder().id(1L).nom("Projet A").build(),
                ProjectDto.builder().id(2L).nom("Projet B").build());
        when(projectService.getAllProjects()).thenReturn(projects);

        ResponseEntity<List<ProjectDto>> response = projectController.getAllProjects();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        assertEquals("Projet A", response.getBody().get(0).getNom());
    }

    @Test
    void getProjectById_shouldReturnProject() {
        ProjectDto dto = ProjectDto.builder().id(1L).nom("Projet A").build();
        when(projectService.getProjectById(1L)).thenReturn(dto);

        ResponseEntity<ProjectDto> response = projectController.getProjectById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Projet A", response.getBody().getNom());
    }

    @Test
    void createProject_shouldReturn201() {
        ProjectDto inputDto = ProjectDto.builder().nom("Nouveau").build();
        ProjectDto outputDto = ProjectDto.builder().id(1L).nom("Nouveau").build();
        when(projectService.createProject(any(ProjectDto.class))).thenReturn(outputDto);

        ResponseEntity<ProjectDto> response = projectController.createProject(inputDto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(1L, response.getBody().getId());
    }

    @Test
    void updateProject_shouldReturn200() {
        ProjectDto dto = ProjectDto.builder().id(1L).nom("Modifié").build();
        when(projectService.updateProject(eq(1L), any(ProjectDto.class))).thenReturn(dto);

        ResponseEntity<ProjectDto> response = projectController.updateProject(1L, dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Modifié", response.getBody().getNom());
    }

    @Test
    void deleteProject_shouldReturn204() {
        doNothing().when(projectService).deleteProject(1L);

        ResponseEntity<Void> response = projectController.deleteProject(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(projectService, times(1)).deleteProject(1L);
    }
}
