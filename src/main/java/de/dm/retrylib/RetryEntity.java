package de.dm.retrylib;

import java.io.Serializable;

class RetryEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String key;

    private final Class retryType;

    private final Object payload;

    RetryEntity(String key, Class retryType, Object payload) {
        this.key = key;
        this.retryType = retryType;
        this.payload = payload;
    }

    String getKey() {
        return key;
    }

    Class getRetryType() {
        return retryType;
    }

    Object getPayload() {
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

