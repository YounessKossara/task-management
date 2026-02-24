package org.example.task_project.dto;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardDto {

    private Long projectId;
    private String projectNom;
    private long totalTasks;
    private Map<String, Long> tasksByStatus;
}
