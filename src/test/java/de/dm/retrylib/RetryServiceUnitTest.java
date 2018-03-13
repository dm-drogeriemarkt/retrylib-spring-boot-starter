package de.dm.retrylib;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.openhft.chronicle.map.ChronicleMap;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RetryServiceUnitTest {

    private RetryService retryService;
    private ChronicleMap chronicleMap = mock(ChronicleMap.class);
    private RetrylibProperties retrylibProperties = mock(RetrylibProperties.class);

    @Before
    public void setUp() {
        retryService = new RetryService(new ObjectMapper(), chronicleMap, retrylibProperties);
    }

    @Test
    public void queueForRetry() {
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
}