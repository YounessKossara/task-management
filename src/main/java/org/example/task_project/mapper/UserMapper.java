package org.example.task_project.mapper;

import org.example.task_project.dto.UserDto;
import org.example.task_project.entity.User;
import org.mapstruct.Mapper;

@Mapper
public interface UserMapper {

    UserDto toDto(User user);

    User toEntity(UserDto dto);
}
