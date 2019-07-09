package de.dm.retrylib;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(prefix = "retrylib")
public class RetrylibProperties {

    @NestedConfigurationProperty
    private HealthProperties healthProperties = new HealthProperties();

    public HealthProperties getHealthProperties() {
        return healthProperties;
    }

    public void setHealthProperties(HealthProperties healthProperties) {
        this.healthProperties = healthProperties;
    }
}
