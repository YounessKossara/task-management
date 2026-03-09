package org.example.task_project.service;

import org.example.task_project.dto.UserDto;
import org.example.task_project.exception.KeycloakException;
import org.example.task_project.exception.ResourceNotFoundException;
import org.example.task_project.mapper.UserMapper;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.ws.rs.core.Response;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class KeycloakUserService {

    private static final Logger log = LoggerFactory.getLogger(KeycloakUserService.class);

    private final Keycloak keycloak;
    private final UserMapper userMapper;

    @Value("${keycloak.realm}")
    private String realm;

    public KeycloakUserService(Keycloak keycloak, UserMapper userMapper) {
        this.keycloak = keycloak;
        this.userMapper = userMapper;
    }

    private UsersResource getUsersResource() {
        return keycloak.realm(realm).users();
    }

    public List<UserDto> getAllUsers() {
        try {
            List<UserRepresentation> keycloakUsers = getUsersResource().list();
            return keycloakUsers.stream()
                    .map(userMapper::toDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new KeycloakException("Failed to fetch users from Keycloak: " + e.getMessage());
        }
    }

    public UserDto getUserById(String keycloakId) {
        try {
            UserRepresentation keycloakUser = getUsersResource().get(keycloakId).toRepresentation();
            return userMapper.toDto(keycloakUser);
        } catch (jakarta.ws.rs.NotFoundException e) {
            throw new ResourceNotFoundException("Utilisateur non trouvé dans Keycloak: " + keycloakId);
        } catch (Exception e) {
            throw new KeycloakException("Failed to fetch user from Keycloak: " + e.getMessage());
        }
    }

    public UserDto createUser(UserDto userDto, String password) {
        UserRepresentation keycloakUser = new UserRepresentation();
        keycloakUser.setUsername(userDto.getEmail());
        keycloakUser.setEmail(userDto.getEmail());
        keycloakUser.setFirstName(userDto.getPrenom());
        keycloakUser.setLastName(userDto.getNom());
        keycloakUser.setEnabled(true);
        keycloakUser.setEmailVerified(true);

        // Custom Attributes
        Map<String, List<String>> attributes = new HashMap<>();
        if (userDto.getTelephone() != null)
            attributes.put("telephone", Collections.singletonList(userDto.getTelephone()));
        if (userDto.getDateNaissance() != null)
            attributes.put("dateNaissance", Collections.singletonList(userDto.getDateNaissance().toString()));
        if (userDto.getRole() != null)
            attributes.put("role", Collections.singletonList(userDto.getRole()));
        keycloakUser.setAttributes(attributes);

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        credential.setTemporary(false);
        keycloakUser.setCredentials(Collections.singletonList(credential));

        try (Response response = getUsersResource().create(keycloakUser)) {
            if (response.getStatus() != 201) {
                throw new KeycloakException(
                        "Erreur Keycloak lors de la création: " + response.getStatusInfo().getReasonPhrase());
            }

            String keycloakId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");

            if (userDto.getRole() != null) {
                try {
                    RoleRepresentation roleRep = keycloak.realm(realm).roles().get(userDto.getRole())
                            .toRepresentation();
                    getUsersResource().get(keycloakId).roles().realmLevel().add(Collections.singletonList(roleRep));
                } catch (Exception e) {
                    log.warn("Avertissement: Impossible d'assigner le rôle '{}'.", userDto.getRole());
                }
            }

            return getUserById(keycloakId);
        }
    }

    public UserDto updateUser(String keycloakId, UserDto userDto) {
        try {
            UserRepresentation keycloakUser = getUsersResource().get(keycloakId).toRepresentation();

            keycloakUser.setFirstName(userDto.getPrenom());
            keycloakUser.setLastName(userDto.getNom());
            keycloakUser.setEmail(userDto.getEmail());

            Map<String, List<String>> attributes = keycloakUser.getAttributes();
            if (attributes == null)
                attributes = new HashMap<>();

            if (userDto.getTelephone() != null)
                attributes.put("telephone", Collections.singletonList(userDto.getTelephone()));
            if (userDto.getDateNaissance() != null)
                attributes.put("dateNaissance", Collections.singletonList(userDto.getDateNaissance().toString()));

            String oldRole = null;
            if (attributes.containsKey("role") && !attributes.get("role").isEmpty()) {
                oldRole = attributes.get("role").get(0);
            }

            if (userDto.getRole() != null)
                attributes.put("role", Collections.singletonList(userDto.getRole()));

            keycloakUser.setAttributes(attributes);
            getUsersResource().get(keycloakId).update(keycloakUser);

            // Update role if changed
            if (userDto.getRole() != null && !userDto.getRole().equals(oldRole)) {
                if (oldRole != null) {
                    try {
                        RoleRepresentation oldRoleRep = keycloak.realm(realm).roles().get(oldRole).toRepresentation();
                        getUsersResource().get(keycloakId).roles().realmLevel()
                                .remove(Collections.singletonList(oldRoleRep));
                    } catch (Exception e) {
                        log.error("Erreur suppression ancien rôle: {}", e.getMessage());
                    }
                }
                try {
                    RoleRepresentation newRoleRep = keycloak.realm(realm).roles().get(userDto.getRole())
                            .toRepresentation();
                    getUsersResource().get(keycloakId).roles().realmLevel().add(Collections.singletonList(newRoleRep));
                } catch (Exception e) {
                    log.error("Erreur assignation nouveau rôle: {}", e.getMessage());
                }
            }

            return getUserById(keycloakId);
        } catch (jakarta.ws.rs.NotFoundException e) {
            throw new ResourceNotFoundException("Utilisateur non trouvé: " + keycloakId);
        }
    }

    public void deleteUser(String keycloakId) {
        try {
            getUsersResource().get(keycloakId).remove();
        } catch (jakarta.ws.rs.NotFoundException e) {
            throw new ResourceNotFoundException("Utilisateur non trouvé dans Keycloak: " + keycloakId);
        } catch (Exception e) {
            throw new KeycloakException("Impossible de supprimer l'utilisateur dans Keycloak: " + e.getMessage());
        }
    }

    public void updateIdentityDocUrl(String keycloakId, String url) {
        try {
            UserRepresentation keycloakUser = getUsersResource().get(keycloakId).toRepresentation();

            Map<String, List<String>> attributes = keycloakUser.getAttributes();
            if (attributes == null)
                attributes = new HashMap<>();

            attributes.put("identityDocUrl", Collections.singletonList(url));
            keycloakUser.setAttributes(attributes);

            getUsersResource().get(keycloakId).update(keycloakUser);
        } catch (jakarta.ws.rs.NotFoundException e) {
            throw new ResourceNotFoundException("Utilisateur non trouvé: " + keycloakId);
        }
    }
}
