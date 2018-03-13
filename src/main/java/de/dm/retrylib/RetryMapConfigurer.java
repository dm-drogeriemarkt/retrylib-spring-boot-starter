package de.dm.retrylib;

import net.openhft.chronicle.map.ChronicleMap;
import net.openhft.chronicle.map.ChronicleMapBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class RetryMapConfigurer {

    private static final Logger LOG = LoggerFactory.getLogger(RetryMapConfigurer.class);

    private RetrylibProperties retrylibProperties;

    public RetryMapConfigurer(RetrylibProperties retrylibProperties) {
        this.retrylibProperties = retrylibProperties;
    }

    public ChronicleMap<String, RetryEntity> configureChronicleMap() throws IOException {
        String filePath = retrylibProperties.getPersistence().getFilePath();
        String fileName = retrylibProperties.getPersistence().getFileName();

        String fileLocation = String.format("%s%s%s", filePath, File.separator, fileName);

        ChronicleMap<String, RetryEntity> retryMapWithoutRecovery = null;

        try {
            retryMapWithoutRecovery = createChronicleMapBuilder(retrylibProperties.getPersistence().getMaxEntries(), retrylibProperties.getPersistence().getAverageValueSize()).createPersistedTo(new File(fileLocation));
            checkIfMapHasOnlyValidEntries(retryMapWithoutRecovery);
            return retryMapWithoutRecovery;
        } catch (IOException caughtException) {
            return recoverMapFromInvalidHeader(retrylibProperties.getPersistence().getMaxEntries(), retrylibProperties.getPersistence().getAverageValueSize(), fileLocation, caughtException);
        } catch (RuntimeException caughtException) {
            if (retryMapWithoutRecovery != null) {
                retryMapWithoutRecovery.close();
            }
            return recoverMapFromInvalidEntries(retrylibProperties.getPersistence().getMaxEntries(), retrylibProperties.getPersistence().getAverageValueSize(), fileLocation, caughtException);
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
