package de.dm.retrylib;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "retrylib")
class RetrylibProperties {

    static final int DEFAULT_QUEUE_LIMIT = 100000;
    static final int DEFAULT_RETRY_INTERVAL_IN_MILLIS = 60000;

    /**
     * The maximum capacity for initialization of the in-memory queue.
     */
    private int queueLimit = DEFAULT_QUEUE_LIMIT;

    /**
     * The interval in milliseconds that is used to process a retry batch.
     */
    long retryIntervalInMillis = DEFAULT_RETRY_INTERVAL_IN_MILLIS;

    long getRetryIntervalInMillis() {
        return retryIntervalInMillis;
    }

    void setRetryIntervalInMillis(long retryIntervalInMillis) {
        this.retryIntervalInMillis = retryIntervalInMillis;
    }

    int getQueueLimit() {
        return queueLimit;
    }

    void setQueueLimit(int queueLimit) {
        this.queueLimit = queueLimit;
    }
}
