package org.example.task_project.controller;

import org.example.task_project.dto.TaskDto;
import org.example.task_project.enums.TaskPriority;
import org.example.task_project.enums.TaskStatus;
import org.example.task_project.service.TaskService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskControllerTest {

        @Mock
        private TaskService taskService;

        @Mock
        private Authentication authentication;

        @InjectMocks
        private TaskController taskController;

        @Test
        void getTasksByProject_shouldReturnTasks() {
                List<TaskDto> tasks = Arrays.asList(
                        TaskDto.builder().id(1L).titre("Tâche 1").statut(TaskStatus.TODO).build(),
                        TaskDto.builder().id(2L).titre("Tâche 2").statut(TaskStatus.DONE).build());

                // Corrigé : 5 paramètres
                when(taskService.getTasksByProject(1L, null, null, "admin-id", true)).thenReturn(tasks);
                when(authentication.getName()).thenReturn("admin-id");
                doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))).when(authentication).getAuthorities();

                ResponseEntity<List<TaskDto>> response = taskController.getTasksByProject(1L, null, null, authentication);

                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertEquals(2, response.getBody().size());
        }

        @Test
        void getTasksByProject_withStatusFilter() {
                List<TaskDto> tasks = List.of(TaskDto.builder().id(1L).titre("Tâche 1").statut(TaskStatus.TODO).build());

                // Corrigé : 5 paramètres
                when(taskService.getTasksByProject(1L, TaskStatus.TODO, null, "admin-id", true)).thenReturn(tasks);
                when(authentication.getName()).thenReturn("admin-id");
                doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))).when(authentication).getAuthorities();

                ResponseEntity<List<TaskDto>> response = taskController.getTasksByProject(1L, TaskStatus.TODO, null, authentication);

                assertEquals(1, response.getBody().size());
                assertEquals(TaskStatus.TODO, response.getBody().get(0).getStatut());
        }

        @Test
        void createTask_shouldReturn201() {
                TaskDto inputDto = TaskDto.builder().titre("Nouvelle").priorite(TaskPriority.HAUTE).build();
                TaskDto outputDto = TaskDto.builder().id(1L).titre("Nouvelle").statut(TaskStatus.TODO).build();

                when(taskService.createTask(eq(1L), any(TaskDto.class), anyString(), anyBoolean())).thenReturn(outputDto);
                when(authentication.getName()).thenReturn("admin-id");
                doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))).when(authentication).getAuthorities();

                ResponseEntity<TaskDto> response = taskController.createTask(1L, inputDto, authentication);

                assertEquals(HttpStatus.CREATED, response.getStatusCode());
                assertEquals("Nouvelle", response.getBody().getTitre());
        }

        @Test
        void updateTask_shouldReturn200() {
                TaskDto dto = TaskDto.builder().id(1L).titre("Modifiée").build();
                when(taskService.updateTask(eq(1L), any(TaskDto.class), anyString(), anyBoolean())).thenReturn(dto);

                when(authentication.getName()).thenReturn("admin-id");
                doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))).when(authentication).getAuthorities();

                ResponseEntity<TaskDto> response = taskController.updateTask(1L, dto, authentication);

                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertEquals("Modifiée", response.getBody().getTitre());
        }

        @Test
        void updateTaskStatus_shouldReturn200() {
                TaskDto dto = TaskDto.builder().id(1L).statut(TaskStatus.DONE).build();
                when(taskService.updateTaskStatus(eq(1L), eq(TaskStatus.DONE), anyString(), anyBoolean())).thenReturn(dto);

                when(authentication.getName()).thenReturn("admin-id");
                doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))).when(authentication).getAuthorities();

                ResponseEntity<TaskDto> response = taskController.updateTaskStatus(1L, TaskStatus.DONE, authentication);

                assertEquals(TaskStatus.DONE, response.getBody().getStatut());
        }

        @Test
        void deleteTask_shouldReturn204() {
                doNothing().when(taskService).deleteTask(eq(1L), anyString(), anyBoolean());
                when(authentication.getName()).thenReturn("admin-id");
                doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))).when(authentication).getAuthorities();

                ResponseEntity<Void> response = taskController.deleteTask(1L, authentication);

                assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
                verify(taskService, times(1)).deleteTask(1L, "admin-id", true);
        }
}