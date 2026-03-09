package org.example.task_project.service;

import org.example.task_project.dto.UserDto;
import org.example.task_project.exception.ResourceNotFoundException;
import org.example.task_project.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KeycloakUserServiceTest {

    @Mock
    private Keycloak keycloak;

    @Mock
    private RealmResource realmResource;

    @Mock
    private UsersResource usersResource;

    @Mock
    private UserResource userResource;

    @Mock
    private UserMapper userMapper;

    @Mock
    private org.keycloak.admin.client.resource.RoleMappingResource roleMappingResource;

    @Mock
    private org.keycloak.admin.client.resource.RoleScopeResource roleScopeResource;

    @Mock
    private org.keycloak.admin.client.resource.RolesResource rolesResource;

    @Mock
    private org.keycloak.admin.client.resource.RoleResource roleResource;

    @InjectMocks
    private KeycloakUserService keycloakUserService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(keycloakUserService, "realm", "task-realm");
    }

    @Test
    void getAllUsers_shouldReturnListOfUserDto() {
        // Given
        UserRepresentation userRep1 = new UserRepresentation();
        userRep1.setId("id1");
        userRep1.setLastName("Nom1");

        UserRepresentation userRep2 = new UserRepresentation();
        userRep2.setId("id2");
        userRep2.setLastName("Nom2");

        UserDto dto1 = new UserDto();
        dto1.setKeycloakId("id1");
        dto1.setNom("Nom1");

        UserDto dto2 = new UserDto();
        dto2.setKeycloakId("id2");
        dto2.setNom("Nom2");

        when(keycloak.realm("task-realm")).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.list()).thenReturn(Arrays.asList(userRep1, userRep2));

        when(userMapper.toDto(userRep1)).thenReturn(dto1);
        when(userMapper.toDto(userRep2)).thenReturn(dto2);

        // When
        List<UserDto> result = keycloakUserService.getAllUsers();

        // Then
        assertEquals(2, result.size());
        assertEquals("Nom1", result.get(0).getNom());
        assertEquals("Nom2", result.get(1).getNom());
        verify(usersResource, times(1)).list();
    }

    @Test
    void getUserById_shouldReturnUser_whenExists() {
        // Given
        UserRepresentation userRep = new UserRepresentation();
        userRep.setId("id1");
        userRep.setLastName("Ahmed");

        UserDto dto = new UserDto();
        dto.setKeycloakId("id1");
        dto.setNom("Ahmed");

        when(keycloak.realm("task-realm")).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.get("id1")).thenReturn(userResource);
        when(userResource.toRepresentation()).thenReturn(userRep);
        when(userMapper.toDto(userRep)).thenReturn(dto);

        // When
        UserDto result = keycloakUserService.getUserById("id1");

        // Then
        assertEquals("Ahmed", result.getNom());
    }

    @Test
    void getUserById_shouldThrowException_whenNotFound() {
        // Given
        when(keycloak.realm("task-realm")).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.get("id999")).thenReturn(userResource);
        when(userResource.toRepresentation()).thenThrow(new NotFoundException());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            keycloakUserService.getUserById("id999");
        });
    }

    @Test
    void deleteUser_shouldRemoveUser_whenExists() {
        // Given
        when(keycloak.realm("task-realm")).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.get("id1")).thenReturn(userResource);
        doNothing().when(userResource).remove();

        // When
        assertDoesNotThrow(() -> keycloakUserService.deleteUser("id1"));

        // Then
        verify(userResource, times(1)).remove();
    }

    @Test
    void deleteUser_shouldThrowException_whenNotFound() {
        // Given
        when(keycloak.realm("task-realm")).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.get("id999")).thenReturn(userResource);
        doThrow(new NotFoundException()).when(userResource).remove();

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            keycloakUserService.deleteUser("id999");
        });
    }

    @Test
    void updateIdentityDocUrl_shouldThrowException_whenNotFound() {
        // Given
        when(keycloak.realm("task-realm")).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.get("id999")).thenReturn(userResource);
        when(userResource.toRepresentation()).thenThrow(new NotFoundException());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            keycloakUserService.updateIdentityDocUrl("id999", "http://rustfs/doc.pdf");
        });
    }

    @Test
    void createUser_shouldReturnGeneratedUser_whenSuccessful() throws Exception {
        // Given
        UserDto inputDto = new UserDto();
        inputDto.setEmail("test@ex.com");
        inputDto.setNom("Test");
        inputDto.setPrenom("User");
        inputDto.setTelephone("1234");
        inputDto.setDateNaissance(LocalDate.of(1990, 1, 1));
        inputDto.setRole("USER");

        Response mockResponse = mock(Response.class);
        when(mockResponse.getStatus()).thenReturn(201);

        Response.StatusType statusInfo = mock(Response.StatusType.class);
        lenient().when(mockResponse.getStatusInfo()).thenReturn(statusInfo);

        lenient().when(mockResponse.getLocation())
                .thenReturn(new URI("http://localhost/auth/admin/realms/task-realm/users/new-id"));

        when(keycloak.realm("task-realm")).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.create(any(UserRepresentation.class))).thenReturn(mockResponse);

        // Mocking the generated user retrieval (part of createUser logic)
        UserResource newUserResource = mock(UserResource.class);
        UserRepresentation createdRep = new UserRepresentation();
        createdRep.setId("new-id");

        when(usersResource.get("new-id")).thenReturn(newUserResource);
        when(newUserResource.toRepresentation()).thenReturn(createdRep);

        UserDto returnedDto = new UserDto();
        returnedDto.setKeycloakId("new-id");
        when(userMapper.toDto(createdRep)).thenReturn(returnedDto);

        // Mock role assignment
        RoleRepresentation roleRep = new RoleRepresentation();
        roleRep.setName("USER");
        when(realmResource.roles()).thenReturn(rolesResource);
        when(rolesResource.get("USER")).thenReturn(roleResource);
        when(roleResource.toRepresentation()).thenReturn(roleRep);
        when(newUserResource.roles()).thenReturn(roleMappingResource);
        when(roleMappingResource.realmLevel()).thenReturn(roleScopeResource);

        // When
        UserDto result = keycloakUserService.createUser(inputDto, "password");

        // Then
        assertEquals("new-id", result.getKeycloakId());
        verify(roleScopeResource, times(1)).add(anyList());
    }

    @Test
    void updateUser_shouldUpdateAndReturnUser() {
        // Given
        UserDto updateDto = new UserDto();
        updateDto.setEmail("updated@ex.com");
        updateDto.setNom("Test2");
        updateDto.setPrenom("User2");
        updateDto.setRole("ADMIN");

        UserRepresentation existingRep = new UserRepresentation();
        existingRep.setId("id1");
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put("role", Arrays.asList("USER"));
        existingRep.setAttributes(attributes);

        when(keycloak.realm("task-realm")).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.get("id1")).thenReturn(userResource);
        when(userResource.toRepresentation()).thenReturn(existingRep);

        // Role updating mocks
        RoleRepresentation oldRoleRep = new RoleRepresentation();
        RoleRepresentation newRoleRep = new RoleRepresentation();
        when(realmResource.roles()).thenReturn(rolesResource);
        when(rolesResource.get("USER")).thenReturn(roleResource);
        when(rolesResource.get("ADMIN")).thenReturn(roleResource);
        when(roleResource.toRepresentation()).thenReturn(oldRoleRep, newRoleRep);

        when(userResource.roles()).thenReturn(roleMappingResource);
        when(roleMappingResource.realmLevel()).thenReturn(roleScopeResource);

        // Expected mapping
        UserDto mappedDto = new UserDto();
        mappedDto.setNom("Test2");
        when(userMapper.toDto(any())).thenReturn(mappedDto);

        // When
        UserDto result = keycloakUserService.updateUser("id1", updateDto);

        // Then
        assertEquals("Test2", result.getNom());
        verify(userResource, times(1)).update(existingRep);
    }

    @Test
    void updateIdentityDocUrl_shouldUpdateUrl() {
        // Given
        UserRepresentation existingRep = new UserRepresentation();
        existingRep.setId("id1");

        when(keycloak.realm("task-realm")).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.get("id1")).thenReturn(userResource);
        when(userResource.toRepresentation()).thenReturn(existingRep);

        // When
        keycloakUserService.updateIdentityDocUrl("id1", "http://docs/id.pdf");

        // Then
        assertEquals("http://docs/id.pdf", existingRep.getAttributes().get("identityDocUrl").get(0));
        verify(userResource, times(1)).update(existingRep);
    }

    @Test
    void getAllUsers_shouldThrowKeycloakException_onFailure() {
        when(keycloak.realm("task-realm")).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.list()).thenThrow(new RuntimeException("Connection error"));

        assertThrows(org.example.task_project.exception.KeycloakException.class, () -> {
            keycloakUserService.getAllUsers();
        });
    }

    @Test
    void getUserById_shouldThrowKeycloakException_whenGenericError() {
        when(keycloak.realm("task-realm")).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.get("id999")).thenThrow(new RuntimeException("Server Erreur"));

        assertThrows(org.example.task_project.exception.KeycloakException.class, () -> {
            keycloakUserService.getUserById("id999");
        });
    }

    @Test
    void createUser_shouldThrowException_whenStatusNot201() {
        UserDto inputDto = new UserDto();
        inputDto.setEmail("test@ex.com");

        Response mockResponse = mock(Response.class);
        when(mockResponse.getStatus()).thenReturn(400);
        Response.StatusType statusInfo = mock(Response.StatusType.class);
        when(statusInfo.getReasonPhrase()).thenReturn("Bad Request");
        lenient().when(mockResponse.getStatusInfo()).thenReturn(statusInfo);

        when(keycloak.realm("task-realm")).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.create(any(UserRepresentation.class))).thenReturn(mockResponse);

        assertThrows(org.example.task_project.exception.KeycloakException.class, () -> {
            keycloakUserService.createUser(inputDto, "password");
        });
    }

    @Test
    void updateUser_shouldHandleNullAttributesAndHandleExceptions() {
        UserDto updateDto = new UserDto();
        updateDto.setRole("ADMIN");

        UserRepresentation existingRep = new UserRepresentation();
        existingRep.setId("id1");
        existingRep.setAttributes(null);

        when(keycloak.realm("task-realm")).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.get("id1")).thenReturn(userResource);
        when(userResource.toRepresentation()).thenReturn(existingRep);

        when(realmResource.roles()).thenReturn(rolesResource);
        when(rolesResource.get("ADMIN")).thenThrow(new RuntimeException("Role Admin fetch failed"));

        UserDto mappedDto = new UserDto();
        when(userMapper.toDto(any())).thenReturn(mappedDto);

        UserDto result = keycloakUserService.updateUser("id1", updateDto);

        assertNotNull(result);
        verify(userResource, times(1)).update(existingRep);
    }

    @Test
    void deleteUser_shouldThrowKeycloakException_whenGenericError() {
        when(keycloak.realm("task-realm")).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.get("id1")).thenReturn(userResource);
        doThrow(new RuntimeException("Delete error")).when(userResource).remove();

        assertThrows(org.example.task_project.exception.KeycloakException.class, () -> {
            keycloakUserService.deleteUser("id1");
        });
    }

    @Test
    void updateIdentityDocUrl_shouldHandleNullAttributes() {
        UserRepresentation existingRep = new UserRepresentation();
        existingRep.setId("id1");
        existingRep.setAttributes(null);

        when(keycloak.realm("task-realm")).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.get("id1")).thenReturn(userResource);
        when(userResource.toRepresentation()).thenReturn(existingRep);

        keycloakUserService.updateIdentityDocUrl("id1", "http://docs/id.pdf");

        assertEquals("http://docs/id.pdf", existingRep.getAttributes().get("identityDocUrl").get(0));
        verify(userResource, times(1)).update(existingRep);
    }
}
