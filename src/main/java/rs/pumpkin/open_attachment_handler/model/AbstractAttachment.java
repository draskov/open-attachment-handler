package rs.pumpkin.open_attachment_handler.model;

import java.time.Instant;
import java.util.UUID;

public interface AbstractAttachment {
    UUID getId();

    void setId(UUID newValue);

    String getFileName();

    void setFileName(String fileName);

    String getHolderId();

    void setHolderId(String holderId);

    String getExtension();

    void setExtension(String extension);

    String getPath();

    void setPath(String path);

    void setCreatedAt(Instant time);

    AbstractAttachment copy();
}
