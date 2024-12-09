package com.computerrock.attachmentmanager.spring.entity;

import com.computerrock.attachmentmanager.model.AbstractAttachment;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@ToString
@MappedSuperclass
public abstract class Attachment<H> extends AbstractAttachment<H> {

    @Id
    private UUID id;

    @Column(name = "extension", nullable = false, updatable = false)
    private String extension;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

    @ManyToOne
    private H holder;

    @Column(name = "is_foreign_source", columnDefinition = "boolean default false", nullable = false)
    private boolean isForeignSource = false;

    @Column(name = "source_name")
    private String sourceName;

    @Column(name = "path")
    private String path;

    protected void copy(AbstractAttachment<H> source) {
        this.id = source.getId();
        this.extension = source.getExtension();
        this.fileName = source.getFileName();
        this.holder = source.getHolder();
        this.isForeignSource = source.isForeignSource();
        this.sourceName = source.getSourceName();
        this.path = source.getPath();
    }
}
