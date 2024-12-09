package com.computerrock.attachmentmanager.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.security.SecureRandom;

@Data
@Builder
public class AttachmentParamsDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = new SecureRandom().nextLong();

    private String url;
    private String id;
}
