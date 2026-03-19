package rs.pumpkin.open_attachment_handler.service.impl;


import com.azure.storage.blob.models.BlobStorageException;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import rs.pumpkin.open_attachment_handler.OpenAttachmentManagerProps;
import rs.pumpkin.open_attachment_handler.exception.AttachmentNotFoundException;
import rs.pumpkin.open_attachment_handler.exception.ExternalServiceException;
import rs.pumpkin.open_attachment_handler.exception.InternalException;
import rs.pumpkin.open_attachment_handler.model.AbstractAttachment;
import rs.pumpkin.open_attachment_handler.model.AttachmentContent;
import rs.pumpkin.open_attachment_handler.ports.AttachmentRepository;
import rs.pumpkin.open_attachment_handler.service.AttachmentServiceSpecification;
import rs.pumpkin.open_attachment_handler.storage.FileService;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@RequiredArgsConstructor
@Slf4j
public class AttachmentService<A extends AbstractAttachment> implements AttachmentServiceSpecification<A> {

    protected final FileService fileService;
    protected final AttachmentRepository<A> attachmentRepository;
    protected final OpenAttachmentManagerProps openAttachmentManagerProps;
    private final String holderName;

    @Override
    public Set<A> updateAttachments(String holderId, List<A> updateAttachmentList) {
        if (updateAttachmentList == null) {
            return Collections.emptySet();
        }
        updateAttachmentList.forEach(attachment -> {
            attachment.setHolderId(holderId);
            attachment.setHolderName(holderName);
        });

        List<A> existingAttachments = new ArrayList<>(findAllByHolder(holderId));
        removeAttachments(existingAttachments, updateAttachmentList);

        List<A> inserted = insertAttachments(
                existingAttachments,
                updateAttachmentList
        );

        var existingUUIDs = existingAttachments.stream()
                .map(AbstractAttachment::getId)
                .collect(Collectors.toSet());

        updateAttachmentList.stream()
                .filter(l -> existingUUIDs.contains(l.getId()))
                .forEach(attachmentRepository::save);

        return Stream.of(existingAttachments, inserted)
                .flatMap(Collection::stream)
                .peek(a -> a.setUrl(getUrl(a)))
                .collect(Collectors.toSet());
    }

    @Override
    public Set<A> findByIds(Set<UUID> ids) {
        return new HashSet<>(
                attachmentRepository.findAllById(ids).stream()
                        .peek(a -> a.setUrl(getUrl(a)))
                        .toList()
        );
    }

    @Override
    public AttachmentContent getContentById(UUID id) {
        A attachment = attachmentRepository.findById(id)
                .orElseThrow(() -> new AttachmentNotFoundException(
                        String.format("Attachment with id %s is not found", id)
                ));
        byte[] byteArrayResource = fileService.getFileContent(attachment.getPath());

        return new AttachmentContent(
                attachment.getFileName(),
                byteArrayResource
        );
    }

    protected void moveAttachment(A attachment) {
        // Move Attachment from /tmp...
        String sourcePath =
                fileService.generatePath(
                        attachment.getId().toString(),
                        attachment.getExtension(),
                        fileService.getTempDir()
                );

        // Move Attachment to...
        String destinationPath = generateRelativePath(attachment);

        fileService.move(sourcePath, destinationPath);
    }

    public void removeAttachments(
            List<A> existingAttachments,
            List<A> linkAttachments
    ) {
        Set<UUID> removed = existingAttachments.stream()
                // Candidates for removal from existing...
                .filter(attachment -> linkAttachments.stream()
                        .map(A::getId)
                        .noneMatch(id -> id.equals(attachment.getId()))
                )
                // Remove each from file service
                .peek(attachment -> fileService.remove(attachment.getPath()))
                .map(A::getId)
                .collect(Collectors.toSet());


        attachmentRepository.deleteAllById(removed);

        // Reduce existing by removed ones
        existingAttachments.removeIf(
                attachment -> removed.contains(attachment.getId())
        );
    }

    @Override
    public void upload(A attachment, byte[] resource) {
        fileService.save(resource, generateRelativePath(attachment));
    }

