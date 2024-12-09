package com.computerrock.attachmentmanager.spring.entity;
import com.computerrock.attachmentmanager.model.AbstractAttachment;
import com.computerrock.attachmentmanager.ports.AttachmentHolder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;


import java.io.Serial;
import java.io.Serializable;
import java.security.SecureRandom;
import java.util.UUID;


@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public abstract class AttachmentCassandra<H extends AttachmentHolder> extends AbstractAttachment<H> implements Serializable {

    @Serial
    private static final long serialVersionUID = new SecureRandom().nextLong();

    @Id
    @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED)
    private UUID id;

    @Column(isStatic = true)
    H holder;

    @Column(isStatic = true)
    private String extension;
    @Column
    private String fileName;

}
