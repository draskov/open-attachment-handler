package com.computerrock.attachmentmanager.service;

import com.computerrock.attachmentmanager.dto.AttachmentParamsDTO;
import com.computerrock.attachmentmanager.model.AttachmentContent;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface GeneralAttachmentService {
    AttachmentParamsDTO getUploadParameters(String fileName);
    AttachmentContent getContentById(UUID id);
    List<AttachmentContent> getContentsByIds(Set<UUID> ids);
    List<AttachmentContent> getContentsByHolderId(String holderId);
}
