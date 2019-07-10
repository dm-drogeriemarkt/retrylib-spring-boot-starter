package de.dm.retrylib;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Collections;
import java.util.List;

class RetryProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(RetryProcessor.class);

    private static final Integer BATCH_SIZE = 200;

    private final RetryService retryService;

    private final List<RetryHandler> retryHandlers;

    RetryProcessor(RetryService retryService, List<RetryHandler> retryHandlers) {
        this.retryService = retryService;
        this.retryHandlers = Collections.unmodifiableList(retryHandlers);
    }

    @Scheduled(fixedRate = 60000)
    void processNextRetryBatch() {
        LOG.info("Scheduling the next batch of {} retry entries...", BATCH_SIZE);
        List<RetryEntity> retryBatch = retryService.loadNextRetryEntities(BATCH_SIZE);
        retryBatch.forEach(this::processRetryEntity);
    }

    @SuppressWarnings("unchecked")
    private void processRetryEntity(RetryEntity retryEntity) {
        LOG.info("Processing retry entry {}", retryEntity);
        RetryHandler retryHandler = getRetryHandlerForType(retryEntity.getRetryType());
        retryHandler.handleWithRetry(retryEntity.getPayload());
    }

    private RetryHandler<?> getRetryHandlerForType(Class retryType) {
        return retryHandlers
                .stream()
                .filter(it -> retryType.equals(it.getClass()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No retryHandler found for type " + retryType));
    }

}
