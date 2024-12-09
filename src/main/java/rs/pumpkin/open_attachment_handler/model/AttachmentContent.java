package rs.pumpkin.open_attachment_handler.model;

import lombok.Data;
import org.springframework.core.io.ByteArrayResource;

@Data
public class AttachmentContent {
    private ByteArrayResource byteArrayResource;
    private String fileName;
}
