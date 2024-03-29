package de.dm.retrylib;

import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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
            int totalCount = retryEntities.size();
            int currentIndex = 1;
            for (RetryEntity retryEntity : retryEntities) {
                LOG.error("RetryEntity {} of {}: {}", currentIndex++, totalCount, retryEntitySerializer.serialize(retryEntity));
            }
        } else {
            LOG.info("Retry queue is empty, shutting down.");
        }
    }
}
