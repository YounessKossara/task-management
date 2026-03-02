package org.example.task_project.dto;

import lombok.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectDto {

    private Long id;
    @NotBlank(message = "Le nom du projet est obligatoire")
    private String nom;
    private String description;
    @NotNull(message = "La date de début est obligatoire")
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String responsableKeycloakId;
}
