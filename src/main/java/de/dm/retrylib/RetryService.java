package de.dm.retrylib;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.openhft.chronicle.map.ChronicleMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class RetryService {

    private static final Logger LOG = LoggerFactory.getLogger(RetryService.class);

    private ChronicleMap<String, RetryEntity> retryEntities;

    private final ObjectMapper objectMapper;

    private final RetrylibProperties retrylibProperties;

    public RetryService(ObjectMapper objectMapper, @Value("#{retryMap}") ChronicleMap<String, RetryEntity> retryMap, RetrylibProperties retrylibProperties) {
        this.objectMapper = objectMapper;
        this.retryEntities = retryMap;
        this.retrylibProperties = retrylibProperties;
    }

    public synchronized void queueForRetry(String retryType, Object payload) {
        try {
            String payloadAsJson = objectMapper.writeValueAsString(payload);
            String key = retryType + "_" + UUID.randomUUID().toString();
            RetryEntity retryEntity = new RetryEntity(key, retryType, payloadAsJson);
            if (retrylibProperties.getPersistence().getMaxEntries().equals(retryEntities.longSize())) {
                throw new IllegalStateException("MaxEntries of " + retrylibProperties.getPersistence().getMaxEntries() + " reached while trying to add a new RetryEntity for retryType " + retryType + " and payload " + payloadAsJson);
            }
            retryEntities.putIfAbsent(key, retryEntity);
            LOG.info("Queued for retry: {}", retryEntity);
        } catch (JsonProcessingException e) {
            LOG.error("Could not serialize object to json: {}", payload, e);
        }
    }

    public void deleteRetryEntity(RetryEntity retryEntity) {
        retryEntities.remove(retryEntity.getKey());
    }

    public List<RetryEntity> loadNextRetryEntities(Integer batchSize) {
        if (!retryEntities.isOpen()) {
            return Collections.emptyList();
        }
        return retryEntities.entrySet()
                .stream()
                .limit(batchSize)
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }
}
