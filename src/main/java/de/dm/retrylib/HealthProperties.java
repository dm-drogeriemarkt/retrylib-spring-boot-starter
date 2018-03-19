package de.dm.retrylib;

public class HealthProperties {

    private static final Long QUEUE_ERROR_THRESHOLD = 9L;
    private static final Long QUEUE_WARN_THRESHOLD = 0L;

    /**
     * The amount of unprocessed retry messages that can be in the map before the HealthIndicator will
     * return Status.DOWN
     */
    private Long queueErrorThreshold = QUEUE_ERROR_THRESHOLD;
    /**
     * The amount of unprocessed retry messages that can be in the map before the HealthIndicator will
     * return Status.WARN
     */
    private Long queueWarnThreshold = QUEUE_WARN_THRESHOLD;

    public Long getQueueWarnThreshold() {
        return queueWarnThreshold;
    }

    public void setQueueWarnThreshold(Long queueWarnThreshold) {
        this.queueWarnThreshold = queueWarnThreshold;
    }

    public Long getQueueErrorThreshold() {
        return queueErrorThreshold;
    }

    public void setQueueErrorThreshold(Long queueErrorThreshold) {
        this.queueErrorThreshold = queueErrorThreshold;
    }

}
