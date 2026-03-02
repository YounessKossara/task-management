package org.example.task_project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    private String keycloakId;

    @Column(nullable = false)
    private String nom;
    @Column(nullable = false)
    private String prenom;
    private LocalDate dateNaissance;
    @Column(nullable = false, unique = true)
    private String email;
    private String telephone;
    @Column(length = 1024)
    private String identityDocUrl;
    private String role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
