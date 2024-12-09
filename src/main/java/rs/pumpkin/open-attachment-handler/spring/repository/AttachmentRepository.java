package com.computerrock.attachmentmanager.spring.repository;

import com.computerrock.attachmentmanager.model.AbstractAttachment;
import com.computerrock.attachmentmanager.ports.AttachmentHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import java.util.*;

public interface AttachmentRepository<A extends AbstractAttachment<H>, H extends AttachmentHolder> extends CrudRepository<A, UUID> {
    List<A> findAllByHolder(H holder);

    List<A> findAll();

    Optional<A> findById(UUID id);

    Page findAll(Pageable pageable);

    Collection<A> findByPathIsNull();

}

