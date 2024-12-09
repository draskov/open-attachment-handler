package rs.pumpkin.open_attachment_handler.service.impl;

import com.azure.storage.blob.models.BlobStorageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import rs.pumpkin.open_attachment_handler.exception.AttachmentNotFoundException;
import rs.pumpkin.open_attachment_handler.exception.ExternalServiceException;
import rs.pumpkin.open_attachment_handler.exception.InternalException;
import rs.pumpkin.open_attachment_handler.model.AttachmentContent;
import rs.pumpkin.open_attachment_handler.model.AttachmentParams;
import rs.pumpkin.open_attachment_handler.service.FileService;
import rs.pumpkin.open_attachment_handler.service.GeneralAttachmentService;
import rs.pumpkin.open_attachment_handler.utils.FileUtils;

import java.util.*;

@Slf4j
@Service
@ConditionalOnProperty(value = "uploader.enabled", matchIfMissing = true)
public class GeneralAttachmentServiceImpl implements GeneralAttachmentService {

    protected final FileService fileService;
    protected final List<AttachmentService<?, ?>> attachmentService;

    public GeneralAttachmentServiceImpl(
        @Qualifier("attachmentManagerAzureFileService") FileService fileService,
        List<AttachmentService<?, ?>> attachmentService
    ) {
        this.fileService = fileService;
        this.attachmentService = attachmentService;
    }

    @Override
    public AttachmentParams getUploadParameters(String fileName) {
        String extension = FileUtils.getExtension(fileName);
        String id = UUID.randomUUID().toString();

        final String attachmentUploadingUrl = fileService
            .getUploadingUrl(id, extension);

        return AttachmentParams.builder()
            .id(id)
            .url(attachmentUploadingUrl)
            .build();
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
    public List<AttachmentContent> getContentsByHolderId(String holderId) {
        if (attachmentService.isEmpty()) {
            throw new IllegalStateException("There is no available Attachment services.");
        }
        var attachmentContents = new ArrayList<AttachmentContent>();
        attachmentService.forEach(attachmentServiceCurr -> {
                var results = attachmentServiceCurr.getAttachmentContentByHolderID(holderId);
                if (!results.isEmpty()) {
                    attachmentContents.addAll(results);
                }
            }
        );
        return attachmentContents;
    }
}
