package rs.pumpkin.open_attachment_handler.service;

import rs.pumpkin.open_attachment_handler.model.AttachmentContent;
import rs.pumpkin.open_attachment_handler.model.AttachmentParams;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface GeneralAttachmentService {
    AttachmentParams getUploadParameters(String holderName, String fileName);

    AttachmentContent getContentById(UUID id);

    List<AttachmentContent> getContentsByIds(Set<UUID> ids);

    List<AttachmentContent> getContentsByHolderId(String holderName, String holderId);
}
