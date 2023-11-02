package de.dm.retrylib;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.concurrent.LinkedBlockingQueue;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

class RetrylibAutoConfigurationUnitTest {

    private RetrylibAutoConfiguration retrylibAutoConfiguration;

    @BeforeEach
    void setUp() {
        retrylibAutoConfiguration = new RetrylibAutoConfiguration();
    }

    @Test
    void createRetryServiceBeanSuccessfully() {
        RetryService retryService = retrylibAutoConfiguration.retryService(new LinkedBlockingQueue<>(), mock(RetryEntitySerializer.class));
        assertThat(retryService, notNullValue());
    }

    @Test
    void createRetryProcessorBeanSuccessfully() {
        RetryProcessor retryProcessor = retrylibAutoConfiguration.retryProcessor(mock(RetryService.class), Collections.emptyList(), mock(RetryEntitySerializer.class));
        assertThat(retryProcessor, notNullValue());
    }

    @Test
    void createNoOpRetryHandlerBeanSuccessfully() {
        RetryHandler noOpRetryHandler = retrylibAutoConfiguration.noOpRetryHandler();
        assertThat(noOpRetryHandler, notNullValue());
    }

    @Test
    void createRetryAspectBeanSuccessfully() {
        RetryAspect retryAspect = retrylibAutoConfiguration.retryAspect(mock(RetryService.class));
        assertThat(retryAspect, notNullValue());
    }
}