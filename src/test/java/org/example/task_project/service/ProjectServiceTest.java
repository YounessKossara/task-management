package org.example.task_project.service;

import org.example.task_project.dto.ProjectDto;
import org.example.task_project.entity.Project;
import org.example.task_project.exception.ResourceNotFoundException;
import org.example.task_project.mapper.ProjectMapper;
import org.example.task_project.repository.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMapper projectMapper;

    @InjectMocks
    private ProjectService projectService;

    @Test
    void getAllProjects_shouldReturnList() {
        // Given
        Project p1 = Project.builder().id(1L).nom("Projet A").build();
        Project p2 = Project.builder().id(2L).nom("Projet B").build();
        ProjectDto dto1 = ProjectDto.builder().id(1L).nom("Projet A").build();
        ProjectDto dto2 = ProjectDto.builder().id(2L).nom("Projet B").build();

        when(projectRepository.findAll()).thenReturn(Arrays.asList(p1, p2));
        when(projectMapper.toDto(p1)).thenReturn(dto1);
        when(projectMapper.toDto(p2)).thenReturn(dto2);

        // When
        List<ProjectDto> result = projectService.getAllProjects("user-id", true, false);

        // Then
        assertEquals(2, result.size());
        assertEquals("Projet A", result.get(0).getNom());
    }

    @Test
    void getProjectById_shouldReturnProject_whenExists() {
        // Given
        Project project = Project.builder().id(1L).nom("Projet A").build();
        ProjectDto dto = ProjectDto.builder().id(1L).nom("Projet A").build();

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMapper.toDto(project)).thenReturn(dto);

        // When
        ProjectDto result = projectService.getProjectById(1L);

        // Then
        assertEquals("Projet A", result.getNom());
    }

    @Test
    void getProjectById_shouldThrowException_whenNotFound() {
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            projectService.getProjectById(999L);
        });
    }

    @Test
    void createProject_shouldSaveAndReturnDto() {
        // Given
        ProjectDto inputDto = ProjectDto.builder().nom("Nouveau").description("Desc").build();
        Project entity = Project.builder().nom("Nouveau").description("Desc").build();
        Project saved = Project.builder().id(1L).nom("Nouveau").description("Desc").build();
        ProjectDto outputDto = ProjectDto.builder().id(1L).nom("Nouveau").description("Desc").build();

        when(projectMapper.toEntity(inputDto)).thenReturn(entity);
        when(projectRepository.save(entity)).thenReturn(saved);
        when(projectMapper.toDto(saved)).thenReturn(outputDto);

        // When
        ProjectDto result = projectService.createProject(inputDto);

        // Then
        assertEquals(1L, result.getId());
        assertEquals("Nouveau", result.getNom());
        verify(projectRepository, times(1)).save(entity);
    }

    @Test
    void deleteProject_shouldThrowException_whenNotFound() {
        when(projectRepository.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> {
            projectService.deleteProject(999L);
        });
    }

    @Test
    void deleteProject_shouldDelete_whenExists() {
        when(projectRepository.existsById(1L)).thenReturn(true);

        projectService.deleteProject(1L);

        verify(projectRepository, times(1)).deleteById(1L);
    }

    @Test
    void updateProject_shouldUpdateAndReturnDto() {
        // Given
        Project existing = Project.builder().id(1L).responsableKeycloakId("resp-id").nom("Ancien")
                .description("Ancienne desc").build();
        ProjectDto updateDto = ProjectDto.builder().nom("Nouveau").description("Nouvelle desc")
                .dateDebut(LocalDate.of(2026, 3, 1)).dateFin(LocalDate.of(2026, 6, 30))
                .responsableKeycloakId("resp-id").build();
        Project saved = Project.builder().id(1L).nom("Nouveau").description("Nouvelle desc").build();
        ProjectDto outputDto = ProjectDto.builder().id(1L).nom("Nouveau").description("Nouvelle desc").build();

        when(projectRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(projectRepository.save(existing)).thenReturn(saved);
        when(projectMapper.toDto(saved)).thenReturn(outputDto);

        // When
        ProjectDto result = projectService.updateProject(1L, updateDto, "resp-id", false);

        // Then
        assertEquals("Nouveau", result.getNom());
        verify(projectRepository, times(1)).save(existing);
    }

    @Test
    void updateProject_shouldThrowException_whenNotFound() {
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            projectService.updateProject(999L, new ProjectDto(), "any", true);
        });
    }
}
