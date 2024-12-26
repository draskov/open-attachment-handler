package rs.pumpkin.open_attachment_handler.model;

import java.time.Instant;
import java.util.UUID;

public interface AbstractAttachment<H> {
     UUID getId();

     String getFileName();

     String getHolderId();

     String getExtension();

     H getHolder();

     void setId(UUID newValue);

     void setFileName(String newValue);

     void setExtension(String newValue);

     void setHolder(H newValue);

     void setPath(String path);

     String getPath();

     void setCreatedAt(Instant time);

     AbstractAttachment<H> copy();
}
