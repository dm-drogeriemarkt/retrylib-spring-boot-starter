package de.dm.retrylib;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

@AutoConfiguration
@Configuration
@EnableScheduling
@EnableConfigurationProperties(RetrylibProperties.class)
class RetrylibAutoConfiguration {

    @Bean
    RetryService retryService(LinkedBlockingQueue<RetryEntity> retryEntities, RetryEntitySerializer retryEntitySerializer) {
        return new RetryService(retryEntities, retryEntitySerializer);
    }

    @Bean
    LinkedBlockingQueue<RetryEntity> retryEntities(MeterRegistry meterRegistry, RetrylibProperties retrylibProperties) {
        LinkedBlockingQueue<RetryEntity> retryEntities = new LinkedBlockingQueue<>(retrylibProperties.getQueueLimit());
        meterRegistry.gauge("retrylib.entitiesToRetry", Collections.emptyList(), retryEntities, LinkedBlockingQueue::size);
        return retryEntities;
    }

    @Bean
    RetryProcessor retryProcessor(RetryService retryService, List<RetryHandler> retryHandlers, RetryEntitySerializer retryEntitySerializer) {
        return new RetryProcessor(retryService, retryHandlers, retryEntitySerializer);
    }

    @ConditionalOnMissingBean(RetryHandler.class)
    @Bean
    RetryHandler noOpRetryHandler() {
        return payload -> {
            // Noop implementation
        };
    }

    @Bean
    RetryAspect retryAspect(RetryService retryService) {
        return new RetryAspect(retryService);
    }

    @Bean
    RetryEntitySerializer retryEntitySerializer(ObjectMapper objectMapper) {
        return new RetryEntitySerializer(objectMapper);
    }

    @Bean
    ApplicationShutdownHandler applicationShutdownHandler(LinkedBlockingQueue<RetryEntity> retryEntities, RetryEntitySerializer retryEntitySerializer) {
        return new ApplicationShutdownHandler(retryEntities, retryEntitySerializer);
    }
}
