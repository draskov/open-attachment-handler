package com.computerrock.attachmentmanager.ports;

import com.computerrock.attachmentmanager.dto.LinkAttachmentDTO;
import com.computerrock.attachmentmanager.model.AbstractAttachment;

public interface AttachmentFactory<E extends AbstractAttachment<?>> {
    E create(LinkAttachmentDTO linkAttachmentDTO);
}
