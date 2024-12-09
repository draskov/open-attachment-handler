package com.computerrock.attachmentmanager.service.impl;

import com.azure.storage.blob.models.BlobStorageException;
import com.computerrock.attachmentmanager.dto.AttachmentParamsDTO;
import com.computerrock.attachmentmanager.exception.AttachmentNotFoundException;
import com.computerrock.attachmentmanager.exception.ExternalServiceException;
import com.computerrock.attachmentmanager.exception.InternalException;
import com.computerrock.attachmentmanager.model.AttachmentContent;
import com.computerrock.attachmentmanager.service.FileService;
import com.computerrock.attachmentmanager.service.GeneralAttachmentService;
import com.computerrock.attachmentmanager.utils.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@ConditionalOnProperty(value = "uploader.enabled", matchIfMissing = true)
public class GeneralAttachmentServiceImpl implements GeneralAttachmentService {

    protected final FileService fileService;
    protected final List<com.computerrock.attachmentmanager.service.impl.AttachmentService<?, ?>> attachmentService;

    public GeneralAttachmentServiceImpl(
        @Qualifier("attachmentManagerAzureFileService") FileService fileService,
        List<com.computerrock.attachmentmanager.service.impl.AttachmentService<?, ?>> attachmentService
    ) {
        this.fileService = fileService;
        this.attachmentService = attachmentService;
    }

    @Override
    public AttachmentParamsDTO getUploadParameters(String fileName) {
        String extension = FileUtils.getExtension(fileName);
        String id = UUID.randomUUID().toString();

        final String attachmentUploadingUrl = fileService
            .getUploadingUrl(id, extension);

        return AttachmentParamsDTO.builder()
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
