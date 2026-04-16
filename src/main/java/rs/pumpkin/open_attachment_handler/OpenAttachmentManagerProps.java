package rs.pumpkin.open_attachment_handler;

import lombok.Data;

import java.util.Set;

@Data
public class OpenAttachmentManagerProps {

    private PrivateUrlInfo privateUrl;
    private Set<String> allowedFileTypes;

    @Data
    public static class PrivateUrlInfo {
        private Boolean enabled;
        private String baseUri;
    }

}
