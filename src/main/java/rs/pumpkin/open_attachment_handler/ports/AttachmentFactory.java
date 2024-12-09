package rs.pumpkin.open_attachment_handler.ports;


import rs.pumpkin.open_attachment_handler.model.AbstractAttachment;
import rs.pumpkin.open_attachment_handler.model.LinkAttachment;

public interface AttachmentFactory<E extends AbstractAttachment<?>> {
    E create(LinkAttachment linkAttachment);
}
