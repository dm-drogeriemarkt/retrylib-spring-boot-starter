package de.dm.retrylib;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

class RetryServiceUnitTest {

    private RetryService retryService;

    private RetryEntitySerializer retryEntitySerializer = mock(RetryEntitySerializer.class);

    private RetryHandler<String> demoRetryHandler = new RetryHandler<String>() {
        @Override
        public void handleWithRetry(String payload) {
        }
    };
    private LinkedBlockingQueue<RetryEntity> retryEntities;

    @BeforeEach
    void setUp() {
        retryEntities = new LinkedBlockingQueue<>(5);
        retryService = new RetryService(retryEntities, retryEntitySerializer);
    }

    @Test
    void queueForRetryThrowsIllegalStateExceptionOnLimitReached() {
        LinkedBlockingQueue<RetryEntity> retryEntities = new LinkedBlockingQueue<>(1);
        retryService = new RetryService(retryEntities, retryEntitySerializer);

        retryService.queueForRetry(demoRetryHandler.getClass(), "payload1");

        Assertions.assertThrows(IllegalStateException.class, () -> retryService.queueForRetry(demoRetryHandler.getClass(), "payload2"));
    }


    @Test
    void queueForRetryWritesToQueueSuccessfully() throws InterruptedException {
        String payload = "payload";
        retryService.queueForRetry(demoRetryHandler.getClass(), payload);

        RetryEntity savedValue = retryEntities.take();
        assertThat(savedValue.getKey(), is(notNullValue()));
        assertThat(savedValue.getPayload(), is(payload));
        assertThat(savedValue.getRetryType(), equalTo(demoRetryHandler.getClass()));
    }

    @Test
    void loadNextRetryEntitiesReturnsListOfEntries() {
        String payload = "payload";
        retryService.queueForRetry(demoRetryHandler.getClass(), payload);

        List<RetryEntity> retryEntities = retryService.loadNextRetryEntities(1);

        assertThat(retryEntities.size(), is(1));
    }

    @Test
    void loadNextRetryEntitiesReturnsListOfEntriesLimitedByBatchSize() {
        retryService.queueForRetry(demoRetryHandler.getClass(), "payload1");
        retryService.queueForRetry(demoRetryHandler.getClass(), "payload2");
        retryService.queueForRetry(demoRetryHandler.getClass(), "payload3");

        List<RetryEntity> retryEntities = retryService.loadNextRetryEntities(2);

        assertThat(retryEntities.size(), is(2));
        assertThat(retryEntities.get(0).getPayload(), is("payload1"));
        assertThat(retryEntities.get(1).getPayload(), is("payload2"));
    }
}