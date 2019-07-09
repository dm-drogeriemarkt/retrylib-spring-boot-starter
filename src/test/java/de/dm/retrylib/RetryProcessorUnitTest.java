package de.dm.retrylib;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class RetryProcessorUnitTest {

    private RetryProcessor retryProcessor;
    private List<RetryHandler> retryHandlers;

    private RetryService retryService = mock(RetryService.class);
    private RetryHandler<String> validRetryHandler = new RetryHandler<String>() {
        @Override
        public void handleWithRetry(String payload) {
        }
    };

    @Before
    public void setUp() {
        validRetryHandler = spy(validRetryHandler);

        retryHandlers = new ArrayList<>();
        retryHandlers.add(validRetryHandler);
        retryProcessor = new RetryProcessor(retryService, retryHandlers);
    }

    @Test(expected = IllegalArgumentException.class)
    public void processNextRetryBatchThrowsExceptionWhenNoHandlerIsAvailable() {
        RetryEntity retryEntity = new RetryEntity("key", String.class, "payload");
        List<RetryEntity> retryEntities = Collections.singletonList(retryEntity);

        when(retryService.loadNextRetryEntities(anyInt())).thenReturn(retryEntities);

        retryProcessor.processNextRetryBatch();

        verifyZeroInteractions(retryService);
    }

    @Test
    public void processNextRetryBatchTriggersRetry() {
        RetryEntity retryEntity1 = new RetryEntity("key1", validRetryHandler.getClass(), "payload1");
        RetryEntity retryEntity2 = new RetryEntity("key2", validRetryHandler.getClass(), "payload2");
        List<RetryEntity> retryEntities = Arrays.asList(retryEntity1, retryEntity2);

        when(retryService.loadNextRetryEntities(anyInt())).thenReturn(retryEntities);

        retryProcessor.processNextRetryBatch();

        verify(validRetryHandler).handleWithRetry("payload1");
        verify(validRetryHandler).handleWithRetry("payload2");
    }

}