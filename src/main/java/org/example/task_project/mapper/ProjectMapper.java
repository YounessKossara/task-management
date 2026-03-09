package org.example.task_project.mapper;

import org.example.task_project.dto.ProjectDto;
import org.example.task_project.entity.Project;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProjectMapper {

    ProjectDto toDto(Project project);

    Project toEntity(ProjectDto dto);
}
