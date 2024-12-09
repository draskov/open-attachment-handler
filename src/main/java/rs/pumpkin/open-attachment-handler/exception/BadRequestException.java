package com.computerrock.attachmentmanager.exception;

public class BadRequestException extends AttachmentLibraryBaseException {


    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    public BadRequestException(Throwable cause) {
        super(cause);
    }
}
