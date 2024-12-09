package com.computerrock.attachmentmanager.spring.controller.specification;


import com.computerrock.attachmentmanager.dto.AttachmentParamsDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.UUID;

@Tag(
        name = "Attachment API",
        description = "This API is for managing attachments"
)
public interface AttachmentApi{
    String DOWNLOAD_ENDPOINT = "/v1/attachments/download";

    @Operation(summary = "This request creates needed parameters for uploading an attachment.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Parameters are prepared for uploading an attachment and returned as DTO representation.",
                    content = @Content(schema = @Schema(implementation = AttachmentParamsDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request.",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error.",
                    content = @Content
            )
    })
    @GetMapping(
            value = "/v1/attachments/upload-parameters",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    AttachmentParamsDTO getUploadingParams(
            @RequestParam(value = "fileName") String fileName
    );


    @Operation(summary = "This request download attachment by id.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Attachment is downloaded.",
                    content = @Content(schema = @Schema(implementation = Resource.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request.",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Requested resource is not found.",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error.",
                    content = @Content
            )
    })
    @GetMapping(
            value= DOWNLOAD_ENDPOINT,
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
     ResponseEntity<Resource> downloadAttachment(@RequestParam("id") UUID id);

    @Operation(summary = "This request download attachments by ids as zip file.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Attachments are downloaded.",
                    content = @Content(schema = @Schema(implementation = Resource.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request.",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Requested resource is not found.",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error.",
                    content = @Content
            )
    })
    @GetMapping(
            value= DOWNLOAD_ENDPOINT+"/multiple",
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    ResponseEntity<Resource> downloadAttachments(@RequestParam("ids") @Valid Set<UUID> id);


    @Operation(summary = "This request download attachments by holder id as zip file.")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Attachments are downloaded.",
            content = @Content(schema = @Schema(implementation = Resource.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request.",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "204",
            description = "Requested resource is not found.",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error.",
            content = @Content
        )
    })
    @GetMapping(
        value= "/v1/attachments/holder/download",
        produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    ResponseEntity<Resource> downloadAttachmentsByHolder(@RequestParam("holderId") @Valid String holderId);
}
