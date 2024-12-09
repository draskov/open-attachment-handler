package rs.pumpkin.open_attachment_handler.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.security.SecureRandom;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LinkAttachment {

    @Serial
    private static final long serialVersionUID = new SecureRandom().nextLong();

    private String fileName;
    private UUID id;
}
