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
    private MinioClient storageClient;

    @InjectMocks
    private FileStorageService fileStorageService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(fileStorageService, "bucketName", "identity-documents");
    }

    @Test
    void uploadFile_shouldReturnUrl() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file", "carte-id.pdf", "application/pdf", "contenu pdf".getBytes());

        when(storageClient.putObject(any())).thenReturn(mock(ObjectWriteResponse.class));
        when(storageClient.getPresignedObjectUrl(any())).thenReturn("http://rustfs/signed-url");

        // When
        String url = fileStorageService.uploadFile("user1", "carte-id.pdf", file);

        // Then
        assertEquals("http://rustfs/signed-url", url);
        verify(storageClient, times(1)).putObject(any());
    }

    @Test
    void uploadFile_shouldThrowException_onError() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "carte-id.pdf", "application/pdf", "contenu pdf".getBytes());

        when(storageClient.putObject(any())).thenThrow(new RuntimeException("Connection refused"));

        assertThrows(RuntimeException.class, () -> {
            fileStorageService.uploadFile("user1", "carte-id.pdf", file);
        });
    }

    @Test
    void getFileUrl_shouldReturnPresignedUrl() throws Exception {
        when(storageClient.getPresignedObjectUrl(any())).thenReturn("http://rustfs/presigned");

        String url = fileStorageService.getFileUrl("user1/carte-id.pdf");

        assertEquals("http://rustfs/presigned", url);
    }

    @Test
    void getFileUrl_shouldThrowException_onError() throws Exception {
        when(storageClient.getPresignedObjectUrl(any())).thenThrow(new RuntimeException("Error"));

        assertThrows(RuntimeException.class, () -> {
            fileStorageService.getFileUrl("user1/carte-id.pdf");
        });
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

    @Test
    void deleteFile_shouldThrowException_onError() throws Exception {
        doThrow(new RuntimeException("Error")).when(storageClient).removeObject(any(RemoveObjectArgs.class));

        assertThrows(RuntimeException.class, () -> {
            fileStorageService.deleteFile("user1/carte-id.pdf");
        });
    }
}
