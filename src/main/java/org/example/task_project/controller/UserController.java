package org.example.task_project.controller;

import org.example.task_project.dto.UserDto;
import org.example.task_project.service.FileStorageService;
import org.example.task_project.service.KeycloakUserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final KeycloakUserService keycloakUserService;
    private final FileStorageService fileStorageService;

    public UserController(KeycloakUserService keycloakUserService, FileStorageService fileStorageService) {
        this.keycloakUserService = keycloakUserService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONSABLE')")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(keycloakUserService.getAllUsers());
    }

    @GetMapping("/{keycloakId}")
    @PreAuthorize("hasRole('ADMIN') or #keycloakId == authentication.name")
    public ResponseEntity<UserDto> getUserById(@PathVariable String keycloakId) {
        return ResponseEntity.ok(keycloakUserService.getUserById(keycloakId));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserDto userDto,
            @RequestParam String password) {
        UserDto created = keycloakUserService.createUser(userDto, password);
        return ResponseEntity.status(201).body(created);
    }

    @PutMapping("/{keycloakId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> updateUser(@PathVariable String keycloakId,
            @Valid @RequestBody UserDto userDto) {
        return ResponseEntity.ok(keycloakUserService.updateUser(keycloakId, userDto));
    }

    @DeleteMapping("/{keycloakId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable String keycloakId) {
        keycloakUserService.deleteUser(keycloakId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{keycloakId}/identity-doc", consumes = "multipart/form-data")
    @PreAuthorize("hasRole('ADMIN') or #keycloakId == authentication.name")
    public ResponseEntity<String> uploadIdentityDoc(@PathVariable String keycloakId,
            @RequestParam("file") MultipartFile file) {
        String url = fileStorageService.uploadFile(keycloakId, file.getOriginalFilename(), file);
        // Sauvegarder l'URL dans PostgreSQL
        keycloakUserService.updateIdentityDocUrl(keycloakId, url);
        return ResponseEntity.ok(url);
    }
}
