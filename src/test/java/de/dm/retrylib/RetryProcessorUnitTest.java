package de.dm.retrylib;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class RetryProcessorUnitTest {

    private RetryProcessor retryProcessor;
    private ArrayList retryHandlers;

    private RetryService retryService = mock(RetryService.class);
    private RetryHandler validRetryHandler = mock(RetryHandler.class);
    private ObjectMapper objectMapper = mock(ObjectMapper.class);

    @Before
    public void setUp() {
        retryHandlers = new ArrayList();
        retryHandlers.add(validRetryHandler);
        retryProcessor = new RetryProcessor(retryService, retryHandlers, objectMapper);
    }

    @Test(expected = IllegalArgumentException.class)
    public void processNextRetryBatchThrowsExceptionWhenNoHandlerIsAvailable() {
        RetryEntity retryEntity = new RetryEntity("key", "retryTypeNotAvailable", "payload");
        List<RetryEntity> retryEntities = Arrays.asList(retryEntity);

        when(retryService.loadNextRetryEntities(anyInt())).thenReturn(retryEntities);
        when(validRetryHandler.retryType()).thenReturn("retryType");

        retryProcessor.processNextRetryBatch();

        verifyZeroInteractions(retryService);
    }

    @Test
    public void processNextRetryBatchTriggersRetry() {
        RetryEntity retryEntity = new RetryEntity("key", "retryType", "payload");
        List<RetryEntity> retryEntities = Arrays.asList(retryEntity);

        when(retryService.loadNextRetryEntities(anyInt())).thenReturn(retryEntities);
        when(validRetryHandler.retryType()).thenReturn("retryType");
        retryProcessor.processNextRetryBatch();

        verify(retryService).deleteRetryEntity(retryEntity);
        verify(validRetryHandler).handleWithRetry(any());
    }
}