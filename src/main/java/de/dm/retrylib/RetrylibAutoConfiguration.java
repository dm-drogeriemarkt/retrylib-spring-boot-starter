package de.dm.retrylib;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

@Configuration
@EnableScheduling
@EnableConfigurationProperties(RetrylibProperties.class)
public class RetrylibAutoConfiguration {

    @ConditionalOnMissingBean(RetryService.class)
    @Bean
    RetryService retryService(LinkedBlockingQueue<RetryEntity> retryEntities) {
        return new RetryService(retryEntities);
    }

    @Bean
    LinkedBlockingQueue<RetryEntity> retryEntities(MeterRegistry meterRegistry, RetrylibProperties retrylibProperties) {
        LinkedBlockingQueue<RetryEntity> retryEntities = new LinkedBlockingQueue<>(retrylibProperties.getQueueLimit());
        meterRegistry.gauge("retrylib.entriesToRetry", Collections.emptyList(), retryEntities, LinkedBlockingQueue::size);
        return retryEntities;
    }

    @ConditionalOnMissingBean(RetryProcessor.class)
    @Bean
    RetryProcessor retryProcessor(RetryService retryService, List<RetryHandler> retryHandlers) {
        return new RetryProcessor(retryService, retryHandlers);
    }

    @ConditionalOnMissingBean(RetryHandler.class)
    @Bean
    RetryHandler noOpRetryHandler() {
        return new RetryHandler() {
            @Override
            public void handleWithRetry(Object payload) {
                // Noop implementation
            }
        };
    }

    @ConditionalOnMissingBean(RetryAspect.class)
    @Bean
    RetryAspect retryAspect(RetryService retryService) {
        return new RetryAspect(retryService);
    }

}
