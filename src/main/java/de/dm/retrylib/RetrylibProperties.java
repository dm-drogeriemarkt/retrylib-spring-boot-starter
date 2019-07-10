package de.dm.retrylib;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "retrylib")
public class RetrylibProperties {

    private static final int DEFAULT_QUEUE_LIMIT = 100000;

    /**
     * The maximum capacity for initialization of the in-memory queue
     */
    private int queueLimit = DEFAULT_QUEUE_LIMIT;

    public int getQueueLimit() {
        return queueLimit;
    }

    public void setQueueLimit(int queueLimit) {
        this.queueLimit = queueLimit;
    }
}
