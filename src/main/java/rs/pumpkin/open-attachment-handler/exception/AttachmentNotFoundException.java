package com.computerrock.attachmentmanager.exception;


public class AttachmentNotFoundException extends AttachmentLibraryBaseException{

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
