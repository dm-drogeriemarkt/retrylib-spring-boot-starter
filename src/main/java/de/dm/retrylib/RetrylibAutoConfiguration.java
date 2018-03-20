package de.dm.retrylib;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.openhft.chronicle.map.ChronicleMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.IOException;
import java.util.List;

@Configuration
@EnableScheduling
@EnableConfigurationProperties(RetrylibProperties.class)
public class RetrylibAutoConfiguration {

    @Autowired
    private RetrylibProperties retrylibProperties;

    @ConditionalOnMissingBean(RetryService.class)
    @Bean
    public RetryService retryService(ObjectMapper objectMapper, @Value("#{retryMap}") ChronicleMap<String, RetryEntity> retryMap) {
        return new RetryService(objectMapper, retryMap, retrylibProperties);
    }

    @ConditionalOnMissingBean(RetryProcessor.class)
    @Bean
    public RetryProcessor retryProcessor(RetryService retryService, List<RetryHandler> retryHandlers, ObjectMapper objectMapper) {
        return new RetryProcessor(retryService, retryHandlers, objectMapper);
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

    @Bean
    public RetryMapConfigurer retryMapConfigurer() {
        return new RetryMapConfigurer(retrylibProperties);
    }

    @Bean
    public ChronicleMap<String, RetryEntity> retryMap(RetryMapConfigurer retryMapConfigurer) throws IOException {
        return retryMapConfigurer.configureChronicleMap();
    }

    @Bean
    public RetryMapHealthIndicator retryHandlerHealthIndicator(@Value("#{retryMap}") ChronicleMap<String, RetryEntity> retryMap) {
        return new RetryMapHealthIndicator(retryMap, retrylibProperties.getHealthProperties().getQueueWarnThreshold(), retrylibProperties.getHealthProperties().getQueueErrorThreshold());
    }

}
