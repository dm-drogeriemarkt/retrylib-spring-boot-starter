package de.dm.retrylib;

public interface RetryHandler<T> {

    void handleWithRetry(T payload);

}
