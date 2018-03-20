package de.dm.retrylib;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(prefix = "retrylib")
public class RetrylibProperties {

    @NestedConfigurationProperty
    private PersistenceProperties persistence = new PersistenceProperties();

    public PersistenceProperties getPersistence() {
        return persistence;
    }

    public void setPersistence(PersistenceProperties persistence) {
        this.persistence = persistence;
    }

    @NestedConfigurationProperty
    private HealthProperties healthProperties = new HealthProperties();

    public HealthProperties getHealthProperties() {
        return healthProperties;
    }

    public void setHealthProperties(HealthProperties healthProperties) {
        this.healthProperties = healthProperties;
    }
}
