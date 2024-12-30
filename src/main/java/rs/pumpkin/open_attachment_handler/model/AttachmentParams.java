package rs.pumpkin.open_attachment_handler.model;

import lombok.Builder;

@Builder
public record AttachmentParams(String url, String id) {}