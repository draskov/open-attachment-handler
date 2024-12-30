package rs.pumpkin.open_attachment_handler.model;

import java.time.Instant;
import java.util.UUID;

public interface AbstractAttachment {
     UUID getId();

     String getFileName();

     String getHolderId();

     String getExtension();

     void setId(UUID newValue);

     void setFileName(String fileName);

     void setExtension(String extension);

     void setHolderId(String holderId);

     void setPath(String path);

     String getPath();

     void setCreatedAt(Instant time);

     AbstractAttachment copy();
}
