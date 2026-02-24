package org.example.task_project.service;

import org.example.task_project.dto.UserDto;
import org.example.task_project.entity.User;
import org.example.task_project.exception.ResourceNotFoundException;
import org.example.task_project.mapper.UserMapper;
import org.example.task_project.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KeycloakUserServiceTest {

    @Mock
    private Keycloak keycloak;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private KeycloakUserService keycloakUserService;

    @Test
    void getAllUsers_shouldReturnListOfUserDto() {
        // Given
        User user1 = User.builder().keycloakId("id1").nom("Nom1").prenom("Prenom1").email("test1@test.com")
                .role("ADMIN").build();
        User user2 = User.builder().keycloakId("id2").nom("Nom2").prenom("Prenom2").email("test2@test.com").role("USER")
                .build();
        UserDto dto1 = UserDto.builder().keycloakId("id1").nom("Nom1").prenom("Prenom1").email("test1@test.com")
                .role("ADMIN").build();
        UserDto dto2 = UserDto.builder().keycloakId("id2").nom("Nom2").prenom("Prenom2").email("test2@test.com")
                .role("USER").build();

        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));
        when(userMapper.toDto(user1)).thenReturn(dto1);
        when(userMapper.toDto(user2)).thenReturn(dto2);

        // When
        List<UserDto> result = keycloakUserService.getAllUsers();

        // Then
        assertEquals(2, result.size());
        assertEquals("Nom1", result.get(0).getNom());
        assertEquals("Nom2", result.get(1).getNom());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void getUserById_shouldReturnUser_whenExists() {
        // Given
        User user = User.builder().keycloakId("id1").nom("Ahmed").email("ahmed@test.com").role("ADMIN").build();
        UserDto dto = UserDto.builder().keycloakId("id1").nom("Ahmed").email("ahmed@test.com").role("ADMIN").build();

        when(userRepository.findById("id1")).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(dto);

        // When
        UserDto result = keycloakUserService.getUserById("id1");

        // Then
        assertEquals("Ahmed", result.getNom());
        assertEquals("ahmed@test.com", result.getEmail());
    }

    @Test
    void getUserById_shouldThrowException_whenNotFound() {
        // Given
        when(userRepository.findById("id999")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            keycloakUserService.getUserById("id999");
        });
    }

    @Test
    void deleteUser_shouldThrowException_whenNotFound() {
        // Given
        when(userRepository.existsById("id999")).thenReturn(false);

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            keycloakUserService.deleteUser("id999");
        });
    }
}
