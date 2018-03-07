package de.dm.retrylib;

import java.io.Serializable;

public class RetryEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String key;

    private final String retryType;

    private final String payload;

    public RetryEntity(String key, String retryType, String payload) {
        this.key = key;
        this.retryType = retryType;
        this.payload = payload;
    }

    public String getKey() {
        return key;
    }

    public String getRetryType() {
        return retryType;
    }

    public String getPayload() {
        return payload;
    }

    @Override
    public String toString() {
        return "RetryEntity{" +
                "key='" + key + '\'' +
                ", retryType='" + retryType + '\'' +
                ", payload='" + payload + '\'' +
                '}';
    }

}

