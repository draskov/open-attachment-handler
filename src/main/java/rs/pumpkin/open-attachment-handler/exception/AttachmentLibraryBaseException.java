package com.computerrock.attachmentmanager.exception;

import lombok.Getter;

@Getter
public class AttachmentLibraryBaseException extends RuntimeException{


    public AttachmentLibraryBaseException(String message) {
        super(message);
    }

    public AttachmentLibraryBaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public AttachmentLibraryBaseException(Throwable cause) {
        super(cause);
    }
}
