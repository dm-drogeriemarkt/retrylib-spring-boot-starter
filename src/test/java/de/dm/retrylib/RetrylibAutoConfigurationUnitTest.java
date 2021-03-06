package de.dm.retrylib;

import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.concurrent.LinkedBlockingQueue;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class RetrylibAutoConfigurationUnitTest {

    private RetrylibAutoConfiguration retrylibAutoConfiguration;

    @Before
    public void setUp() {
        retrylibAutoConfiguration = new RetrylibAutoConfiguration();
    }

    @Test
    public void createRetryServiceBeanSuccessfully() {
        RetryService retryService = retrylibAutoConfiguration.retryService(new LinkedBlockingQueue<>(), mock(RetryEntitySerializer.class));
        assertThat(retryService, notNullValue());
    }

    @Test
    public void createRetryProcessorBeanSuccessfully() {
        RetryProcessor retryProcessor = retrylibAutoConfiguration.retryProcessor(mock(RetryService.class), Collections.emptyList(), mock(RetryEntitySerializer.class));
        assertThat(retryProcessor, notNullValue());
    }

    @Test
    public void createNoOpRetryHandlerBeanSuccessfully() {
        RetryHandler noOpRetryHandler = retrylibAutoConfiguration.noOpRetryHandler();
        assertThat(noOpRetryHandler, notNullValue());
    }

    @Test
    public void createRetryAspectBeanSuccessfully() {
        RetryAspect retryAspect = retrylibAutoConfiguration.retryAspect(mock(RetryService.class));
        assertThat(retryAspect, notNullValue());
    }
}