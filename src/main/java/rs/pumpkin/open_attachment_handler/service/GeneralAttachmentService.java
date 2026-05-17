package rs.pumpkin.open_attachment_handler.service;

import rs.pumpkin.open_attachment_handler.model.AttachmentContent;
import rs.pumpkin.open_attachment_handler.model.AttachmentParams;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface GeneralAttachmentService {
    AttachmentParams getUploadParameters(String holderName, String fileName);

    AttachmentContent getContentById(UUID id);

    /**
     * Resolves a time-limited, pre-signed storage URL for the attachment with the given id.
     * Intended for download endpoints that issue an HTTP redirect to storage instead of
     * streaming the file bytes through the application.
     *
     * @param id attachment id
     * @return pre-signed download URL the client can be redirected to
     */
    String getContentUrlById(UUID id);

    List<AttachmentContent> getContentsByIds(Set<UUID> ids);

    List<AttachmentContent> getContentsByHolderId(String holderName, String holderId);
}
