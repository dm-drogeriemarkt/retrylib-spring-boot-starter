package de.dm.retrylib;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.LinkedBlockingQueue;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ApplicationShutdownHandlerUnitTest {

    private ApplicationShutdownHandler applicationShutdownHandler;
    private LinkedBlockingQueue<RetryEntity> retryEntities = mock(LinkedBlockingQueue.class);

    @Before
    public void setUp() throws Exception {
        applicationShutdownHandler = new ApplicationShutdownHandler(retryEntities);

    }

    @Test
    public void onExitDrainsRemainingRetryEntities() throws InterruptedException {
        when(retryEntities.isEmpty()).thenReturn(false);

        applicationShutdownHandler.onExit();

        verify(retryEntities).drainTo(anyList());
    }

    @Test
    public void onExitSkipsDrainingOfEmptyRetryList() throws InterruptedException {
        when(retryEntities.isEmpty()).thenReturn(true);

        applicationShutdownHandler.onExit();

        verify(retryEntities, times(0)).drainTo(anyList());
    }
}