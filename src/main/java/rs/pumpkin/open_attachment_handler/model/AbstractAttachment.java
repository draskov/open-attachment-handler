package rs.pumpkin.open_attachment_handler.model;

import java.time.Instant;
import java.util.UUID;

public abstract class AbstractAttachment<H> {
    public abstract UUID getId();

    public abstract String getFileName();

    public abstract String getHolderId();

    public abstract String getExtension();

    public abstract H getHolder();

    public abstract void setId(UUID newValue);

    public abstract void setFileName(String newValue);

    public abstract void setExtension(String newValue);

    public abstract void setHolder(H newValue);

    public abstract void setPath(String path);

    public abstract void setSourceName(String sourceName);

    public abstract String getSourceName();

    public abstract void setForeignSource(boolean isForeignSource);

    public abstract boolean isForeignSource();

    public abstract String getPath();

    public abstract void setCreatedAt(Instant time);

    public abstract AbstractAttachment<H> copy();
}
