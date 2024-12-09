package com.computerrock.attachmentmanager.service;

import com.computerrock.attachmentmanager.ports.AttachmentHolder;

public interface HolderService<H extends AttachmentHolder> {
    H getHolder(String id);
}
