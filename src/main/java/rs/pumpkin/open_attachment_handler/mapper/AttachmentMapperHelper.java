package rs.pumpkin.open_attachment_handler.mapper;


import rs.pumpkin.open_attachment_handler.model.AbstractAttachment;
import rs.pumpkin.open_attachment_handler.model.Attachment;
import rs.pumpkin.open_attachment_handler.ports.AttachmentHolder;

public interface AttachmentMapperHelper<H extends AttachmentHolder, E extends AbstractAttachment<H>> extends AbstractAttachmentMapperHelper<H,E> {

    @Override
    Attachment map(E attachment);

}
