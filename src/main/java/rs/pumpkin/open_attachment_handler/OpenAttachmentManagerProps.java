package rs.pumpkin.open_attachment_handler;

import lombok.Data;

@Data
public class OpenAttachmentManagerProps {

    private PrivateUrlInfo privateUrl;

    @Data
    public static class PrivateUrlInfo {
        private Boolean enabled;
        private String baseUri;
    }

}
