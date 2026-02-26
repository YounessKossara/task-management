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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.ws.rs.core.Response;
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

    public List<UserDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toDto)
                .toList();
    }

    public UserDto getUserById(String keycloakId) {
        User user = userRepository.findById(keycloakId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé: " + keycloakId));
        return userMapper.toDto(user);
    }

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

            // 3. Sauvegarder en local
            User user = userMapper.toEntity(userDto);
            user.setKeycloakId(keycloakId);
            user.setCreatedAt(java.time.LocalDateTime.now());
            user.setUpdatedAt(java.time.LocalDateTime.now());
            userRepository.save(user);

            userDto.setKeycloakId(keycloakId);
            return userDto;
        }
    }

    public UserDto updateUser(String keycloakId, UserDto userDto) {
        User user = userRepository.findById(keycloakId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé: " + keycloakId));

        // 1. Mettre à jour dans Keycloak
        UserRepresentation keycloakUser = getUsersResource().get(keycloakId).toRepresentation();
        keycloakUser.setFirstName(userDto.getPrenom());
        keycloakUser.setLastName(userDto.getNom());
        keycloakUser.setEmail(userDto.getEmail());
        getUsersResource().get(keycloakId).update(keycloakUser);

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

    public void deleteUser(String keycloakId) {
        if (!userRepository.existsById(keycloakId)) {
            throw new ResourceNotFoundException("Utilisateur non trouvé: " + keycloakId);
        }

        // 1. Supprimer de Keycloak
        getUsersResource().get(keycloakId).remove();

        // 2. Supprimer en local
        userRepository.deleteById(keycloakId);
    }

    public void updateIdentityDocUrl(String keycloakId, String url) {
        User user = userRepository.findById(keycloakId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé: " + keycloakId));
        user.setIdentityDocUrl(url);
        user.setUpdatedAt(java.time.LocalDateTime.now());
        userRepository.save(user);
    }
}
