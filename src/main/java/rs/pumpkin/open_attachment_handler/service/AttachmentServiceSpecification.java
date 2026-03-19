package rs.pumpkin.open_attachment_handler.service;

import rs.pumpkin.open_attachment_handler.model.AbstractAttachment;
import rs.pumpkin.open_attachment_handler.model.AttachmentContent;
import rs.pumpkin.open_attachment_handler.storage.FileService;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface AttachmentServiceSpecification<A extends AbstractAttachment> {

    Set<A> updateAttachments(String holderId, List<A> linkAttachment);

    Set<A> findAllByHolder(String holderId);

    Set<A> findByIds(Set<UUID> ids);

    AttachmentContent getContentById(UUID id);

    URL getUrl(A attachment);

    void upload(A attachment, byte[] resource);

    void add(A attachment);

    List<AttachmentContent> getAttachmentContentsByIds(Set<UUID> ids);

    List<AttachmentContent> getAttachmentContentByHolderID(String holderId);

    String generateRelativePath(A attachment);

    void copy(String holderName, Collection<String> sourceHolderIds, String targetHolderId);

    FileService getFileService();

    String getHolderName();
}
