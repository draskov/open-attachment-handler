package rs.pumpkin.open_attachment_handler.storage.impl;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.sas.BlobContainerSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.common.sas.SasProtocol;
import lombok.RequiredArgsConstructor;
import rs.pumpkin.open_attachment_handler.model.enums.AllowedFileType;
import rs.pumpkin.open_attachment_handler.storage.FileService;
import rs.pumpkin.open_attachment_handler.utils.FileUtils;

import java.time.OffsetDateTime;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class AzureStorageFileService implements FileService {

    private static final String FILE_SEPARATOR = "/";
    private static final String DOT = ".";
    private final BlobContainerClient blobContainerClient;
    private final String tempDirPath;

    @Override
    public String getUploadingUrl(String fileName, String extension) {

        String blobName = tempDirPath
                + FILE_SEPARATOR
                + fileName
                + DOT
                + extension;

        final var url = blobContainerClient
                .getBlobClient(blobName)
                .getBlobUrl();

        final var token = generateSasPermissionToken(
                blobName, true, true
        );

        return String.format("%s?%s", url, token);
    }


    @Override
    public void move(String source, String destination) {
        // Move blob from...
        BlobClient sourceBlob = blobContainerClient.getBlobClient(source);

        // Move blob to...
        BlobClient destinationBlob = blobContainerClient.getBlobClient(destination);

        boolean copied = destinationBlob.beginCopy(sourceBlob.getBlobUrl(), null)
                .waitForCompletion()
                .getStatus()
                .isComplete();

        if (copied) {
            sourceBlob.delete();
        }

    }


    @Override
    public void remove(String filePath) {
        blobContainerClient.getBlobClient(filePath).delete();
    }


    @Override
    public String getFileFullUrl(String fileName, String extension, String... dirs) {

        String path = Stream.concat(
                Stream.of(dirs),
                Stream.of(fileName)
        ).collect(Collectors.joining(FILE_SEPARATOR));

        var blobName = path + DOT + extension;

        final var url = blobContainerClient
                .getBlobClient(blobName)
                .getBlobUrl();

        final var token = generateSasPermissionToken(
                blobName, true, false
        );

        return String.format("%s?%s", url, token);
    }

    @Override
    public byte[] getFileContent(String filePath) {
        BinaryData binaryData = blobContainerClient.getBlobClient(filePath).downloadContent();
        return binaryData.toBytes();
    }

    @Override
    public void save(byte[] file, String filePath) {
        blobContainerClient.getBlobClient(filePath).upload(BinaryData.fromBytes(file));
    }

    @Override
    public String getTempDir() {
        return tempDirPath;
    }

    @Override
    public String generatePath(String fileName, String extension, String... folders) {
        return String.join(FILE_SEPARATOR, folders)
                + FILE_SEPARATOR
                + fileName
                + DOT
                + extension;
    }

    private String generateSasPermissionToken(
            String blobName, boolean read, boolean write
    ) {

        BlobContainerSasPermission blobContainerSasPermission = new BlobContainerSasPermission()
                .setReadPermission(read)
                .setWritePermission(write);

        BlobServiceSasSignatureValues builder = new BlobServiceSasSignatureValues(
                OffsetDateTime.now().plusMinutes(10),
                blobContainerSasPermission
        )
                .setContentType(AllowedFileType.from(
                        FileUtils.getExtension(blobName)
                ).getContentType())
                .setProtocol(SasProtocol.HTTPS_ONLY);

        return blobContainerClient
                .getBlobClient(blobName)
                .generateSas(builder);
    }


}
