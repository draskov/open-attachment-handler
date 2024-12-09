package rs.pumpkin.open_attachment_handler.mapper;


import rs.pumpkin.open_attachment_handler.model.AbstractAttachment;
import rs.pumpkin.open_attachment_handler.model.Attachment;
import rs.pumpkin.open_attachment_handler.model.LinkAttachment;

public interface AbstractAttachmentMapperHelper<H,E extends AbstractAttachment<H>>{
    Attachment map(E attachment);
    void updateAttachmentData(E attachment, LinkAttachment updateData);
}
