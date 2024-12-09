package com.computerrock.attachmentmanager.model;

import lombok.Data;
import org.springframework.core.io.ByteArrayResource;

@Data
public class AttachmentContent {
    private ByteArrayResource byteArrayResource;
    private String fileName;
}
