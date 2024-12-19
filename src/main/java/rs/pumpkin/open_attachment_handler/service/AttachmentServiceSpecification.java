package rs.pumpkin.open_attachment_handler.service;

import rs.pumpkin.open_attachment_handler.model.AbstractAttachment;
import rs.pumpkin.open_attachment_handler.model.AttachmentContent;
import rs.pumpkin.open_attachment_handler.model.LinkAttachment;
import rs.pumpkin.open_attachment_handler.ports.AttachmentHolder;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface AttachmentServiceSpecification<A extends AbstractAttachment<H>, H extends AttachmentHolder> {

    Set<A> updateAttachments(
            H holder,
            List<? extends LinkAttachment> linkAttachment,
            String sourceName
    );

    Set<A> findAllByHolder(H holder);

    Set<A> findAllByHolderAndSource(H holder, String sourceName);

    Set<A> findByIds(Set<UUID> ids);

    AttachmentContent getContentById(UUID id);

    URL getUrl(AbstractAttachment<H> attachment);

    void upload(AbstractAttachment<H> attachment, byte[] resource);

    void add(AbstractAttachment<H> attachment);

    List<AttachmentContent> getAttachmentContentsByIds(Set<UUID> ids);

    List<AttachmentContent> getAttachmentContentByHolderID(String holderId);

    String generateRelativePath(AbstractAttachment<H> attachment);

    void copy(Collection<H> sources, H target);
}
