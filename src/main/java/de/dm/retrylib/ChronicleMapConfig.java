package de.dm.retrylib;

import net.openhft.chronicle.map.ChronicleMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Configuration
public class ChronicleMapConfig {

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
