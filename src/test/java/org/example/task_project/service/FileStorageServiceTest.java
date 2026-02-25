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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileStorageServiceTest {

    @Mock
    private MinioClient storageClient;

    @InjectMocks
    private FileStorageService fileStorageService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(fileStorageService, "bucketName", "identity-documents");
    }

    @Test
    void uploadFile_shouldCallStorageClient() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file", "carte-id.pdf", "application/pdf", "contenu pdf".getBytes());

        when(storageClient.putObject(any())).thenReturn(mock(ObjectWriteResponse.class));

        // When
        try {
            fileStorageService.uploadFile("user1", "carte-id.pdf", file);
        } catch (RuntimeException e) {
            // Attendu car getPresignedObjectUrl n'est pas mocké
        }

        // Then
        verify(storageClient, times(1)).putObject(any());
    }

    @Test
    void deleteFile_shouldCallStorageRemove() throws Exception {
        // Given
        doNothing().when(storageClient).removeObject(any(RemoveObjectArgs.class));

        // When
        fileStorageService.deleteFile("user1/carte-id.pdf");

        // Then
        verify(storageClient, times(1)).removeObject(any(RemoveObjectArgs.class));
    }
}
