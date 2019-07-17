package de.dm.retrylib;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.util.concurrent.LinkedBlockingQueue;

class ApplicationShutdownHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationShutdownHandler.class);

    private final LinkedBlockingQueue<RetryEntity> retryEntities;

    private final RetryEntitySerializer retryEntitySerializer;

    ApplicationShutdownHandler(LinkedBlockingQueue<RetryEntity> retryEntities, RetryEntitySerializer retryEntitySerializer) {
        this.retryEntities = retryEntities;
        this.retryEntitySerializer = retryEntitySerializer;
    }

    @PreDestroy
    void onExit() {
        LOG.info("Checking retry queue for shutdown.");

        if (!retryEntities.isEmpty() && LOG.isErrorEnabled()) {
            LOG.error("{} retryEntities remained during application shutdown.", retryEntities.size());

            long counter = 0L;
            for (RetryEntity retryEntity : retryEntities) {
                LOG.error("RetryEntity {}: {}", counter++, retryEntitySerializer.serialize(retryEntity));
            }
        } else {
            LOG.info("Retry queue is empty, shutting down.");
        }
    }
}
