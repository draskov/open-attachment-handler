package com.computerrock.attachmentmanager.spring.config;

import com.computerrock.attachmentmanager.spring.config.properties.AttachmentManagerProperties;
import com.computerrock.attachmentmanager.spring.config.properties.AzureStorageBlobProperties;
import com.computerrock.attachmentmanager.spring.config.properties.DatabaseProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(
        {AzureStorageBlobProperties.class, AttachmentManagerProperties.class, DatabaseProperties.class}
)
//@PropertySource(value = "classpath:uploader-application.yaml", factory = YamlPropertySourceFactory.class)
public class UploaderConfiguration {
}
