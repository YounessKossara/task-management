package org.example.task_project.controller;

import org.example.task_project.dto.UserDto;
import org.example.task_project.service.FileStorageService;
import org.example.task_project.service.KeycloakUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

        @Mock
        private KeycloakUserService keycloakUserService;

        @Mock
        private FileStorageService fileStorageService;

        @InjectMocks
        private UserController userController;

        @Test
        void getAllUsers_shouldReturnList() {
                List<UserDto> users = Arrays.asList(
                                UserDto.builder().keycloakId("id1").nom("Nom1").role("ADMIN").build(),
                                UserDto.builder().keycloakId("id2").nom("Nom2").role("USER").build());
                when(keycloakUserService.getAllUsers()).thenReturn(users);

                ResponseEntity<List<UserDto>> response = userController.getAllUsers();

                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertEquals(2, response.getBody().size());
        }

        @Test
        void getUserById_shouldReturnUser() {
                UserDto dto = UserDto.builder().keycloakId("id1").nom("Ahmed").build();
                when(keycloakUserService.getUserById("id1")).thenReturn(dto);

                ResponseEntity<UserDto> response = userController.getUserById("id1");

                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertEquals("Ahmed", response.getBody().getNom());
        }

        @Test
        void createUser_shouldReturn201() {
                UserDto inputDto = UserDto.builder().nom("Nouveau").email("new@test.com").role("USER").build();
                UserDto outputDto = UserDto.builder().keycloakId("new-id").nom("Nouveau").email("new@test.com")
                                .role("USER").build();
                when(keycloakUserService.createUser(any(UserDto.class), eq("pass123"))).thenReturn(outputDto);

                ResponseEntity<UserDto> response = userController.createUser(inputDto, "pass123");

                assertEquals(HttpStatus.CREATED, response.getStatusCode());
                assertEquals("new-id", response.getBody().getKeycloakId());
        }
        @Test
        void updateUser_shouldReturn200() {
                UserDto dto = UserDto.builder().keycloakId("id1").nom("Modifié").build();
                when(keycloakUserService.updateUser(eq("id1"), any(UserDto.class))).thenReturn(dto);

                ResponseEntity<UserDto> response = userController.updateUser("id1", dto);

                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertEquals("Modifié", response.getBody().getNom());
        }

        @Test
        void deleteUser_shouldReturn204() {
                doNothing().when(keycloakUserService).deleteUser("id1");

                ResponseEntity<Void> response = userController.deleteUser("id1");

                assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
                verify(keycloakUserService, times(1)).deleteUser("id1");
        }

        @Test
        void uploadIdentityDoc_shouldReturn200() {
                MockMultipartFile file = new MockMultipartFile(
                                "file", "carte-id.pdf", "application/pdf", "contenu".getBytes());
                when(fileStorageService.uploadFile(eq("id1"), anyString(), any()))
                                .thenReturn("http://localhost:9000/doc.pdf");

                ResponseEntity<String> response = userController.uploadIdentityDoc("id1", file);

                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertEquals("http://localhost:9000/doc.pdf", response.getBody());
        }
}
