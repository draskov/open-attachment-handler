package rs.pumpkin.open_attachment_handler.storage.impl;

import rs.pumpkin.open_attachment_handler.storage.FileService;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

public class S3StorageFileService implements FileService {

    private static final String FILE_SEPARATOR = "/";
    private static final String DOT = ".";
    private final S3Client client;
    private final String tempDirPath;
    private final String bucketName;
    private final Region region;
    private final URI endpoint;
    private final StaticCredentialsProvider credentialsProvider;


    public S3StorageFileService(
            String accessKey,
            String secretKey,
            String regionName,
            String bucketName,
            String tempDirPath,
            URI endpoint
    ) {
        AwsCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        this.region = Region.of(regionName);
        this.credentialsProvider = StaticCredentialsProvider.create(credentials);
        this.endpoint = endpoint;
        this.client = S3Client
                .builder()
                .endpointOverride(endpoint)
                .region(region)
                .credentialsProvider(credentialsProvider)
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
                .build();
        this.tempDirPath = tempDirPath;
        this.bucketName = bucketName;
    }


    @Override
    public String getUploadingUrl(String fileName, String extension) {


        try (S3Presigner preSigner = S3Presigner.builder()
                .region(region) // Replace with your region
                .endpointOverride(endpoint)
                .credentialsProvider(credentialsProvider)
                .build()) {

            // Create a PutObjectRequest
            String filePath = generatePath(fileName, extension, this.tempDirPath);

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(filePath)
                    .build();

            // Create a PresignRequest with an expiration time
            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .putObjectRequest(putObjectRequest)
                    .signatureDuration(Duration.ofMinutes(15)) // URL valid for 15 minutes
                    .build();

            // Generate the pre-signed URL
            return preSigner.presignPutObject(presignRequest).url().toString();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void move(String source, String destination) {
        // Move blob from...
        CopyObjectRequest copyRequest = CopyObjectRequest.builder()
                .sourceBucket(bucketName)
                .sourceKey(source)
                .destinationBucket(bucketName)
                .destinationKey(destination)
                .build();

        client.copyObject(copyRequest);

        remove(source);

    }


    @Override
    public void remove(String filePath) {
        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(filePath)
                .build();

        client.deleteObject(deleteRequest);
    }


    @Override
    public String getFileFullUrl(String fileName, String extension, String... dirs) {

        try (S3Presigner preSigner = S3Presigner.builder()
                .endpointOverride(endpoint)
                .region(region)
                .credentialsProvider(credentialsProvider)
                .build()) {

            // Create a GetObjectRequest
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(generatePath(fileName, extension, dirs))
                    .build();

            // Create a PresignRequest with an expiration time
            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .getObjectRequest(getObjectRequest)
                    .signatureDuration(Duration.ofMinutes(15)) // URL valid for 15 minutes
                    .build();

            // Generate the pre-signed URL
            return preSigner.presignGetObject(presignRequest).url().toString();


        } catch (Exception e) {
            System.err.println("Error generating pre-signed URL: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] getFileContent(String filePath) {
        GetObjectRequest getRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(filePath)
                .build();

        ResponseBytes<GetObjectResponse> response = client.getObjectAsBytes(getRequest);

        return response.asByteArray();

    }

    @Override
    public void save(byte[] file, String filePath) {
        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(filePath)
                .build();

        Path path = Paths.get(filePath);
        client.putObject(putRequest, path);
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


}
