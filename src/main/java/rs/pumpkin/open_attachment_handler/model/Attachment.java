package rs.pumpkin.open_attachment_handler.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.net.URL;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class Attachment implements Serializable {

    @Serial
    private static final long serialVersionUID = 7475231198145102948L;

    protected UUID id;
    protected URL url;
    protected String fileName;

}
