package de.dm.retrylib;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.ClassUtils;

import java.util.Collections;
import java.util.List;

class RetryProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(RetryProcessor.class);

    private static final Integer BATCH_SIZE = 1000;

    private final RetryService retryService;

    private final List<RetryHandler> retryHandlers;

    private final RetryEntitySerializer retryEntitySerializer;

    RetryProcessor(RetryService retryService, List<RetryHandler> retryHandlers, RetryEntitySerializer retryEntitySerializer) {
        this.retryService = retryService;
        this.retryHandlers = Collections.unmodifiableList(retryHandlers);
        this.retryEntitySerializer = retryEntitySerializer;
    }

    @Scheduled(fixedRateString = "${retrylib.retryIntervalInMillis:" + RetrylibProperties.DEFAULT_RETRY_INTERVAL_IN_MILLIS + "}")
    void processNextRetryBatch() {
        LOG.debug("Scheduling the next batch of {} retryEntities...", BATCH_SIZE);
        List<RetryEntity> retryEntities = retryService.loadNextRetryEntities(BATCH_SIZE);
        int totalCount = retryEntities.size();
        int currentIndex = 1;
        LOG.info("Processing {} retryEntities...", totalCount);

        for (RetryEntity retryEntity : retryEntities) {
            if (LOG.isInfoEnabled()) {
                LOG.info("Processing retryEntity {} of {}: {}", currentIndex++, totalCount, retryEntitySerializer.serialize(retryEntity));
            }
        }

        retryEntities.forEach(this::processRetryEntity);
    }

    @SuppressWarnings("unchecked")
    private void processRetryEntity(RetryEntity retryEntity) {
        RetryHandler retryHandler = getRetryHandlerForType(retryEntity.getRetryType());
        retryHandler.handleWithRetry(retryEntity.getPayload());
    }

    private RetryHandler<?> getRetryHandlerForType(Class retryType) {
        return retryHandlers
                .stream()
                .filter(it -> retryType.equals(ClassUtils.getUserClass(it.getClass())))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No retryHandler found for type " + retryType));
    }

}
