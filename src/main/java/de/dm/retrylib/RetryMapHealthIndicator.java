package de.dm.retrylib;


import net.openhft.chronicle.map.ChronicleMap;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;

/**
 * HealthIndicator that checks the amount of retryable entries in the Chronicle Map.
 * Returns Status.UP if queue.size() {@literal <}= queueDownThreshold and queue.size() {@literal <} queueWarnThreshold.
 * Returns Status.WARN if queueWarnThreshold {@literal <} queue.size() {@literal <}= queueDownThreshold.
 * Returns Status.DOWN in every other case.
 */

public class RetryMapHealthIndicator implements HealthIndicator {

    private static final Status WARN_STATUS = new Status("WARN");

    private final Long queueWarnThreshold;
    private final Long queueDownThreshold;
    private ChronicleMap<String, RetryEntity> retryMap;


    public RetryMapHealthIndicator(ChronicleMap<String, RetryEntity> retryMap,
                                   Long queueWarnThreshold,
                                   Long queueDownThreshold) {

        if (queueDownThreshold < queueWarnThreshold) {
            throw new IllegalStateException("The retrylib.health.queueDownThreshold should not " +
                    "be lower than retrylib.health.queueWarnThreshold");
        }

        this.queueWarnThreshold = queueWarnThreshold;
        this.queueDownThreshold = queueDownThreshold;
        this.retryMap = retryMap;
    }

    @Override
    public Health health() {
        long retryMapSize = retryMap.longSize();
        Health.Builder health = Health.up()
                .withDetail("Queue threshold", queueDownThreshold)
                .withDetail("Elements in map", retryMapSize);
        if (retryMapSize > queueWarnThreshold) {
            health.status(WARN_STATUS);
        }
        if (retryMapSize > queueDownThreshold) {
            health.down();
        }
        return health.build();
    }
}