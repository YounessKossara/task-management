package org.example.task_project.service;

import org.example.task_project.dto.UserDto;
import org.example.task_project.entity.User;
import org.example.task_project.exception.KeycloakException;
import org.example.task_project.exception.ResourceNotFoundException;
import org.example.task_project.mapper.UserMapper;
import org.example.task_project.repository.UserRepository;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.ws.rs.core.Response;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
public class KeycloakUserService {

    private final Keycloak keycloak;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Value("${keycloak.realm}")
    private String realm;

    public KeycloakUserService(Keycloak keycloak, UserRepository userRepository, UserMapper userMapper) {
        this.keycloak = keycloak;
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    private UsersResource getUsersResource() {
        return keycloak.realm(realm).users();
    }

    @Transactional
    public List<UserDto> getAllUsers() {
        // 1. Fetch all users currently in Keycloak
        List<UserRepresentation> keycloakUsers = getUsersResource().list();
        List<String> keycloakIds = keycloakUsers.stream()
                .map(UserRepresentation::getId)
                .toList();

        // 2. Fetch all local users
        List<User> localUsers = userRepository.findAll();

        // 3. Find missing users and delete them locally
        List<User> usersToDelete = localUsers.stream()
                .filter(u -> !keycloakIds.contains(u.getKeycloakId()))
                .toList();

        if (!usersToDelete.isEmpty()) {
            userRepository.deleteAll(usersToDelete);
            localUsers.removeAll(usersToDelete);
            System.out.println("Synchronisation Keycloak: " + usersToDelete.size()
                    + " utilisateurs locaux supprimés car absents de Keycloak.");
        }

        // 4. Return the updated list
        return localUsers.stream()
                .map(userMapper::toDto)
                .toList();
    }

    public UserDto getUserById(String keycloakId) {
        User user = userRepository.findById(keycloakId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé: " + keycloakId));
        return userMapper.toDto(user);
    }

    @Transactional
    public UserDto createUser(UserDto userDto, String password) {
        // 1. Créer dans Keycloak
        UserRepresentation keycloakUser = new UserRepresentation();
        keycloakUser.setUsername(userDto.getEmail());
        keycloakUser.setEmail(userDto.getEmail());
        keycloakUser.setFirstName(userDto.getPrenom());
        keycloakUser.setLastName(userDto.getNom());
        keycloakUser.setEnabled(true);
        keycloakUser.setEmailVerified(true);

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        credential.setTemporary(false);
        keycloakUser.setCredentials(Collections.singletonList(credential));

        try (Response response = getUsersResource().create(keycloakUser)) {

            if (response.getStatus() != 201) {
                throw new KeycloakException("Erreur Keycloak: " + response.getStatusInfo().getReasonPhrase());
            }

            // 2. Récupérer l'ID Keycloak généré
            String keycloakId = response.getLocation().getPath()
                    .replaceAll(".*/([^/]+)$", "$1");

            // Assign role to user if specified, handle gracefully if admin-client lacks
            // permissions (403)
            if (userDto.getRole() != null) {
                try {
                    RoleRepresentation roleRep = keycloak.realm(realm).roles().get(userDto.getRole())
                            .toRepresentation();
                    getUsersResource().get(keycloakId).roles().realmLevel().add(Collections.singletonList(roleRep));
                } catch (Exception e) {
                    System.err.println("Avertissement: Impossible d'assigner le rôle '" + userDto.getRole()
                            + "' dans Keycloak. Erreur exacte: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            // 3. Sauvegarder en local
            try {
                User user = userMapper.toEntity(userDto);
                user.setKeycloakId(keycloakId);
                user.setCreatedAt(java.time.LocalDateTime.now());
                user.setUpdatedAt(java.time.LocalDateTime.now());
                userRepository.save(user);

                userDto.setKeycloakId(keycloakId);
                return userDto;
            } catch (Exception e) {
                // Si l'enregistrement en base de données échoue, on supprime l'utilisateur de
                // Keycloak pour éviter les incohérences
                try {
                    getUsersResource().get(keycloakId).remove();
                } catch (Exception deleteEx) {
                    System.err.println(
                            "Attention: Impossible de supprimer l'utilisateur Keycloak suite à une erreur locale.");
                }
                throw e; // Rethrow pour que l'API renvoie une erreur 500 propre et pas un "utilisteur
                         // créé" partiel
            }
        }
    }

    @Transactional
    public UserDto updateUser(String keycloakId, UserDto userDto) {
        User user = userRepository.findById(keycloakId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé: " + keycloakId));

        // 1. Mettre à jour dans Keycloak
        UserRepresentation keycloakUser = getUsersResource().get(keycloakId).toRepresentation();
        keycloakUser.setFirstName(userDto.getPrenom());
        keycloakUser.setLastName(userDto.getNom());
        keycloakUser.setEmail(userDto.getEmail());
        getUsersResource().get(keycloakId).update(keycloakUser);

        // Update role if changed
        if (userDto.getRole() != null && !userDto.getRole().equals(user.getRole())) {
            // Remove old role
            if (user.getRole() != null) {
                try {
                    RoleRepresentation oldRole = keycloak.realm(realm).roles().get(user.getRole()).toRepresentation();
                    getUsersResource().get(keycloakId).roles().realmLevel().remove(Collections.singletonList(oldRole));
                } catch (Exception e) {
                    System.err.println("Erreur suppression ancien rôle: " + e.getMessage());
                }
            }
            // Add new role
            try {
                RoleRepresentation newRole = keycloak.realm(realm).roles().get(userDto.getRole()).toRepresentation();
                getUsersResource().get(keycloakId).roles().realmLevel().add(Collections.singletonList(newRole));
            } catch (Exception e) {
                System.err.println("Erreur assignation nouveau rôle: " + e.getMessage());
            }
        }

        // 2. Mettre à jour en local
        user.setNom(userDto.getNom());
        user.setPrenom(userDto.getPrenom());
        user.setEmail(userDto.getEmail());
        user.setTelephone(userDto.getTelephone());
        user.setDateNaissance(userDto.getDateNaissance());
        user.setRole(userDto.getRole());
        user.setUpdatedAt(java.time.LocalDateTime.now());
        userRepository.save(user);

        return userMapper.toDto(user);
    }

    @Transactional
    public void deleteUser(String keycloakId) {
        if (!userRepository.existsById(keycloakId)) {
            throw new ResourceNotFoundException("Utilisateur non trouvé: " + keycloakId);
        }

        // 1. Supprimer en local d'abord (Transactional SQL)
        userRepository.deleteById(keycloakId);

        // 2. Supprimer de Keycloak
        try {
            getUsersResource().get(keycloakId).remove();
        } catch (jakarta.ws.rs.NotFoundException e) {
            System.out
                    .println("Utilisateur " + keycloakId + " non trouvé dans Keycloak. Suppression locale finalisée.");
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("404")) {
                System.out.println(
                        "Erreur 404 capturée pour l'utilisateur " + keycloakId + ". Suppression locale finalisée.");
            } else {
                // Si Keycloak échoue (autre que 404), on lève une exception pour rollback la
                // transaction SQL
                throw new KeycloakException(
                        "Impossible de supprimer l'utilisateur dans Keycloak: " + e.getMessage()
                                + ". Annulation locale.");
            }
        }
    }

    @Transactional
    public void updateIdentityDocUrl(String keycloakId, String url) {
        User user = userRepository.findById(keycloakId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé: " + keycloakId));
        user.setIdentityDocUrl(url);
        user.setUpdatedAt(java.time.LocalDateTime.now());
        userRepository.save(user);
    }
}
