package de.dm.retrylib;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.openhft.chronicle.map.ChronicleMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Configuration
@EnableScheduling
public class RetrylibAutoConfiguration {

    @ConditionalOnMissingBean(RetryService.class)
    @Bean
    public RetryService retryService(ObjectMapper objectMapper, @Value("#{retryMap}") ChronicleMap<String, RetryEntity> retryMap) {
        return new RetryService(objectMapper, retryMap);
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
            public void handleWithRetry(Object payload) {}

            @Override
            public String retryType() {
                return "";
            }
        };
    }

    @Bean
    public ChronicleMap<String, RetryEntity> retryMap(@Value("${retrylib.persistence.maxEntries:1000000}") Long maxEntries,
                                                      @Value("${retrylib.persistence.averageValueSize:600}") Double averageValueSize,
                                                      @Value("${retrylib.persistence.filePath}") String filePath,
                                                      @Value("${retrylib.persistence.fileName}") String fileName) throws IOException {

        if (filePath.isEmpty()) {
            filePath = System.getProperty("java.io.tmpdir");
        }

        if (fileName.isEmpty()) {
            fileName = "retryChronicleMap.dat";
        }

        String fileLocation = String.format("%s/%s", filePath, fileName);

        return ChronicleMap
                .of(String.class, RetryEntity.class)
                .name("retryMap")
                .entries(maxEntries)
                .averageKey("sampleRetry_" + UUID.randomUUID())
                .averageValueSize(averageValueSize)
                .createPersistedTo(new File(fileLocation));
    }

}
