package rs.pumpkin.open_attachment_handler.ports;

import java.util.*;
import java.util.stream.StreamSupport;

public interface AttachmentRepository<E, H> {

    Optional<E> findById(UUID id);

    Iterable<E> findAllById(Set<UUID> ids);

    void deleteAllById(Set<UUID> removed);

    void saveAll(List<E> toInsert);

    void save(E attachment);

    Collection<E> findAllByHolder(H holder);

}
