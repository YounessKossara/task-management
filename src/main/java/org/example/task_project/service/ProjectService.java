package org.example.task_project.service;

import org.example.task_project.dto.ProjectDto;
import org.example.task_project.entity.Project;
import org.example.task_project.exception.AccessDeniedException;
import org.example.task_project.exception.ResourceNotFoundException;
import org.example.task_project.mapper.ProjectMapper;
import org.example.task_project.repository.ProjectRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProjectService {

    private static final String PROJET_NON_TROUVE = "Projet non trouvé: ";

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;

    public ProjectService(ProjectRepository projectRepository, ProjectMapper projectMapper) {
        this.projectRepository = projectRepository;
        this.projectMapper = projectMapper;
    }

    public List<ProjectDto> getAllProjects(String keycloakId, boolean isAdmin, boolean isResponsable) {
        List<Project> projects;
        if (isAdmin) {
            projects = projectRepository.findAll();
        } else if (isResponsable) {
            projects = projectRepository.findByResponsableKeycloakId(keycloakId);
        } else {
            projects = projectRepository.findProjectsByAssigneeKeycloakId(keycloakId);
        }

        return projects.stream()
                .map(projectMapper::toDto)
                .toList();
    }

    public ProjectDto getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(PROJET_NON_TROUVE + id));
        return projectMapper.toDto(project);
    }

    public ProjectDto createProject(ProjectDto projectDto) {
        Project project = projectMapper.toEntity(projectDto);
        project.setCreatedAt(LocalDateTime.now());
        project.setUpdatedAt(LocalDateTime.now());
        Project saved = projectRepository.save(project);
        return projectMapper.toDto(saved);
    }

    public ProjectDto updateProject(Long id, ProjectDto projectDto, String keycloakId, boolean isAdmin) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(PROJET_NON_TROUVE + id));

        // Security check
        if (!isAdmin && !keycloakId.equals(project.getResponsableKeycloakId())) {
            throw new AccessDeniedException("Accès refusé : Vous n'êtes pas le responsable de ce projet.");
        }

        project.setNom(projectDto.getNom());
        project.setDescription(projectDto.getDescription());
        project.setDateDebut(projectDto.getDateDebut());
        project.setDateFin(projectDto.getDateFin());
        project.setResponsableKeycloakId(projectDto.getResponsableKeycloakId());
        project.setUpdatedAt(LocalDateTime.now());

        Project saved = projectRepository.save(project);
        return projectMapper.toDto(saved);
    }

    public void deleteProject(Long id) {
        if (!projectRepository.existsById(id)) {
            throw new ResourceNotFoundException(PROJET_NON_TROUVE + id);
        }
        projectRepository.deleteById(id);
    }
}
