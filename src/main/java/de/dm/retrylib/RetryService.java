package de.dm.retrylib;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

class RetryService {

    private static final Logger LOG = LoggerFactory.getLogger(RetryService.class);

    private final LinkedBlockingQueue<RetryEntity> retryEntities;

    RetryService(LinkedBlockingQueue<RetryEntity> retryEntities) {
        this.retryEntities = retryEntities;
    }

    synchronized void queueForRetry(Class retryType, Object payload) {
        String key = UUID.randomUUID().toString();
        RetryEntity retryEntity = new RetryEntity(key, retryType, payload);

        retryEntities.add(retryEntity);
        LOG.info("Queued for retry: {}", retryEntity);
    }

    List<RetryEntity> loadNextRetryEntities(Integer batchSize) {
        List<RetryEntity> retryBatch = new ArrayList<>();
        this.retryEntities.drainTo(retryBatch, batchSize);
        return retryBatch;
    }

}
