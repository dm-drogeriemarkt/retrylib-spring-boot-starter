package de.dm.retrylib;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RetryServiceUnitTest {

    private RetryService retryService;
    private ObjectMapper objectMapper = mock(ObjectMapper.class);
    private RetrylibProperties retrylibProperties = mock(RetrylibProperties.class);

    @Before
    public void setUp() {
        retryService = new RetryService(objectMapper, new LinkedBlockingQueue<>(), retrylibProperties);
    }

    @Test(expected = IllegalStateException.class)
    public void queueForRetryThrowsIllegalStateExceptionOnLimitReached() {
        //TODO
        retryService.queueForRetry("retryType", "payload");
    }


    @Test
    public void queueForRetryWritesToMapSuccessFully() throws JsonProcessingException {
        String retryType = "retryType";
        String payload = "payload";
        String payloadAsString = objectMapper.writeValueAsString(payload);
        //TODO

        retryService.queueForRetry(retryType, payload);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<RetryEntity> valueCaptor = ArgumentCaptor.forClass(RetryEntity.class);

        String savedKey = keyCaptor.getValue();
        assertThat(savedKey.contains(retryType), is(true));

        RetryEntity savedValue = valueCaptor.getValue();
        assertThat(savedValue.getKey(), is(savedKey));
        assertThat(savedValue.getPayload(), is(payloadAsString));
        assertThat(savedValue.getRetryType(), is(retryType));
    }

    @Test
    public void loadNextRetryEntitiesReturnsListOfEntries() {
        HashSet entrySet = new HashSet();
        Map.Entry<String, RetryEntity> retryEntityEntry = createMapEntry();
        entrySet.add(retryEntityEntry);
        //TODO
        List<RetryEntity> retryEntities = retryService.loadNextRetryEntities(1);

        assertThat(retryEntities.size(), is(1));
    }

    @Test
    public void loadNextRetryEntitiesReturnsListOfEntriesLimitedByBatchSize() {
        HashSet entrySet = new HashSet();
        Map.Entry<String, RetryEntity> retryEntityEntry = createMapEntry();
        entrySet.add(retryEntityEntry);

        //TODO

        List<RetryEntity> retryEntities = retryService.loadNextRetryEntities(0);

        assertThat(retryEntities.size(), is(0));
    }

    private Map.Entry<String, RetryEntity> createMapEntry() {
        return new Map.Entry<String, RetryEntity>() {

            @Override
            public String getKey() {
                return null;
            }

            @Override
            public RetryEntity getValue() {
                return null;
            }

            @Override
            public RetryEntity setValue(RetryEntity value) {
                return null;
            }
        };
    }
}