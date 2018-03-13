package de.dm.retrylib;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.openhft.chronicle.map.ChronicleMap;
import net.openhft.chronicle.map.ChronicleMapBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Configuration
@EnableScheduling
public class RetrylibAutoConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(RetrylibAutoConfiguration.class);


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

        ChronicleMap<String, RetryEntity> retryMapWithoutRecovery = null;

        try {
            retryMapWithoutRecovery = createChronicleMapBuilder(maxEntries, averageValueSize).createPersistedTo(new File(fileLocation));
            checkIfMapHasOnlyValidEntries(retryMapWithoutRecovery);
            return retryMapWithoutRecovery;
        } catch (IOException caughtException) {
            return recoverMapFromInvalidHeader(maxEntries, averageValueSize, fileLocation, caughtException);
        } catch (RuntimeException caughtException) {
            if (retryMapWithoutRecovery != null) {
                retryMapWithoutRecovery.close();
            }
            return recoverMapFromInvalidEntries(maxEntries, averageValueSize, fileLocation, caughtException);
        }
    }

    private ChronicleMapBuilder<String, RetryEntity> createChronicleMapBuilder(Long entries, Double averageValueSize) {
        return ChronicleMap
                .of(String.class, RetryEntity.class)
                .name("retryMap")
                .entries(entries)
                .averageKey("sampleRetry" + UUID.randomUUID())
                .averageValueSize(averageValueSize);
    }

    private ChronicleMap<String, RetryEntity> recoverMapFromInvalidHeader(Long entries, Double averageValueSize, String fileLocation, IOException iex) throws IOException {
        ChronicleMap<String, RetryEntity> retryMap;
        LOG.warn("Retry Map seems to be corrupt in header section. Starting recovery.", iex);
        retryMap = createChronicleMapBuilder(entries, averageValueSize).recoverPersistedTo(new File(fileLocation), true);
        LOG.warn("Finished recovering Retry Map.");
        return retryMap;
    }

    private void checkIfMapHasOnlyValidEntries(ChronicleMap<String, RetryEntity> retryMapWithoutRecovery) {
        retryMapWithoutRecovery.entrySet()
                .stream()
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    private ChronicleMap<String, RetryEntity> recoverMapFromInvalidEntries(Long entries, Double averageValueSize, String fileLocation, RuntimeException rex) throws IOException {
        ChronicleMap<String, RetryEntity> retryMap;
        LOG.warn("Retry Map seems to have corrupt entries. Starting recovery.", rex);
        retryMap = createChronicleMapBuilder(entries, averageValueSize).recoverPersistedTo(new File(fileLocation), false);
        LOG.warn("Finished recovering Retry Map.");
        return retryMap;
    }

}
