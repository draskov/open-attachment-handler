package rs.pumpkin.open_attachment_handler.model;

import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.security.SecureRandom;

@Data
@Builder
public class AttachmentParams implements Serializable {

    @Serial
    private static final long serialVersionUID = new SecureRandom().nextLong();

    private String url;
    private String id;
}