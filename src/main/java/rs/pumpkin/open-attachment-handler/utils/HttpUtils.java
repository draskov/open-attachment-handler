package com.computerrock.attachmentmanager.utils;

import com.computerrock.attachmentmanager.model.enums.AllowedFileType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;

import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HttpUtils {
    public static HttpHeaders getHttpHeadersForFile(@NonNull String fileName) {
        var headers = new HttpHeaders();

        fileName = Optional.of(fileName)
                .filter(StringUtils::isNotBlank)
                .orElse("default_file_name");

        var contentType = Optional.of(fileName)
                .filter(FileUtils::haveExtension)
                .map(FileUtils::getExtension)
                .map(AllowedFileType::from)
                .map(AllowedFileType::getContentType)
                .orElse(MediaType.APPLICATION_OCTET_STREAM_VALUE);

        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
        headers.add(HttpHeaders.CONTENT_TYPE, contentType);
        return headers;
    }

}
