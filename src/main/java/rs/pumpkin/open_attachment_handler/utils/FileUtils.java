package rs.pumpkin.open_attachment_handler.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import rs.pumpkin.open_attachment_handler.exception.InternalException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileUtils {

    private static final String DUPLICATE_FILE_NAME_BASE_FORMAT = "%s-Copy(%d)";
    private static final String DUPLICATE_FILE_NAME_EXTENSION_FORMAT = DUPLICATE_FILE_NAME_BASE_FORMAT + ".%s";

    public static String getExtension(String filename) {
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }


    public static String formatDuplicateFileName(String fileName, int index) {
        if (!haveExtension(fileName)) {
            return String.format(DUPLICATE_FILE_NAME_BASE_FORMAT, fileName, index);
        }
        String baseName = fileName.substring(0, fileName.lastIndexOf("."));
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
        return String.format(DUPLICATE_FILE_NAME_EXTENSION_FORMAT, baseName, index, extension);
    }

    public static boolean haveExtension(String fileName) {
        return fileName.lastIndexOf(".") > -1;
    }

    public static <T extends OutputStream> T zipFiles(Map<String, InputStream> input, T out) {
        var fileName = "";
        try {
            var zipOut = new ZipOutputStream(out);
            InputStream inputStream = null;
            for(Map.Entry<String, InputStream> mapEntry : input.entrySet()){
                fileName = mapEntry.getKey();
                inputStream = mapEntry.getValue();
                zipOut.putNextEntry(new ZipEntry(fileName));
                zipOut.write(inputStream.readAllBytes());
                zipOut.closeEntry();
                inputStream.close();
            }
            zipOut.close();
        }catch (IOException ex){
            throw new InternalException(String.format(
                    "Error zipping file: %s Exception message: %s",
                    fileName,
                    ex.getMessage()
            ), ex);
        }

        return out;
    }
}
