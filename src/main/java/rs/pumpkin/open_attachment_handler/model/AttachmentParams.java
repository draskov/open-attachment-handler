package rs.pumpkin.open_attachment_handler.model;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record AttachmentParams(String url, String id) implements Serializable {}