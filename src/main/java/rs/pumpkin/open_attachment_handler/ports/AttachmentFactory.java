package rs.pumpkin.open_attachment_handler.ports;


import rs.pumpkin.open_attachment_handler.model.AbstractAttachment;
import rs.pumpkin.open_attachment_handler.model.LinkAttachment;

public interface AttachmentFactory<A extends AbstractAttachment<?>> {
    A create(LinkAttachment linkAttachment);
}
