package rs.pumpkin.open_attachment_handler;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
public class OpenAttachmentManagerProps {

    private PrivateUrlInfo privateUrl;
    private Set<String> allowedFileTypes;
    private ImageResizing imageResizing;

    @Data
    public static class PrivateUrlInfo {
        private Boolean enabled;
        private String baseUri;
    }

    /**
     * Configuration for generating resized (square, center-cropped) copies of image
     * attachments. The application typically triggers generation from a scheduled job.
     */
    @Data
    public static class ImageResizing {
        private Boolean enabled;
        private List<ImageVariant> variants;
    }

    /**
     * A single resized variant: a {@code name} used to address the copy and a
     * {@code size} giving the side length, in pixels, of the resulting square image.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageVariant {
        private String name;
        private Integer size;
    }

}
