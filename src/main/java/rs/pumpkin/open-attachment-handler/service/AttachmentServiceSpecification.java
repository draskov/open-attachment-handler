package com.computerrock.attachmentmanager.service;

import com.computerrock.attachmentmanager.dto.LinkAttachmentDTO;
import com.computerrock.attachmentmanager.model.AbstractAttachment;
import com.computerrock.attachmentmanager.model.AttachmentContent;
import com.computerrock.attachmentmanager.ports.AttachmentHolder;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface AttachmentServiceSpecification<H extends AttachmentHolder> {

    Set<? extends AbstractAttachment<H>> updateAttachments(
            H holder,
            List<? extends LinkAttachmentDTO> linkAttachmentDTO,
            String sourceName
    );

    Set<? extends AbstractAttachment<H>> findAllByHolder(H holder);

    Set<? extends AbstractAttachment<H>> findAllByHolderAndSource(H holder, String sourceName);

    Set<? extends AbstractAttachment<H>> findByIds(Set<UUID> ids);

    AttachmentContent getContentById(UUID id);

    URL getUrl(AbstractAttachment<H> attachment);

    void upload(AbstractAttachment<H> attachment, byte[] resource);

    void add(AbstractAttachment<H> attachment);

    List<AttachmentContent> getAttachmentContentsByIds(Set<UUID> ids);

    List<AttachmentContent> getAttachmentContentByHolderID(String holderId);

    String generateRelativePath(AbstractAttachment<H> attachment);

    void copy(Collection<H> sources, H target);
}
