package de.dm.retrylib;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import static java.util.function.Predicate.isEqual;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;

public class RetryServiceUnitTest {

    private RetryService retryService;

    private RetryHandler<String> demoRetryHandler = new RetryHandler<String>() {
        @Override
        public void handleWithRetry(String payload) {
        }

        @Override
        public String retryType() {
            return "retryType";
        }
    };
    private LinkedBlockingQueue<RetryEntity> retryEntities;

    @Before
    public void setUp() {
        retryEntities = new LinkedBlockingQueue<>(5);
        retryService = new RetryService(retryEntities);
    }

    @Test(expected = IllegalStateException.class)
    public void queueForRetryThrowsIllegalStateExceptionOnLimitReached() {
        LinkedBlockingQueue<RetryEntity> retryEntities = new LinkedBlockingQueue<>(1);
        retryService = new RetryService(retryEntities);

        retryService.queueForRetry(demoRetryHandler.getClass(), "payload1");
        retryService.queueForRetry(demoRetryHandler.getClass(), "payload2");
    }


    @Test
    public void queueForRetryWritesToQueueSuccessfully() throws InterruptedException {
        String payload = "payload";
        retryService.queueForRetry(demoRetryHandler.getClass(), payload);

        RetryEntity savedValue = retryEntities.take();
        assertThat(savedValue.getKey(), is(notNullValue()));
        assertThat(savedValue.getPayload(), is(payload));
        assertThat(savedValue.getRetryType(), equalTo(demoRetryHandler.getClass()));
    }

    @Test
    public void loadNextRetryEntitiesReturnsListOfEntries() {
        String payload = "payload";
        retryService.queueForRetry(demoRetryHandler.getClass(), payload);

        List<RetryEntity> retryEntities = retryService.loadNextRetryEntities(1);

        assertThat(retryEntities.size(), is(1));
    }

    @Test
    public void loadNextRetryEntitiesReturnsListOfEntriesLimitedByBatchSize() {
        retryService.queueForRetry(demoRetryHandler.getClass(), "payload1");
        retryService.queueForRetry(demoRetryHandler.getClass(), "payload2");
        retryService.queueForRetry(demoRetryHandler.getClass(), "payload3");

        List<RetryEntity> retryEntities = retryService.loadNextRetryEntities(2);

        assertThat(retryEntities.size(), is(2));
        assertThat(retryEntities.get(0).getPayload(), is("payload1"));
        assertThat(retryEntities.get(1).getPayload(), is("payload2"));
    }
}