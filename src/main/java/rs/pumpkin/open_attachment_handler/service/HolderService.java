package rs.pumpkin.open_attachment_handler.service;


import rs.pumpkin.open_attachment_handler.ports.AttachmentHolder;

public interface HolderService<H extends AttachmentHolder> {
    H getHolder(String id);
    String getHolderName();
}
