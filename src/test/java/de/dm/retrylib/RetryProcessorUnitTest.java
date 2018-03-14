package de.dm.retrylib;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
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

        @Override
        public String retryType() {
            return "retryType";
        }
    };

    private ObjectMapper objectMapper = mock(ObjectMapper.class);

    @Before
    public void setUp() {
        validRetryHandler = spy(validRetryHandler);

        retryHandlers = new ArrayList<>();
        retryHandlers.add(validRetryHandler);
        retryProcessor = new RetryProcessor(retryService, retryHandlers, objectMapper);
    }

    @Test(expected = IllegalArgumentException.class)
    public void processNextRetryBatchThrowsExceptionWhenNoHandlerIsAvailable() {
        RetryEntity retryEntity = new RetryEntity("key", "retryTypeNotAvailable", "payload");
        List<RetryEntity> retryEntities = Collections.singletonList(retryEntity);

        when(retryService.loadNextRetryEntities(anyInt())).thenReturn(retryEntities);

        retryProcessor.processNextRetryBatch();

        verifyZeroInteractions(retryService);
    }

    @Test
    public void processNextRetryBatchTriggersRetry() {
        RetryEntity retryEntity = new RetryEntity("key", validRetryHandler.retryType(), "payload");
        List<RetryEntity> retryEntities = Collections.singletonList(retryEntity);

        when(retryService.loadNextRetryEntities(anyInt())).thenReturn(retryEntities);
        retryProcessor.processNextRetryBatch();

        verify(retryService).deleteRetryEntity(retryEntity);
        verify(validRetryHandler).handleWithRetry(any());
    }


    @Test(expected = IllegalArgumentException.class)
    public void processNextRetryBatchThrowsExceptionWhenDeserializationHasFailed() throws Exception {
        RetryEntity retryEntity = new RetryEntity("key", validRetryHandler.retryType(), "payload");
        List<RetryEntity> retryEntities = Collections.singletonList(retryEntity);

        when(retryService.loadNextRetryEntities(anyInt())).thenReturn(retryEntities);
        when(objectMapper.readValue(anyString(), eq(String.class))).thenThrow(new IOException("provoked exception"));

        retryProcessor.processNextRetryBatch();
    }

}