    public List<A> insertAttachments(
            List<A> existingAttachments,
            List<A> givenAttachmentsList
    ) {
        List<A> toInsert = givenAttachmentsList.stream()
                .filter(link -> existingAttachments.stream()
                        .map(AbstractAttachment::getId)
                        .noneMatch(uuid -> uuid.equals(link.getId()))
                )
                .peek(att -> {
                    att.setCreatedAt(Instant.now());
                    moveAttachment(att);
                })
                .toList();

        attachmentRepository.saveAll(toInsert);

        return toInsert;
    }

    @Override
    public URL getUrl(A attachment) {
        String fileFullUrl;
        try {
            if (Boolean.TRUE.equals(openAttachmentManagerProps.getPrivateUrl().getEnabled())) {
                String baseUrl = openAttachmentManagerProps.getPrivateUrl().getBaseUri();
                fileFullUrl = getFileFullUrl(attachment.getId().toString(), baseUrl);
            } else {
                fileFullUrl = fileService.getFileFullUrl(
                        attachment.getId().toString(),
                        attachment.getExtension(),
                        attachment.getHolderId()
                );
            }
            return URI.create(fileFullUrl).normalize().toURL();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Set<A> findAllByHolder(String holderId) {
        return attachmentRepository
                .findAllByHolderNameAndHolderId(holderName, holderId)
                .stream()
                .filter(Objects::nonNull)
                .peek(a -> a.setUrl(getUrl(a)))
                .collect(Collectors.toSet());
    }


    @Override
    public void add(A attachment) {
        attachmentRepository.save(attachment);
    }

    @Override
    public List<AttachmentContent> getAttachmentContentsByIds(Set<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }

        var exceptionLogMessageTemplate = "Attachment with uuid {} encountered exception {}. Exception message: {}";
        return ids.stream().map(uuid -> {
                    try {
                        return getContentById(uuid);
                    } catch (AttachmentNotFoundException ex) {
                        log.trace(exceptionLogMessageTemplate,
                                uuid,
                                ex.getClass().getSimpleName(),
                                ex.getMessage()
                        );

                        return null;
                    } catch (BlobStorageException ex) {
                        log.error(exceptionLogMessageTemplate,
                                uuid,
                                ex.getClass().getSimpleName(),
                                ex.getMessage(),
                                ex
                        );
                        throw new ExternalServiceException(
                                String.format("Encountered exception while downloading from blob storage with id %s. Exception message: %s",
                                        uuid, ex.getMessage()),
                                ex);

                    } catch (RuntimeException ex) {
                        throw new InternalException(
                                String.format(
                                        "Something went wrong when downloading Attachment with id: %s. Exception message: %s",
                                        uuid, ex.getMessage()),
                                ex);
                    }
                })
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public List<AttachmentContent> getAttachmentContentByHolderID(String holderId) {
        Set<A> existingAttachments = findAllByHolder(holderId);
        return getAttachmentContentsByIds(
                existingAttachments.stream()
                        .map(AbstractAttachment::getId)
                        .collect(Collectors.toSet())
        );
    }


    @Override
    public String generateRelativePath(A attachment) {
        attachment.setPath(
                fileService.generatePath(
                        attachment.getId().toString(),
                        attachment.getExtension(),
                        this.holderName,
                        attachment.getHolderId()
                )
        );
        attachmentRepository.save(attachment);
        return attachment.getPath();
    }

    @Override
    public void copy(String holderName, Collection<String> sourceIds, String targetId) {
        final List<A> attList = new ArrayList<>();

        sourceIds.forEach(source -> {
            attachmentRepository.findAllByHolderNameAndHolderId(holderName, targetId)
                    .stream()
                    .map(att -> (A) att.copy())
                    .forEach(attList::add);

            attList.forEach(
                    att -> {
                        att.setId(UUID.randomUUID());
                        att.setHolderId(targetId);
                    }
            );
        });

        attachmentRepository.saveAll(attList);
    }

    @Override
    public String getHolderName() {
        return holderName;
    }

    private String getFileFullUrl(String id, String baseUrl) {
        return baseUrl + "?id=" + id;
    }
}
