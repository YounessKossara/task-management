package org.example.task_project.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RustFsConfig {

    @Value("${rustfs.url}")
    private String url;

    @Value("${rustfs.access-key}")
    private String accessKey;

    @Value("${rustfs.secret-key}")
    private String secretKey;

    @Bean
    public MinioClient storageClient() {
        // Le SDK MinIO est compatible S3 — il communique avec RustFS via le protocole
        // S3
        return MinioClient.builder()
                .endpoint(url)
                .credentials(accessKey, secretKey)
                .build();
    }
}
