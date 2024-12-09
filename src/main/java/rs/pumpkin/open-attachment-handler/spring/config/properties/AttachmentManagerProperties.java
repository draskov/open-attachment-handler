package com.computerrock.attachmentmanager.spring.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "uploader")
public class AttachmentManagerProperties {

    private PrivateUrlInfo privateUrl;
    private List<AttachmentSource> sources;

    @Data
    public static class PrivateUrlInfo {
        private Boolean enabled;
        private String baseUri;
    }

    @Data
    public static class AttachmentSource {
        private String name;
        private String baseUri;
    }
}
