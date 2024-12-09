package com.computerrock.attachmentmanager.mapper;

import com.computerrock.attachmentmanager.dto.AttachmentDTO;
import com.computerrock.attachmentmanager.dto.LinkAttachmentDTO;
import com.computerrock.attachmentmanager.spring.entity.Attachment;
import com.computerrock.attachmentmanager.ports.AttachmentHolder;

public interface AttachmentMapperHelper<H extends AttachmentHolder, E extends Attachment<H>> extends AbstractAttachmentMapperHelper<H,E> {

    @Override
    AttachmentDTO map(E attachment);

    @Override
    void updateAttachmentData(E attachment, LinkAttachmentDTO updateData);
}
