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

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskControllerTest {

        @Mock
        private TaskService taskService;

        @InjectMocks
        private TaskController taskController;

        @Test
        void getTasksByProject_shouldReturnTasks() {
                List<TaskDto> tasks = Arrays.asList(
                                TaskDto.builder().id(1L).titre("Tâche 1").statut(TaskStatus.TODO).build(),
                                TaskDto.builder().id(2L).titre("Tâche 2").statut(TaskStatus.DONE).build());
                when(taskService.getTasksByProject(1L, null, null)).thenReturn(tasks);

                ResponseEntity<List<TaskDto>> response = taskController.getTasksByProject(1L, null, null);

                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertEquals(2, response.getBody().size());
        }

        @Test
        void getTasksByProject_withStatusFilter() {
                List<TaskDto> tasks = List.of(
                                TaskDto.builder().id(1L).titre("Tâche 1").statut(TaskStatus.TODO).build());
                when(taskService.getTasksByProject(1L, TaskStatus.TODO, null)).thenReturn(tasks);

                ResponseEntity<List<TaskDto>> response = taskController.getTasksByProject(1L, TaskStatus.TODO, null);

                assertEquals(1, response.getBody().size());
                assertEquals(TaskStatus.TODO, response.getBody().get(0).getStatut());
        }

        @Test
        void createTask_shouldReturn201() {
                TaskDto inputDto = TaskDto.builder().titre("Nouvelle").priorite(TaskPriority.HAUTE).build();
                TaskDto outputDto = TaskDto.builder().id(1L).titre("Nouvelle").statut(TaskStatus.TODO).build();
                when(taskService.createTask(eq(1L), any(TaskDto.class))).thenReturn(outputDto);

                ResponseEntity<TaskDto> response = taskController.createTask(1L, inputDto);

                assertEquals(HttpStatus.CREATED, response.getStatusCode());
                assertEquals("Nouvelle", response.getBody().getTitre());
        }

        @Test
        void updateTask_shouldReturn200() {
                TaskDto dto = TaskDto.builder().id(1L).titre("Modifiée").build();
                when(taskService.updateTask(eq(1L), any(TaskDto.class))).thenReturn(dto);

                ResponseEntity<TaskDto> response = taskController.updateTask(1L, dto);

                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertEquals("Modifiée", response.getBody().getTitre());
        }

        @Test
        void updateTaskStatus_shouldReturn200() {
                TaskDto dto = TaskDto.builder().id(1L).statut(TaskStatus.DONE).build();
                when(taskService.updateTaskStatus(eq(1L), eq(TaskStatus.DONE), any(), anyBoolean())).thenReturn(dto);

                ResponseEntity<TaskDto> response = taskController.updateTaskStatus(1L, TaskStatus.DONE, null);

                assertEquals(TaskStatus.DONE, response.getBody().getStatut());
        }

        @Test
        void deleteTask_shouldReturn204() {
                doNothing().when(taskService).deleteTask(1L);

                ResponseEntity<Void> response = taskController.deleteTask(1L);

                assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
                verify(taskService, times(1)).deleteTask(1L);
        }
}
