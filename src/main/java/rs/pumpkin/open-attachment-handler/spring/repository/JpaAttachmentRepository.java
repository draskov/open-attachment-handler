package com.computerrock.attachmentmanager.spring.repository;
import com.computerrock.attachmentmanager.spring.config.properties.DatabaseProperties;
import com.computerrock.attachmentmanager.spring.entity.Attachment;
import com.computerrock.attachmentmanager.ports.AttachmentHolder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import java.util.Set;
import java.util.UUID;
@ConditionalOnProperty(prefix = "uploader.database", name = "type", havingValue = DatabaseProperties.JPA_TYPE)
@Repository
public interface JpaAttachmentRepository<H extends AttachmentHolder, A extends Attachment<H>> extends com.computerrock.attachmentmanager.spring.repository.AttachmentRepository<A, H> {

    void deleteAllByIdIn(Set<UUID> removed);
}
