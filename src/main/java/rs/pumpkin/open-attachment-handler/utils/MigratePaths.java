package com.computerrock.attachmentmanager.utils;

import com.computerrock.attachmentmanager.model.AbstractAttachment;
import com.computerrock.attachmentmanager.ports.AttachmentHolder;
import com.computerrock.attachmentmanager.service.impl.AttachmentService;
import com.computerrock.attachmentmanager.spring.repository.AttachmentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Slf4j
public class MigratePaths {

    public static <A extends AbstractAttachment<H>, H extends AttachmentHolder> void migrate(
            ExecutorService executorService,
            AttachmentRepository<A, H> repository,
            AttachmentService<H, A> service

    ) {

        if (repository.findByPathIsNull().isEmpty()) {
            log.info("Migration Paths already done");
            return;
        }

        long totalNumberOfRecords = repository.count();

        int page = 0;
        int size = 100;
        int numOfPages = (int) (totalNumberOfRecords / size) + 1;

        log.info(
                "Migration of path started.Total Pages: {}, Page-Size {}, totalCount {}",
                numOfPages,
                size,
                totalNumberOfRecords
        );

        List<CompletableFuture<Boolean>> list = new ArrayList<>();
        while (page < numOfPages) {
            list.add(
                    reindex(
                            page,
                            size,
                            Sort.by(Sort.DEFAULT_DIRECTION, "id"),
                            numOfPages,
                            executorService,
                            repository,
                            service
                    )
            );
            page++;
        }

        list.forEach(CompletableFuture::join);
    }

    private static <A extends AbstractAttachment<H>, H extends AttachmentHolder> CompletableFuture<Boolean> reindex(
            int index,
            int size,
            Sort sort,
            long numOfPages,
            ExecutorService executorService,
            AttachmentRepository<A, H> repository,
            AttachmentService<H, A> service
    ) {

        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("Page {} of size {}. Total Pages {}", index, size, numOfPages);
                Pageable pageable = PageRequest.of(index, size, sort);
                Page<A> resultPage = repository.findAll(pageable);
                log.info("Page {}, num of elements: {}", index, resultPage.getNumberOfElements());
                if (resultPage.getNumberOfElements() > 0) {
                    List<? extends AbstractAttachment> contentList = resultPage.getContent();
                    contentList.forEach(
                            abstractAttachment -> abstractAttachment.setPath(
                                    abstractAttachment.isForeignSource()
                                    ? abstractAttachment.getId().toString()
                                    : service.generateRelativePath(abstractAttachment)
                            ));
                    repository.saveAll((List<A>)contentList);
                }
                log.info("Page {}, finished", index);
                return Boolean.TRUE;
            } catch (RuntimeException re) {
                log.error("Page {} failed with the message {}", index, re.getMessage());
                return Boolean.FALSE;
            }
        }, executorService);
    }
}
