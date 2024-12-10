package rs.pumpkin.open_attachment_handler.model;

import lombok.Data;

@Data
public class AttachmentContent {
    private byte[] byteArrayResource;
    private String fileName;
}
