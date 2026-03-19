package rs.pumpkin.open_attachment_handler.exception;


public class AttachmentNotFoundException extends AttachmentLibraryBaseException {

    public AttachmentNotFoundException(String message) {
        super(message);
    }

    public AttachmentNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public AttachmentNotFoundException(Throwable cause) {
        super(cause);
    }
}
