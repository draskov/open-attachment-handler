package rs.pumpkin.open_attachment_handler.service.impl;


import com.azure.storage.blob.models.BlobStorageException;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import rs.pumpkin.open_attachment_handler.AttachmentManagerProperties;
import rs.pumpkin.open_attachment_handler.exception.AttachmentNotFoundException;
import rs.pumpkin.open_attachment_handler.exception.ExternalServiceException;
import rs.pumpkin.open_attachment_handler.exception.InternalException;
import rs.pumpkin.open_attachment_handler.mapper.AbstractAttachmentMapperHelper;
import rs.pumpkin.open_attachment_handler.model.AbstractAttachment;
import rs.pumpkin.open_attachment_handler.model.AttachmentContent;
import rs.pumpkin.open_attachment_handler.model.LinkAttachment;
import rs.pumpkin.open_attachment_handler.ports.AttachmentFactory;
import rs.pumpkin.open_attachment_handler.ports.AttachmentHolder;
import rs.pumpkin.open_attachment_handler.ports.AttachmentRepository;
import rs.pumpkin.open_attachment_handler.service.AttachmentServiceSpecification;
import rs.pumpkin.open_attachment_handler.storage.FileService;
import rs.pumpkin.open_attachment_handler.service.HolderService;
import rs.pumpkin.open_attachment_handler.service.TokenService;
import rs.pumpkin.open_attachment_handler.utils.FileUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Data
@RequiredArgsConstructor
@Slf4j
public class AttachmentService<H extends AttachmentHolder, A extends AbstractAttachment<H>> implements AttachmentServiceSpecification<H> {

    protected final FileService fileService;
    protected final AttachmentManagerProperties attachmentManagerProperties;
    protected final AttachmentFactory<A> attachmentFactory;
    protected final AbstractAttachmentMapperHelper<H, A> attachmentMapperHelper;
    protected final AttachmentRepository<A, H> attachmentRepository;
    private final TokenService tokenService;
    private final HolderService<H> holderService;
    private static final String DOWNLOAD_ENDPOINT = "url";

    @Override
    public Set<? extends AbstractAttachment<H>> updateAttachments(
        H holder,
        List<? extends LinkAttachment> linkAttachments,
        String sourceName
    ) {
        if (linkAttachments == null) {
            return Collections.emptySet();
        }

        List<A> existingAttachments = new ArrayList<>((Collection<A>) findAllByHolderAndSource(holder, sourceName));
        removeAttachments(existingAttachments, linkAttachments);

        List<A> inserted = insertAttachments(
            holder,
            existingAttachments,
            linkAttachments.stream().map(la -> createAttachment(holder, la, sourceName)).toList()
        );

        var existingUUIDs = existingAttachments.stream()
            .map(AbstractAttachment::getId)
            .collect(Collectors.toSet());
        linkAttachments.stream().filter(l -> existingUUIDs
                .contains(l.getId()))
            .forEach(l -> updateAttachmentData(l.getId(), l));
        return Stream.of(existingAttachments, inserted)
            .flatMap(Collection::stream).collect(Collectors.toSet());
    }

    @Override
    public Set<? extends AbstractAttachment<H>> findByIds(Set<UUID> ids) {
        return StreamSupport.stream(attachmentRepository.findAllById(ids).spliterator(), false)
                .collect(Collectors.toSet());
    }


    @Override
    public AttachmentContent getContentById(UUID id) {
        A attachment = attachmentRepository.findById(id)
            .orElseThrow(() -> new AttachmentNotFoundException(
                String.format("Attachment with id %s is not found", id)
            ));
        byte[] byteArrayResource;
        if (attachment.isForeignSource()) {
            URL url = getUrl(attachment);
            try {
                HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(url.toURI())
                    .header("Authorization", "Bearer " + tokenService.get())
                    .GET()
                    .build();

                HttpResponse<byte[]> response = HttpClient.newBuilder()
                    .build()
                    .send(httpRequest, HttpResponse.BodyHandlers.ofByteArray());

                log.info("Response with status {} for the attachment {} and url {} with body {}", response.statusCode(), id, url, response.body());

                byteArrayResource = response.body();

            } catch (URISyntaxException | IOException | InterruptedException e) {
                log.error("Error fetching attachment from URI: {}", url);
                throw new RuntimeException(e);
            }
        } else {
            byteArrayResource = fileService.getFileContent(attachment.getPath());
        }

        AttachmentContent attachmentContent = new AttachmentContent();
        attachmentContent.setFileName(attachment.getFileName());
        attachmentContent.setByteArrayResource(byteArrayResource);
        return attachmentContent;
    }


    protected void updateAttachmentData(UUID id, LinkAttachment linkAttachmentDTO) {
        A attachment = attachmentRepository.findById(id).orElseThrow(() -> new AttachmentNotFoundException(
            String.format("Attachment with id %s is not found", id)
        ));
        attachmentMapperHelper.updateAttachmentData(attachment, linkAttachmentDTO);
        attachmentRepository.save(attachment);
    }

