package com.computerrock.attachmentmanager.spring.config.properties;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "uploader.azure")
public class AzureStorageBlobProperties {

    private static final char EQUALS_SEPARATOR = '=';
    private static final char SEMICOLON_SEPARATOR = ';';

    private String defaultEndpointsProtocol;
    private String accountName;
    private String accountKey;
    private String endpointSuffix;
    private String containerName;
    private String tempDir;


    public String getConnectionString() {
        return new StringBuilder()
                .append("DefaultEndpointsProtocol")
                .append(EQUALS_SEPARATOR)
                .append(getDefaultEndpointsProtocol())
                .append(SEMICOLON_SEPARATOR)

                .append("AccountName")
                .append(EQUALS_SEPARATOR)
                .append(getAccountName())
                .append(SEMICOLON_SEPARATOR)

                .append("AccountKey")
                .append(EQUALS_SEPARATOR)
                .append(getAccountKey())
                .append(SEMICOLON_SEPARATOR)

                .append("EndpointSuffix")
                .append(EQUALS_SEPARATOR)
                .append(getEndpointSuffix())

                .toString();
    }
}
