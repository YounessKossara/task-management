package org.example.task_project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "projects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nom;
    private String description;
    @Column(nullable = false)
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String responsableKeycloakId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
