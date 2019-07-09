package de.dm.retrylib;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

@Configuration
@EnableScheduling
@EnableConfigurationProperties(RetrylibProperties.class)
public class RetrylibAutoConfiguration {

    @Autowired
    private RetrylibProperties retrylibProperties;

    @ConditionalOnMissingBean(RetryService.class)
    @Bean
    public RetryService retryService() {
        LinkedBlockingQueue<RetryEntity> retryEntities = new LinkedBlockingQueue<>(5);
        return new RetryService(retryEntities);
    }

    @ConditionalOnMissingBean(RetryProcessor.class)
    @Bean
    public RetryProcessor retryProcessor(RetryService retryService, List<RetryHandler> retryHandlers) {
        return new RetryProcessor(retryService, retryHandlers);
    }

    @ConditionalOnMissingBean(RetryHandler.class)
    @Bean
    public RetryHandler noOpRetryHandler() {
        return new RetryHandler() {
            @Override
            public void handleWithRetry(Object payload) {
                // Noop implementation
            }

            @Override
            public String retryType() {
                return "";
            }
        };
    }

    @ConditionalOnMissingBean(RetryAspect.class)
    @Bean
    public RetryAspect retryAspect(RetryService retryService) {
        return new RetryAspect(retryService);
    }

}
