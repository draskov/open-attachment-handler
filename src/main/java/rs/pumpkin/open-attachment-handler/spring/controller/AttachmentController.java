package com.computerrock.attachmentmanager.spring.controller;

import com.computerrock.attachmentmanager.spring.controller.specification.AttachmentApi;
import com.computerrock.attachmentmanager.dto.AttachmentParamsDTO;
import com.computerrock.attachmentmanager.exception.BadRequestException;
import com.computerrock.attachmentmanager.model.AttachmentContent;
import com.computerrock.attachmentmanager.service.GeneralAttachmentService;
import com.computerrock.attachmentmanager.utils.HttpUtils;
import com.computerrock.attachmentmanager.utils.ResourceUtils;
import com.computerrock.attachmentmanager.utils.FileUtils;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;


@RestController
@AllArgsConstructor
@ConditionalOnProperty(value = "uploader.enabled", matchIfMissing = true)
public class AttachmentController implements AttachmentApi {

    private final GeneralAttachmentService attachmentService;


    @Override
    public AttachmentParamsDTO getUploadingParams(String fileName) {
        return attachmentService.getUploadParameters(fileName);
    }

    @Override
    public ResponseEntity<Resource> downloadAttachment(UUID id) {
        AttachmentContent attachmentContent = attachmentService.getContentById(id);
        return new ResponseEntity<>(
                attachmentContent.getByteArrayResource(),
                HttpUtils.getHttpHeadersForFile(attachmentContent.getFileName()),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Resource> downloadAttachments(@NotEmpty Set<UUID> ids) {

        var attachmentContents = attachmentService.getContentsByIds(ids)
                .stream()
                .map(attachmentContent -> ResourceUtils.convertWithCustomName(
                        attachmentContent.getFileName(),
                        attachmentContent.getByteArrayResource()
                ))
                .toList();

        if (attachmentContents.size() != ids.size()) {
            throw new BadRequestException(
                    String.format("Some of attachments were not found within provided ids. Provided Id's: %s",
                            Arrays.toString(ids.toArray()))
            );
        }
        var attachmentInputMap = ResourceUtils.convertToInputStreamMap(attachmentContents);

        return ResponseEntity.ok()
                .headers(HttpUtils.getHttpHeadersForFile("downloads.zip"))
                .body(new ByteArrayResource(
                        FileUtils.zipFiles(
                                        attachmentInputMap,
                                        new ByteArrayOutputStream()
                                )
                                .toByteArray()));
    }

    @Override
    public ResponseEntity<Resource> downloadAttachmentsByHolder(String holderId) {

        var attachmentContents = attachmentService.getContentsByHolderId(holderId)
            .stream()
            .map(attachmentContent -> ResourceUtils.convertWithCustomName(
                attachmentContent.getFileName(),
                attachmentContent.getByteArrayResource()
            ))
            .toList();


        if (attachmentContents.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        var attachmentInputMap = ResourceUtils.convertToInputStreamMap(attachmentContents);

        return ResponseEntity.ok()
            .headers(HttpUtils.getHttpHeadersForFile(holderId + "_attachments.zip"))
            .body(new ByteArrayResource(
                FileUtils.zipFiles(
                        attachmentInputMap,
                        new ByteArrayOutputStream()
                    )
                    .toByteArray()));
    }

}
