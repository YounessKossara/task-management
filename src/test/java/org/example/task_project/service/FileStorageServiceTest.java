package org.example.task_project.service;

import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.RemoveObjectArgs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileStorageServiceTest {

    @Mock
    private MinioClient minioClient;

    @InjectMocks
    private FileStorageService fileStorageService;

    @BeforeEach
    void setUp() {
        // Injecte la valeur de bucketName manuellement (car @Value ne marche pas sans
        // Spring)
        ReflectionTestUtils.setField(fileStorageService, "bucketName", "identity-documents");
    }

    @Test
    void uploadFile_shouldCallMinioClient() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file", "carte-id.pdf", "application/pdf", "contenu pdf".getBytes());

        // putObject retourne un ObjectWriteResponse, pas void
        when(minioClient.putObject(any())).thenReturn(mock(ObjectWriteResponse.class));

        // When — l'upload va marcher, mais getFileUrl va échouer (mock non configuré)
        try {
            fileStorageService.uploadFile("user1", "carte-id.pdf", file);
        } catch (RuntimeException e) {
            // Attendu car getPresignedObjectUrl n'est pas mocké
        }

        // Then
        verify(minioClient, times(1)).putObject(any());
    }

    @Test
    void deleteFile_shouldCallMinioRemove() throws Exception {
        // Given — removeObject est void, doNothing est correct ici
        doNothing().when(minioClient).removeObject(any(RemoveObjectArgs.class));

        // When
        fileStorageService.deleteFile("user1/carte-id.pdf");

        // Then
        verify(minioClient, times(1)).removeObject(any(RemoveObjectArgs.class));
    }
}
