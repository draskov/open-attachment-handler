package com.computerrock.attachmentmanager.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public enum AllowedFileType {
    JPG("image/jpeg"),
    JPEG("image/jpeg"),
    PNG("image/png"),
    XLS("application/vnd.ms-excel"),
    XLSX("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
    DOC("application/msword"),
    DOCX("application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
    ZIP("application/zip"),
    MSG("application/vnd.ms-outlook"),
    PDF("application/pdf");

    private static final Map<String, AllowedFileType> VALUE_MAP = Arrays.stream(values()).collect(Collectors.toMap(
            en -> en.name().toLowerCase(),
            Function.identity()
    ));
    @Getter
    private final String contentType;

    public static AllowedFileType from(String extension) {
        return VALUE_MAP.get(extension);
    }

    public static Set<String> getExtensions() {
        return VALUE_MAP.keySet();
    }

}
