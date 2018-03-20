package de.dm.retrylib;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.openhft.chronicle.map.ChronicleMap;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.StringUtils;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RetrylibAutoConfigurationUnitTest {

    private RetrylibAutoConfiguration retrylibAutoConfiguration;
    private ObjectMapper objectMapper;

    private ChronicleMap<String, RetryEntity> chronicleMap = mock(ChronicleMap.class);

    @Before
    public void setUp() {
        objectMapper = new ObjectMapper();
        retrylibAutoConfiguration = new RetrylibAutoConfiguration();
    }

    @Test
    public void createRetryServiceBeanSuccessfully() {
        RetryService retryService = retrylibAutoConfiguration.retryService(objectMapper, chronicleMap);
        assertThat(retryService, notNullValue());
    }

    @Test
    public void createRetryProcessorBeanSuccessfully() {
        RetryProcessor retryProcessor = retrylibAutoConfiguration.retryProcessor(mock(RetryService.class), Collections.emptyList(), objectMapper);
        assertThat(retryProcessor, notNullValue());
    }

    @Test
    public void createNoOpRetryHandlerBeanSuccessfully() {
        RetryHandler noOpRetryHandler = retrylibAutoConfiguration.noOpRetryHandler();
        assertThat(noOpRetryHandler, notNullValue());

        String retryType = noOpRetryHandler.retryType();
        assertThat(StringUtils.isEmpty(retryType), is(true));
    }

    @Test
    public void createRetryAspectBeanSuccessfully() {
        RetryAspect retryAspect = retrylibAutoConfiguration.retryAspect(mock(RetryService.class));
        assertThat(retryAspect, notNullValue());
    }

    @Test
    public void createRetryMapConfigurerBeanSuccessfully() {
        assertThat(retrylibAutoConfiguration.retryMapConfigurer(), notNullValue());
    }

    @Test
    public void createRetryMapBeanSuccessfully() throws Exception {
        RetryMapConfigurer retryMapConfigurer = mock(RetryMapConfigurer.class);
        when(retryMapConfigurer.configureChronicleMap()).thenReturn(chronicleMap);

        assertThat(retrylibAutoConfiguration.retryMap(retryMapConfigurer), is(chronicleMap));
    }

}