    protected void moveAttachment(A attachment) {
        // Move Attachment from /tmp...

        String sourcePath = fileService.getTempDir() +
            "/" + attachment.getId() +
            "." + attachment.getExtension();

        // Move Attachment to...
        String destinationPath = generateRelativePath(attachment);

        fileService.move(sourcePath, destinationPath);
    }


    public A createAttachment(H holder, LinkAttachment linkAttachment, String sourceName) {
        A attachment = attachmentFactory.create(linkAttachment);
        attachment.setExtension(FileUtils.getExtension(linkAttachment.getFileName()));
        attachment.setHolder(holder);
        attachment.setSourceName(sourceName);
        attachment.setForeignSource(sourceName != null);
        attachmentMapperHelper.updateAttachmentData(attachment, linkAttachment);
        return attachment;
    }

    public void removeAttachments(
        List<A> existingAttachments,
        List<? extends LinkAttachment> linkAttachments
    ) {
        Set<UUID> removed = existingAttachments.stream()
            // Candidates for removal from existing...
            .filter(attachment -> linkAttachments.stream()
                .map(LinkAttachment::getId)
                .noneMatch(id -> id.equals(attachment.getId()))
            )
            // Remove each from file service
            .peek(attachment -> {
                if (!attachment.isForeignSource()) {
                    fileService.remove(attachment.getPath());
                }
            })
            .map(A::getId)
            .collect(Collectors.toSet());


        attachmentRepository.deleteAllById(removed);

        // Reduce existing by removed ones
        existingAttachments.removeIf(
            attachment -> removed.contains(attachment.getId())
        );
    }

    @Override
    public void upload(AbstractAttachment<H> attachment, byte[] resource) {
        fileService.save(resource, generateRelativePath(attachment));
    }

    public List<A> insertAttachments(
        H holder,
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
                if (!att.isForeignSource()) {
                    moveAttachment(att);
                }
            })
            .toList();

        attachmentRepository.saveAll(toInsert);

        return toInsert;
    }


    @Override
    public URL getUrl(AbstractAttachment<H> attachment) {
        String fileFullUrl;
        try {
            if (attachment.isForeignSource()) {
                String baseUrl = attachmentManagerProperties.getSources().stream()
                    .filter(source -> source.getName().equals(attachment.getSourceName()))
                    .findFirst()
                    .map(AttachmentManagerProperties.AttachmentSource::getBaseUri)
                    .orElseThrow(MalformedURLException::new);
                fileFullUrl = getFileFullUrl(attachment.getPath(), baseUrl);
            } else {
                if (Boolean.TRUE.equals(attachmentManagerProperties.getPrivateUrl().getEnabled())) {
                    String baseUrl = attachmentManagerProperties.getPrivateUrl().getBaseUri();
                    fileFullUrl = getFileFullUrl(attachment.getId().toString(), baseUrl);
                } else {
                    fileFullUrl = fileService.getFileFullUrl(
                        attachment.getId().toString(),
                        attachment.getExtension(),
                        attachment.getHolderId()
                    );
                }
            }
            return URI.create(fileFullUrl).normalize().toURL();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String getFileFullUrl(String id, String baseUrl) {
        return baseUrl + DOWNLOAD_ENDPOINT + "?id=" + id;
    }

    @Override
    public Set<? extends AbstractAttachment<H>> findAllByHolder(H holder) {
        return attachmentRepository
            .findAllByHolder(holder)
            .stream()
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    }

    @Override
    public Set<? extends AbstractAttachment<H>> findAllByHolderAndSource(H holder, String sourceName) {
        return sourceName == null
            ? findAllByHolder(holder)
            : findAllByHolder(holder)
            .stream()
            .filter(att -> sourceName.equals(att.getSourceName()))
            .collect(Collectors.toSet());
    }

    @Override
    public void add(AbstractAttachment<H> attachment) {
        attachmentRepository.save((A) attachment);
    }

    @Override
    public List<AttachmentContent> getAttachmentContentsByIds(Set<UUID> ids) {
        if (ids == null) {
            return Collections.emptyList();
        }
        if (ids.isEmpty()) {
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
        H holder;
        try {
            holder = holderService.getHolder(holderId);
        } catch (RuntimeException runtimeException) {
            return Collections.emptyList();
        }

        Set<A> existingAttachments = (Set<A>) findAllByHolder(holder);
        return getAttachmentContentsByIds(
            existingAttachments.stream()
                .map(AbstractAttachment::getId)
                .collect(Collectors.toSet())
        );
    }


    @Override
    public String generateRelativePath(AbstractAttachment<H> attachment) {
         attachment.setPath(attachment.getHolderId() + "/" + attachment.getId() + "." + attachment.getExtension());
         attachmentRepository.save((A) attachment);
         return attachment.getPath();
    }

    @Override
    public void copy(Collection<H> sources, H target) {
        final List<A> attList = new ArrayList<>();

        sources.forEach(source -> {
            attachmentRepository.findAllByHolder(source)
                .stream()
                .map(att -> (A) att.copy())
                .forEach(attList::add);

            attList.forEach(
                att -> {
                    att.setId(UUID.randomUUID());
                    att.setHolder(target);
                }
            );
        });

        attachmentRepository.saveAll(attList);
    }
}
