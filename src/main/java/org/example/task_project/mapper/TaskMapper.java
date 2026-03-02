package org.example.task_project.mapper;

import org.example.task_project.dto.TaskDto;
import org.example.task_project.entity.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper
public interface TaskMapper {

    @Mapping(target = "projectId", expression = "java(task.getProject() != null ? task.getProject().getId() : null)")
    TaskDto toDto(Task task);

    @Mapping(target = "project", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Task toEntity(TaskDto dto);
}
