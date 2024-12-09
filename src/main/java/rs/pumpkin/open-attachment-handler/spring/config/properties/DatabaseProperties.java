package com.computerrock.attachmentmanager.spring.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

@Data
@ConfigurationProperties(prefix = "uploader.database")
public class DatabaseProperties {
    private String type;

    public static final String CASSANDRA_TYPE="cassandra";
    public static final String JPA_TYPE="jpa";
    public static final String ALL_TYPE="all";


    public static Set<String> allowedTypes(){
        return Set.of(CASSANDRA_TYPE,JPA_TYPE,ALL_TYPE);
    }

    public String getType() {
        isValid();
        return type;
    }

    private void isValid() {
        var allowedValues = allowedTypes();
        if (!allowedValues.contains(type)) {
            throw new RuntimeException(String
                    .format("In application.properties" +
                            " uploader.database.type have invalid value." +
                            " Value: %s Allowed values: %s", type, allowedValues));
        }
    }
}
