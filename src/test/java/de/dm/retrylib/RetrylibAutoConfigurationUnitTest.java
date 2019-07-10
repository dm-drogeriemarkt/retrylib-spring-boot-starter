package de.dm.retrylib;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class RetrylibAutoConfigurationUnitTest {

    private RetrylibAutoConfiguration retrylibAutoConfiguration;
    private ObjectMapper objectMapper;

    @Before
    public void setUp() {
        objectMapper = new ObjectMapper();
        retrylibAutoConfiguration = new RetrylibAutoConfiguration();
    }

    @Test
    public void createRetryServiceBeanSuccessfully() {
        RetryService retryService = retrylibAutoConfiguration.retryService();
        assertThat(retryService, notNullValue());
    }

    @Test
    public void createRetryProcessorBeanSuccessfully() {
        RetryProcessor retryProcessor = retrylibAutoConfiguration.retryProcessor(mock(RetryService.class), Collections.emptyList());
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