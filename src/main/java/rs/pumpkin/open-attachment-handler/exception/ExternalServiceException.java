package com.computerrock.attachmentmanager.exception;

public class ExternalServiceException extends AttachmentLibraryBaseException {
    public ExternalServiceException(String message) {
        super(message);
    }
    public ExternalServiceException(String message, Throwable cause) {
        super(message, cause);
    }
    public ExternalServiceException(Throwable cause) {
        super(cause);
    }
}
