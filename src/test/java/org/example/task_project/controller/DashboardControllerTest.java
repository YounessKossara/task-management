package org.example.task_project.controller;

import org.example.task_project.dto.DashboardDto;
import org.example.task_project.service.DashboardService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

        @Mock
        private DashboardService dashboardService;

        @InjectMocks
        private DashboardController dashboardController;

        @Test
        void getDashboard_shouldReturnList() {
                List<DashboardDto> dashboard = Arrays.asList(
                                DashboardDto.builder()
                                                .projectId(1L)
                                                .projectNom("Projet A")
                                                .totalTasks(10)
                                                .tasksByStatus(Map.of("TODO", 3L, "IN_PROGRESS", 2L, "DONE", 5L))
                                                .build(),
                                DashboardDto.builder()
                                                .projectId(2L)
                                                .projectNom("Projet B")
                                                .totalTasks(5)
                                                .tasksByStatus(Map.of("TODO", 3L, "DONE", 2L))
                                                .build());
                when(dashboardService.getDashboard()).thenReturn(dashboard);

                ResponseEntity<List<DashboardDto>> response = dashboardController.getDashboard();

                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertEquals(2, response.getBody().size());
                assertEquals("Projet A", response.getBody().get(0).getProjectNom());
                assertEquals(10, response.getBody().get(0).getTotalTasks());
        }

        @Test
        void getDashboard_emptyList() {
                when(dashboardService.getDashboard()).thenReturn(List.of());

                ResponseEntity<List<DashboardDto>> response = dashboardController.getDashboard();

                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertEquals(0, response.getBody().size());
        }
}
