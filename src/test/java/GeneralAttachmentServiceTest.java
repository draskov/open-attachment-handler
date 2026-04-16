import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import rs.pumpkin.open_attachment_handler.OpenAttachmentManagerProps;
import rs.pumpkin.open_attachment_handler.exception.InvalidFileTypeException;
import rs.pumpkin.open_attachment_handler.model.AttachmentParams;
import rs.pumpkin.open_attachment_handler.ports.AttachmentRepository;
import rs.pumpkin.open_attachment_handler.service.impl.AttachmentService;
import rs.pumpkin.open_attachment_handler.service.impl.GeneralAttachmentServiceImpl;
import rs.pumpkin.open_attachment_handler.storage.FileService;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.net.URL;
import java.time.Instant;

public class GeneralAttachmentServiceTest {

    @Test
    void shouldGenerateUploadParametersForConfiguredAllowedType() {
        OpenAttachmentManagerProps props = new OpenAttachmentManagerProps();
        props.setAllowedFileTypes(Set.of("pdf", "jpg", "jpeg", "png", "stl", "obj", "ply", "dcm", "zip", "docx", "xlsx"));

        GeneralAttachmentServiceImpl service = new GeneralAttachmentServiceImpl(
                List.of(buildAttachmentService("model", new StubFileService())),
                props
        );

        AttachmentParams params = service.getUploadParameters("model", "scan.dcm");

        Assertions.assertNotNull(params.id());
        Assertions.assertTrue(params.url().endsWith(".dcm"));
    }

    @Test
    void shouldRejectExtensionThatIsNotAllowedByConfiguration() {
        OpenAttachmentManagerProps props = new OpenAttachmentManagerProps();
        props.setAllowedFileTypes(Set.of("pdf"));

        GeneralAttachmentServiceImpl service = new GeneralAttachmentServiceImpl(
                List.of(buildAttachmentService("docs", new StubFileService())),
                props
        );

        InvalidFileTypeException exception = Assertions.assertThrows(
                InvalidFileTypeException.class,
                () -> service.getUploadParameters("docs", "image.png")
        );

        Assertions.assertTrue(exception.getMessage().contains("not allowed"));
    }

    @Test
    void shouldRejectUnsupportedExtensionEvenWhenConfigured() {
        OpenAttachmentManagerProps props = new OpenAttachmentManagerProps();
        props.setAllowedFileTypes(Set.of("exe"));

        GeneralAttachmentServiceImpl service = new GeneralAttachmentServiceImpl(
                List.of(buildAttachmentService("docs", new StubFileService())),
                props
        );

        InvalidFileTypeException exception = Assertions.assertThrows(
                InvalidFileTypeException.class,
                () -> service.getUploadParameters("docs", "malware.exe")
        );

        Assertions.assertTrue(exception.getMessage().contains("not supported"));
    }

    @Test
    void shouldRejectFileWithoutExtension() {
        OpenAttachmentManagerProps props = new OpenAttachmentManagerProps();
        props.setAllowedFileTypes(Set.of("pdf"));

        GeneralAttachmentServiceImpl service = new GeneralAttachmentServiceImpl(
                List.of(buildAttachmentService("docs", new StubFileService())),
                props
        );

        InvalidFileTypeException exception = Assertions.assertThrows(
                InvalidFileTypeException.class,
                () -> service.getUploadParameters("docs", "README")
        );

        Assertions.assertTrue(exception.getMessage().contains("does not have an extension"));
    }

    private AttachmentService<TestAttachment> buildAttachmentService(String holderName, FileService fileService) {
        return new AttachmentService<>(
                fileService,
                new StubAttachmentRepository(),
                new OpenAttachmentManagerProps(),
                holderName
        );
    }

    private static final class StubFileService implements FileService {

        @Override
        public String getUploadingUrl(String fileName, String extension) {
            return "https://upload.test/" + fileName + "." + extension;
        }

        @Override
        public void move(String source, String destination) {
        }

        @Override
        public void remove(String filePath) {
        }

        @Override
        public String getFileFullUrl(String fileName, String extension, String... dirs) {
            return "https://files.test/" + fileName + "." + extension;
        }

        @Override
        public byte[] getFileContent(String filePath) {
            return new byte[0];
        }

        @Override
        public void save(byte[] file, String filePath) {
        }

        @Override
        public String getTempDir() {
            return "tmp";
        }

        @Override
        public String generatePath(String fileName, String extension, String... folders) {
            return String.join("/", folders) + "/" + fileName + "." + extension;
        }
    }

    private static final class StubAttachmentRepository implements AttachmentRepository<TestAttachment> {

        @Override
        public Optional<TestAttachment> findById(UUID id) {
            return Optional.empty();
        }

        @Override
        public Collection<TestAttachment> findAllById(Set<UUID> ids) {
            return Collections.emptyList();
        }

        @Override
        public void deleteAllById(Set<UUID> removed) {
        }

        @Override
        public void saveAll(List<TestAttachment> toInsert) {
        }

        @Override
        public void save(TestAttachment attachment) {
        }

        @Override
        public Collection<TestAttachment> findAllByHolderNameAndHolderId(String holderName, String holderId) {
            return Collections.emptyList();
        }
    }

    private static final class TestAttachment implements rs.pumpkin.open_attachment_handler.model.AbstractAttachment {
        private UUID id;
        private String fileName;
        private String holderId;
        private String holderName;
        private String extension;
        private String path;
        private Instant createdAt;
        private URL url;

        @Override
        public UUID getId() {
            return id;
        }

        @Override
        public void setId(UUID newValue) {
            this.id = newValue;
        }

        @Override
        public String getFileName() {
            return fileName;
        }

        @Override
        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        @Override
        public String getHolderId() {
            return holderId;
        }

        @Override
        public void setHolderId(String holderId) {
            this.holderId = holderId;
        }

        @Override
        public String getHolderName() {
            return holderName;
        }

        @Override
        public void setHolderName(String holderName) {
            this.holderName = holderName;
        }

        @Override
        public String getExtension() {
            return extension;
        }

        @Override
        public void setExtension(String extension) {
            this.extension = extension;
        }

        @Override
        public String getPath() {
            return path;
        }

        @Override
        public void setPath(String path) {
            this.path = path;
        }

        @Override
        public void setCreatedAt(Instant time) {
            this.createdAt = time;
        }

        @Override
        public rs.pumpkin.open_attachment_handler.model.AbstractAttachment copy() {
            TestAttachment copy = new TestAttachment();
            copy.id = id;
            copy.fileName = fileName;
            copy.holderId = holderId;
            copy.holderName = holderName;
            copy.extension = extension;
            copy.path = path;
            copy.createdAt = createdAt;
            copy.url = url;
            return copy;
        }

        @Override
        public void setUrl(URL url) {
            this.url = url;
        }

        @Override
        public URL getUrl() {
            return url;
        }
    }
}
