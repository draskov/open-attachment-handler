package rs.pumpkin.open_attachment_handler.mapper;


import rs.pumpkin.open_attachment_handler.model.AbstractAttachment;
import rs.pumpkin.open_attachment_handler.model.Attachment;

public interface AbstractAttachmentMapperHelper<H,E extends AbstractAttachment>{
    Attachment map(E attachment);
}
