package rs.pumpkin.open_attachment_handler.exception;

public class InternalException extends AttachmentLibraryBaseException {
    public InternalException(String message) {
        super(message);
    }

    public InternalException(Throwable cause) {
        super(cause);
    }
    public InternalException(String message, Throwable cause) {
        super(message, cause);
    }
}
