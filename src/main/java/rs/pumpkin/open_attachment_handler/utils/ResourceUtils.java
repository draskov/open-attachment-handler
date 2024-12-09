package rs.pumpkin.open_attachment_handler.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;
import rs.pumpkin.open_attachment_handler.exception.InternalException;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ResourceUtils {
    public static Map<String, InputStream> convertToInputStreamMap(Collection<Resource> resources){
        var map = new HashMap<String,InputStream>();
        var duplicateNameCounterMap = new HashMap<String,Integer>();
        resources.forEach(resource -> {
            var fileName = Objects.requireNonNull(resource.getFilename());
            if (map.containsKey(fileName)) {
                duplicateNameCounterMap.put(
                        fileName,
                        duplicateNameCounterMap.getOrDefault(fileName, 0) + 1
                );
                fileName =  FileUtils.formatDuplicateFileName(fileName,duplicateNameCounterMap.get(fileName));
            }

            try {
                map.put(fileName,resource.getInputStream());
            }catch (IOException ex){
                throw new InternalException(
                        "I/O exception occurred when getting Resource input stream. Exception message: "+ex.getMessage(),
                        ex
                );
            }

        });
        return map;
    }


    public static <T extends AbstractResource> Resource convertWithCustomName(
            @NonNull String fileName,
            @NonNull T resource){
        return new AbstractResource() {
            @Override
            public String getFilename() {
                return Optional.of(fileName)
                        .filter(StringUtils::isNotBlank)
                        .orElse("default_filename");
            }
            @Override
            @NonNull
            public String getDescription() {
                return Optional.of(resource.getDescription())
                        .filter(StringUtils::isNotBlank)
                        .orElse("Default file description");
            }
            @Override
            @NonNull
            public InputStream getInputStream() throws IOException {
                return resource.getInputStream();
            }
        };
    }
}
