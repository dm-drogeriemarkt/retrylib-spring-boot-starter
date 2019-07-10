package de.dm.retrylib;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(prefix = "retrylib")
public class RetrylibProperties {

    private static final int DEFAULT_QUEUE_LIMIT = 100000;

    /**
     * The maximum capacity for initialization of the in-memory queue
     */
    private int queueLimit = DEFAULT_QUEUE_LIMIT;

    @NestedConfigurationProperty
    private HealthProperties healthProperties = new HealthProperties();

    public int getQueueLimit() {
        return queueLimit;
    }

    public void setQueueLimit(int queueLimit) {
        this.queueLimit = queueLimit;
    }

    public HealthProperties getHealthProperties() {
        return healthProperties;
    }

    public void setHealthProperties(HealthProperties healthProperties) {
        this.healthProperties = healthProperties;
    }
}
