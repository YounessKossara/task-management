package org.example.task_project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.task_project.enums.TaskPriority;
import org.example.task_project.enums.TaskStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskDto {

    private Long id;
    @NotBlank(message = "Le titre de la tâche est obligatoire")
    private String titre;
    private String description;
    @NotNull(message = "Le statut est obligatoire")
    private TaskStatus statut;
    private TaskPriority priorite;
    @NotNull(message = "Le projet est obligatoire")
    private Long projectId;
    private String assigneeKeycloakId;
}
