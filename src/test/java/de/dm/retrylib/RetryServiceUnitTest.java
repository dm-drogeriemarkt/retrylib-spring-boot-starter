package de.dm.retrylib;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.openhft.chronicle.map.ChronicleMap;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RetryServiceUnitTest {

    private RetryService retryService;
    private ObjectMapper objectMapper;
    private ChronicleMap chronicleMap = mock(ChronicleMap.class);
    private RetrylibProperties retrylibProperties = mock(RetrylibProperties.class);

    @Before
    public void setUp() {
        objectMapper = new ObjectMapper();
        retryService = new RetryService(objectMapper, chronicleMap, retrylibProperties);
    }

    @Test(expected = IllegalStateException.class)
    public void queueForRetryThrowsIllegalStateExceptionOnLimitReached() {
        RetrylibProperties.PersistenceProperties persistenceProperties = mock(RetrylibProperties.PersistenceProperties.class);
        when(retrylibProperties.getPersistence()).thenReturn(persistenceProperties);
        when(persistenceProperties.getMaxEntries()).thenReturn(5L);
        when(chronicleMap.longSize()).thenReturn(5L);

        retryService.queueForRetry("retryType", "payload");
    }

    @Test
    public void queueForRetryWritesToMapSuccessFully() throws JsonProcessingException {
        String retryType = "retryType";
        String payload = "payload";
        String payloadAsString = objectMapper.writeValueAsString(payload);

        RetrylibProperties.PersistenceProperties persistenceProperties = mock(RetrylibProperties.PersistenceProperties.class);
        when(retrylibProperties.getPersistence()).thenReturn(persistenceProperties);
        when(persistenceProperties.getMaxEntries()).thenReturn(1L);
        when(chronicleMap.longSize()).thenReturn(5L);

        retryService.queueForRetry(retryType, payload);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<RetryEntity> valueCaptor = ArgumentCaptor.forClass(RetryEntity.class);
        verify(chronicleMap).putIfAbsent(keyCaptor.capture(), valueCaptor.capture());

        String savedKey = keyCaptor.getValue();
        assertThat(savedKey.contains(retryType), is(true));

        RetryEntity savedValue = valueCaptor.getValue();
        assertThat(savedValue.getKey(), is(savedKey));
        assertThat(savedValue.getPayload(), is(payloadAsString));
        assertThat(savedValue.getRetryType(), is(retryType));
    }

    @Test
    public void deleteRetryEntityCallsChronicleMap() {
        RetryEntity retryEntity = new RetryEntity("key", "retryType", "payload");

        retryService.deleteRetryEntity(retryEntity);

        verify(chronicleMap).remove(retryEntity.getKey());
    }

    @Test
    public void loadNextRetryEntitiesReturnsEmptyListOnClosedMap() {
        when(chronicleMap.isOpen()).thenReturn(false);

        List<RetryEntity> retryEntities = retryService.loadNextRetryEntities(5);

        assertThat(retryEntities.size(), is(0));
    }

    @Test
    public void loadNextRetryEntitiesReturnsListOfEntries() {
        HashSet entrySet = new HashSet();
        Map.Entry<String, RetryEntity> retryEntityEntry = createMapEntry();
        entrySet.add(retryEntityEntry);

        when(chronicleMap.isOpen()).thenReturn(true);
        when(chronicleMap.entrySet()).thenReturn(entrySet);

        List<RetryEntity> retryEntities = retryService.loadNextRetryEntities(1);

        assertThat(retryEntities.size(), is(1));
    }

    @Test
    public void loadNextRetryEntitiesReturnsListOfEntriesLimitedByBatchSize() {
        HashSet entrySet = new HashSet();
        Map.Entry<String, RetryEntity> retryEntityEntry = createMapEntry();
        entrySet.add(retryEntityEntry);

        when(chronicleMap.isOpen()).thenReturn(true);
        when(chronicleMap.entrySet()).thenReturn(entrySet);

        List<RetryEntity> retryEntities = retryService.loadNextRetryEntities(0);

        assertThat(retryEntities.size(), is(0));
    }

    @NotNull
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