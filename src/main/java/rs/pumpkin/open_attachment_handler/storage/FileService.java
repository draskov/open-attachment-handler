package rs.pumpkin.open_attachment_handler.storage;


public interface FileService {

    String getUploadingUrl(String fileName, String extension);

    void move(String source, String destination);

    void remove(String filePath);

    String getFileFullUrl(String fileName, String extension, String... dirs);

    /**
     * Generates a time-limited, pre-signed download URL for an already stored object,
     * identified by its full storage key/path. Unlike {@link #getFileFullUrl}, this does
     * not reconstruct the path, so it always points to the exact stored object.
     *
     * @param filePath full storage key/path of the object (e.g. an attachment's path)
     * @return pre-signed GET URL a client can be redirected to
     */
    String getDownloadUrl(String filePath);

    byte[] getFileContent(String filePath);

    void save(byte[] file, String filePath);

    String getTempDir();

    String generatePath(String fileName, String extension, String... folders);
}
