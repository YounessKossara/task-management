package org.example.task_project.dto;

import lombok.*;
import org.example.task_project.enums.TaskPriority;
import org.example.task_project.enums.TaskStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskDto {

    private Long id;
    private String titre;
    private String description;
    private TaskStatus statut;
    private TaskPriority priorite;
    private Long projectId;
    private String assigneeKeycloakId;
}
