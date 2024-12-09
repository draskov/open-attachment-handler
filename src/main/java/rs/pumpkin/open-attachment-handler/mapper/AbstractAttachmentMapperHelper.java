package com.computerrock.attachmentmanager.mapper;

import com.computerrock.attachmentmanager.dto.AttachmentDTO;
import com.computerrock.attachmentmanager.dto.LinkAttachmentDTO;
import com.computerrock.attachmentmanager.model.AbstractAttachment;

public interface AbstractAttachmentMapperHelper<H,E extends AbstractAttachment<H>>{
    AttachmentDTO map(E attachment);
    void updateAttachmentData(E attachment, LinkAttachmentDTO updateData);
}
