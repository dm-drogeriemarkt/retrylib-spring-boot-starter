package de.dm.retrylib;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

class ApplicationShutdownHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationShutdownHandler.class);

    private final LinkedBlockingQueue<RetryEntity> retryEntities;

    ApplicationShutdownHandler(LinkedBlockingQueue<RetryEntity> retryEntities) {
        this.retryEntities = retryEntities;
    }

    @PreDestroy
    public void onExit() throws InterruptedException {
        LOG.info("Checking retry queue for shutdown.");

        if (!retryEntities.isEmpty()) {
            List<RetryEntity> remainingEntries = new ArrayList<>();
            int remainingEntriesCount = retryEntities.drainTo(remainingEntries);
            LOG.info("{} retry entries remained during application shutdown: {}", remainingEntriesCount, remainingEntries.toString());
        } else {
            LOG.info("Retry queue is empty, shutting down.");
        }
    }
}
