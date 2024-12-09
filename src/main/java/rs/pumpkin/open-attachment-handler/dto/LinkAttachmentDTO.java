package com.computerrock.attachmentmanager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.security.SecureRandom;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class LinkAttachmentDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = new SecureRandom().nextLong();

    private String fileName;
    private UUID id;
}
