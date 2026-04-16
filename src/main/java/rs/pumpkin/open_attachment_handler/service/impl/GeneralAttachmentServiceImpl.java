package rs.pumpkin.open_attachment_handler.service.impl;

import com.azure.storage.blob.models.BlobStorageException;
import lombok.extern.slf4j.Slf4j;
import rs.pumpkin.open_attachment_handler.OpenAttachmentManagerProps;
import rs.pumpkin.open_attachment_handler.exception.AttachmentNotFoundException;
import rs.pumpkin.open_attachment_handler.exception.ExternalServiceException;
import rs.pumpkin.open_attachment_handler.exception.InvalidFileTypeException;
import rs.pumpkin.open_attachment_handler.exception.InternalException;
import rs.pumpkin.open_attachment_handler.model.AttachmentContent;
import rs.pumpkin.open_attachment_handler.model.AttachmentParams;
import rs.pumpkin.open_attachment_handler.model.enums.AllowedFileType;
import rs.pumpkin.open_attachment_handler.service.GeneralAttachmentService;
import rs.pumpkin.open_attachment_handler.utils.FileUtils;

import java.util.*;
import java.util.function.Supplier;

@Slf4j
public class GeneralAttachmentServiceImpl implements GeneralAttachmentService {

    protected final List<AttachmentService<?>> attachmentService;
    protected final OpenAttachmentManagerProps openAttachmentManagerProps;

    public GeneralAttachmentServiceImpl(
            List<AttachmentService<?>> attachmentService
    ) {
        this(attachmentService, new OpenAttachmentManagerProps());
    }

    public GeneralAttachmentServiceImpl(
            List<AttachmentService<?>> attachmentService,
            OpenAttachmentManagerProps openAttachmentManagerProps
    ) {
        this.attachmentService = attachmentService;
        this.openAttachmentManagerProps = openAttachmentManagerProps;
    }

    private static Supplier<IllegalStateException> getIllegalStateExceptionSupplier() {
        return () -> new IllegalStateException("There is no available Attachment services.");
    }

    @Override
    public AttachmentParams getUploadParameters(
            String holderName,
            String fileName
    ) {
        String extension = FileUtils.getExtension(fileName);
        validateUploadExtension(extension);
        String id = UUID.randomUUID().toString();

        final String attachmentUploadingUrl = attachmentService.stream()
                .filter(service -> service.getHolderName().equals(holderName))
                .map(AttachmentService::getFileService)
                .findFirst()
                .orElseThrow(getIllegalStateExceptionSupplier())
                .getUploadingUrl(id, extension);

        return AttachmentParams.builder()
                .id(id)
                .url(attachmentUploadingUrl)
                .build();
    }

    private void validateUploadExtension(String extension) {
        if (!AllowedFileType.supports(extension)) {
            throw new InvalidFileTypeException(String.format(
                    "File extension '%s' is not supported. Supported extensions are: %s",
                    extension,
                    String.join(", ", new TreeSet<>(AllowedFileType.getExtensions()))
            ));
        }

        Set<String> configuredAllowedTypes = Optional.ofNullable(openAttachmentManagerProps)
                .map(OpenAttachmentManagerProps::getAllowedFileTypes)
                .filter(allowedTypes -> !allowedTypes.isEmpty())
                .orElse(AllowedFileType.getExtensions());

        boolean allowed = configuredAllowedTypes.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .map(String::toLowerCase)
                .anyMatch(extension::equals);

        if (!allowed) {
            throw new InvalidFileTypeException(String.format(
                    "File extension '%s' is not allowed for upload. Allowed extensions are: %s",
                    extension,
                    String.join(", ", new TreeSet<>(configuredAllowedTypes))
            ));
        }
    }

    @Override
    public AttachmentContent getContentById(UUID id) {
        return attachmentService.stream()
                .map(service -> {
                    try {
                        return service.getContentById(id);
                    } catch (AttachmentNotFoundException notFoundException) {
                        log.trace("Service with name {} encountered not found exception with message: {}",
                                service.getClass().getName(),
                                notFoundException.getMessage(),
                                notFoundException);
                        return null;
                    } catch (BlobStorageException ex) {
                        log.error("Encountered exception while downloading from blob storage with id {}. Exception message: {}",
                                id,
                                ex.getMessage(),
                                ex
                        );
                        throw new ExternalServiceException(
                                String.format("Encountered exception while downloading from blob storage with id %s. Exception message: %s",
                                        id, ex.getMessage()),
                                ex);
                    } catch (RuntimeException ex) {
                        throw new InternalException(
                                String.format(
                                        "Something went wrong when downloading Attachment with id: %s. Exception message: %s",
                                        id, ex.getMessage()),
                                ex);
                    }
                })
                .filter(Objects::nonNull)
                .findFirst().orElseThrow(() -> new AttachmentNotFoundException(
                        String.format("Attachment with id %s is not found", id)
                ));

    }

    @Override
    public List<AttachmentContent> getContentsByIds(Set<UUID> ids) {
        if (attachmentService.isEmpty()) {
            throw new IllegalStateException("There is no available Attachment services.");
        }
        var attachmentContents = new ArrayList<AttachmentContent>();
        attachmentService.forEach(attachmentServiceCurr -> {
                    var results = attachmentServiceCurr.getAttachmentContentsByIds(ids);
                    if (!results.isEmpty()) {
                        attachmentContents.addAll(results);
                    }
                }
        );
        return attachmentContents;
    }

    @Override
    public List<AttachmentContent> getContentsByHolderId(String holderName, String holderId) {
        return attachmentService
                .stream()
                .filter(service -> service.getHolderName().equals(holderName))
                .findFirst()
                .orElseThrow(getIllegalStateExceptionSupplier())
                .getAttachmentContentByHolderID(holderId);
    }
}
