package com.computerrock.attachmentmanager.exception;

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
