package com.computerrock.attachmentmanager.mapper;

import com.computerrock.attachmentmanager.dto.AttachmentDTO;
import com.computerrock.attachmentmanager.dto.LinkAttachmentDTO;
import com.computerrock.attachmentmanager.spring.entity.AttachmentCassandra;
import com.computerrock.attachmentmanager.ports.AttachmentHolder;

public interface AttachmentMapperCassandraHelper<H extends AttachmentHolder,A extends AttachmentCassandra<H>> extends AbstractAttachmentMapperHelper<H,A>{
    @Override
    AttachmentDTO map(A attachment);

    @Override
    void updateAttachmentData(A attachment, LinkAttachmentDTO updateData);
}
