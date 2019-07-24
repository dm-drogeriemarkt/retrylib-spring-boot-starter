package de.dm.retrylib;

public class RetryEntity {

    private final String key;

    private final Class retryType;

    private final Object payload;

    public RetryEntity(String key, Class retryType, Object payload) {
        this.key = key;
        this.retryType = retryType;
        this.payload = payload;
    }

    public String getKey() {
        return key;
    }

    public Class getRetryType() {
        return retryType;
    }

    public Object getPayload() {
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

