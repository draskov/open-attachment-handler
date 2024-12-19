package rs.pumpkin.open_attachment_handler.ports;

import java.util.*;

public interface AttachmentRepository<A, H> {

    Optional<A> findById(UUID id);

    Collection<A> findAllById(Set<UUID> ids);

    void deleteAllById(Set<UUID> removed);

    void saveAll(List<A> toInsert);

    void save(A attachment);

    Collection<A> findAllByHolder(H holder);

}
