package de.dm.retrylib;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class RetryProcessorUnitTest {

    private RetryProcessor retryProcessor;
    private List<RetryHandler> retryHandlers;

    private RetryService retryService = mock(RetryService.class);
    private RetryHandler<String> validRetryHandler = new RetryHandler<String>() {
        @Override
        public void handleWithRetry(String payload) {
        }
    };
    private RetryEntitySerializer retryEntitySerializer = mock(RetryEntitySerializer.class);

    @BeforeEach
    void setUp() {
        validRetryHandler = spy(validRetryHandler);

        retryHandlers = new ArrayList<>();
        retryHandlers.add(validRetryHandler);
        retryProcessor = new RetryProcessor(retryService, retryHandlers, retryEntitySerializer);
    }

    @Test
    void processNextRetryBatchThrowsExceptionWhenNoHandlerIsAvailable() {
        RetryEntity retryEntity = new RetryEntity("key", String.class, "payload");
        List<RetryEntity> retryEntities = Collections.singletonList(retryEntity);

        when(retryService.loadNextRetryEntities(anyInt())).thenReturn(retryEntities);

        Assertions.assertThrows(IllegalArgumentException.class, () -> retryProcessor.processNextRetryBatch());

        verify(retryService).loadNextRetryEntities(anyInt());
        verifyNoMoreInteractions(retryService);
    }

    @Test
    void processNextRetryBatchTriggersRetry() {
        RetryEntity retryEntity1 = new RetryEntity("key1", validRetryHandler.getClass(), "payload1");
        RetryEntity retryEntity2 = new RetryEntity("key2", validRetryHandler.getClass(), "payload2");
        List<RetryEntity> retryEntities = Arrays.asList(retryEntity1, retryEntity2);

        when(retryService.loadNextRetryEntities(anyInt())).thenReturn(retryEntities);

        retryProcessor.processNextRetryBatch();

        verify(validRetryHandler).handleWithRetry("payload1");
        verify(validRetryHandler).handleWithRetry("payload2");
    }

}