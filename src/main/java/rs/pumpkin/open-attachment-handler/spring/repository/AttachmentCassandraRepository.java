package com.computerrock.attachmentmanager.spring.repository;
import com.computerrock.attachmentmanager.spring.config.properties.DatabaseProperties;
import com.computerrock.attachmentmanager.spring.entity.AttachmentCassandra;
import com.computerrock.attachmentmanager.ports.AttachmentHolder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.cassandra.repository.AllowFiltering;
import org.springframework.data.cassandra.repository.CassandraRepository;



import java.util.*;

@ConditionalOnProperty(prefix = "uploader.database", name = "type", havingValue = DatabaseProperties.CASSANDRA_TYPE)
public interface AttachmentCassandraRepository<E extends AttachmentCassandra<H>,H extends AttachmentHolder> extends CassandraRepository<E,UUID>, com.computerrock.attachmentmanager.spring.repository.AttachmentRepository<E, H> {
    @AllowFiltering
    @Override
    List<E> findAllByHolder(H t);
}
