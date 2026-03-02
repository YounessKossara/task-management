package org.example.task_project.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.task_project.enums.TaskPriority;
import org.example.task_project.enums.TaskStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titre;
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus statut;

    @Enumerated(EnumType.STRING)
    private TaskPriority priorite;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    private String assigneeKeycloakId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
