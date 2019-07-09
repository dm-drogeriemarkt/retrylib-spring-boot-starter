package de.dm.retrylib;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

public class RetryService {

    private static final Logger LOG = LoggerFactory.getLogger(RetryService.class);

    private LinkedBlockingQueue<RetryEntity> retryEntities;

    private final ObjectMapper objectMapper;

    private final RetrylibProperties retrylibProperties;

    public RetryService(ObjectMapper objectMapper, LinkedBlockingQueue<RetryEntity> retryEntities, RetrylibProperties retrylibProperties) {
        this.objectMapper = objectMapper;
        this.retryEntities = retryEntities;
        this.retrylibProperties = retrylibProperties;
    }

    public synchronized void queueForRetry(String retryType, Object payload) {
        try {
            String payloadAsJson = objectMapper.writeValueAsString(payload);
            String key = retryType + "_" + UUID.randomUUID().toString();
            RetryEntity retryEntity = new RetryEntity(key, retryType, payloadAsJson);

            retryEntities.add(retryEntity);
            LOG.info("Queued for retry: {}", retryEntity);
        } catch (JsonProcessingException e) {
            LOG.error("Could not serialize object to json: {}", payload, e);
            throw new IllegalStateException("Could not serialize object to json: " + payload, e);
        }
    }

    public void deleteRetryEntity(RetryEntity retryEntity) {
        retryEntities.remove(retryEntity.getKey());
    }

    public List<RetryEntity> loadNextRetryEntities(Integer batchSize) {
        List<RetryEntity> retryBatch = new ArrayList<>();
        this.retryEntities.drainTo(retryBatch, batchSize);
        return retryBatch;
    }
}
