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

    private final RetryEntitySerializer retryEntitySerializer;

    RetryService(LinkedBlockingQueue<RetryEntity> retryEntities, RetryEntitySerializer retryEntitySerializer) {
        this.retryEntities = retryEntities;
        this.retryEntitySerializer = retryEntitySerializer;
    }

    synchronized void queueForRetry(Class retryType, Object payload) {
        String key = UUID.randomUUID().toString();
        RetryEntity retryEntity = new RetryEntity(key, retryType, payload);

        retryEntities.add(retryEntity);
        if (LOG.isInfoEnabled()) {
            LOG.info("Queued for retry: {}", retryEntitySerializer.serialize(retryEntity));
        }
    }

    List<RetryEntity> loadNextRetryEntities(Integer batchSize) {
        List<RetryEntity> retryBatch = new ArrayList<>();
        this.retryEntities.drainTo(retryBatch, batchSize);
        return retryBatch;
    }

}
