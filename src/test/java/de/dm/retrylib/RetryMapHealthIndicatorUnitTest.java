package de.dm.retrylib;

import net.openhft.chronicle.map.ChronicleMap;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RetryMapHealthIndicatorUnitTest {

    private static final Long QUEUE_WARN_THRESHOLD = 0L;
    private static final Long QUEUE_DOWN_THRESHOLD = 9L;

    private RetryMapHealthIndicator retryMapHealthIndicator;

    private ChronicleMap<String, RetryEntity> retryMap = mock(ChronicleMap.class);

    @Before
    public void setUp() {
        retryMapHealthIndicator = new RetryMapHealthIndicator(retryMap, QUEUE_WARN_THRESHOLD, QUEUE_DOWN_THRESHOLD);
    }

    @Test
    public void healthWithoutMapEntriesReturnsUp() {
        when(retryMap.longSize()).thenReturn(QUEUE_WARN_THRESHOLD);

        Health health = retryMapHealthIndicator.health();
        assertThat(health.getStatus(), is(Status.UP));
        assertThat(health.getDetails().get("Elements in map"), is(QUEUE_WARN_THRESHOLD));
    }

    @Test
    public void healthWithinErrorThresholdMapEntriesReturnsWarn() {
        when(retryMap.longSize()).thenReturn(QUEUE_DOWN_THRESHOLD);

        Health health = retryMapHealthIndicator.health();
        assertThat(health.getStatus().getCode(), is("WARN"));
        assertThat(health.getDetails().get("Elements in map"), is(QUEUE_DOWN_THRESHOLD));
    }

    @Test
    public void healthWithMoreThanDownThresholdMapEntriesReturnsWarn() {
        when(retryMap.longSize()).thenReturn(QUEUE_DOWN_THRESHOLD + 1);

        Health health = retryMapHealthIndicator.health();
        assertThat(health.getStatus(), is(Status.DOWN));
        assertThat(health.getDetails().get("Elements in map"), is(QUEUE_DOWN_THRESHOLD + 1));
    }

    @Test(expected = IllegalStateException.class)
    public void healthWithWrongConfiguredThresholdsThrowsException() {
        new RetryMapHealthIndicator(retryMap, 5L, 4L);
    }
}