package org.example.task_project.service;

import org.example.task_project.dto.ProjectDto;
import org.example.task_project.entity.Project;
import org.example.task_project.exception.ResourceNotFoundException;
import org.example.task_project.mapper.ProjectMapper;
import org.example.task_project.repository.ProjectRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;

    public ProjectService(ProjectRepository projectRepository, ProjectMapper projectMapper) {
        this.projectRepository = projectRepository;
        this.projectMapper = projectMapper;
    }

    public List<ProjectDto> getAllProjects() {
        return projectRepository.findAll()
                .stream()
                .map(projectMapper::toDto)
                .toList();
    }

    public ProjectDto getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Projet non trouvé: " + id));
        return projectMapper.toDto(project);
    }

    public ProjectDto createProject(ProjectDto projectDto) {
        Project project = projectMapper.toEntity(projectDto);
        project.setCreatedAt(LocalDateTime.now());
        project.setUpdatedAt(LocalDateTime.now());
        Project saved = projectRepository.save(project);
        return projectMapper.toDto(saved);
    }

    public ProjectDto updateProject(Long id, ProjectDto projectDto) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Projet non trouvé: " + id));

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
            throw new ResourceNotFoundException("Projet non trouvé: " + id);
        }
        projectRepository.deleteById(id);
    }
}
