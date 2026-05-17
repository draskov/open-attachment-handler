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

    /**
     * Generates resized, square, center-cropped copies of the image attachment with the
     * given id, one per variant configured under {@code imageResizing}. Intended to be
     * driven by an application-side scheduled job.
     *
     * @param id attachment id
     * @return names of the variants that were generated; empty if the attachment is not
     *         a resizable image or no variants are configured
     */
    List<String> generateResizedCopies(UUID id);

    /**
     * Resolves a time-limited, pre-signed storage URL for a previously generated resized
     * copy, so galleries and lists can display lightweight thumbnails.
     *
     * @param id          attachment id
     * @param variantName name of a configured image variant
     * @return pre-signed download URL of the resized copy
     */
    String getResizedContentUrlById(UUID id, String variantName);

    List<AttachmentContent> getContentsByIds(Set<UUID> ids);

    List<AttachmentContent> getContentsByHolderId(String holderName, String holderId);
}
