package com.computerrock.attachmentmanager.spring.config;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.computerrock.attachmentmanager.spring.config.properties.AzureStorageBlobProperties;
import com.computerrock.attachmentmanager.service.impl.AzureStorageFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Optional;

@Profile("!test")
@Configuration
@ConditionalOnProperty(value = "uploader.enabled", matchIfMissing = true)
@RequiredArgsConstructor
public class AzureStorageBlobConfig {

    private final AzureStorageBlobProperties azureProperties;

    @Bean
    public BlobServiceClient blobServiceClient() {
        return new BlobServiceClientBuilder()
                .connectionString(azureProperties.getConnectionString())
                .buildClient();
    }

    @Bean
    public BlobContainerClient blobContainerClient(BlobServiceClient blobServiceClient) {

        final String containerName = azureProperties.getContainerName();

        return Optional.of(blobServiceClient)
                .map(client -> client.getBlobContainerClient(containerName))
                .filter(BlobContainerClient::exists)
                .orElseGet(() ->
                        blobServiceClient.createBlobContainer(containerName)
                );
    }

    @Bean("attachmentManagerAzureFileService")
    @ConditionalOnProperty(value = "uploader.enabled", matchIfMissing = true)
    public AzureStorageFileService azureStorageFileService(BlobContainerClient blobContainerClient) {
        return new AzureStorageFileService(
                blobContainerClient,
                azureProperties.getTempDir()
        );
    }

}
