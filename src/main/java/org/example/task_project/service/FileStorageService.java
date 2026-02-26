package org.example.task_project.service;

import io.minio.*;
import io.minio.http.Method;
import org.example.task_project.exception.FileStorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

    private final MinioClient storageClient;

    @Value("${rustfs.bucket-name}")
    private String bucketName;

    public FileStorageService(MinioClient storageClient) {
        this.storageClient = storageClient;
    }

    public String uploadFile(String folder, String fileName, MultipartFile file) {
        try {
            String objectName = folder + "/" + fileName;

            storageClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());

            return getFileUrl(objectName);
        } catch (Exception e) {
            throw new FileStorageException("Erreur lors de l'upload du fichier: " + e.getMessage(), e);
        }
    }

    public String getFileUrl(String objectName) {
        try {
            return storageClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(60 * 60 * 24) // URL valide 24h
                            .build());
        } catch (Exception e) {
            throw new FileStorageException("Erreur lors de la génération de l'URL: " + e.getMessage(), e);
        }
    }

    public void deleteFile(String objectName) {
        try {
            storageClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build());
        } catch (Exception e) {
            throw new FileStorageException("Erreur lors de la suppression du fichier: " + e.getMessage(), e);
        }
    }
}
