package de.dm.retrylib;

import org.junit.Test;

import java.util.concurrent.LinkedBlockingQueue;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class ApplicationShutdownHandlerUnitTest {

    private final LinkedBlockingQueue<RetryEntity> retryEntities = new LinkedBlockingQueue<>();
    private final RetryEntitySerializer retryEntitySerializer = mock(RetryEntitySerializer.class);
    private final ApplicationShutdownHandler applicationShutdownHandler = new ApplicationShutdownHandler(retryEntities, retryEntitySerializer);

    @Test
    public void onExitLogsRemainingRetryEntities() {
        RetryEntity retryEntity1 = new RetryEntity("key1", ExternalServiceRetryHandler.class, "payload1");
        RetryEntity retryEntity2 = new RetryEntity("key2", ExternalServiceRetryHandler.class, "payload2");
        retryEntities.add(retryEntity1);
        retryEntities.add(retryEntity2);

        applicationShutdownHandler.onExit();

        verify(retryEntitySerializer).serialize(retryEntity1);
        verify(retryEntitySerializer).serialize(retryEntity2);
    }

    @Test
    public void onExitSkipsLoggingIfNoRetryEntitiesArePending() throws InterruptedException {
        applicationShutdownHandler.onExit();

        verify(retryEntitySerializer, never()).serialize(any(RetryEntity.class));
    }
}