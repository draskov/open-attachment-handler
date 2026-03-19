package rs.pumpkin.open_attachment_handler.storage;


public interface FileService {

    String getUploadingUrl(String fileName, String extension);

    void move(String source, String destination);

    void remove(String filePath);

    String getFileFullUrl(String fileName, String extension, String... dirs);

    byte[] getFileContent(String filePath);

    void save(byte[] file, String filePath);

    String getTempDir();

    String generatePath(String fileName, String extension, String... folders);
}
