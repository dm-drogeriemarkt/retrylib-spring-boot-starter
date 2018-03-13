package de.dm.retrylib;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.GenericTypeResolver;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;
import java.util.List;

public class RetryProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(RetryProcessor.class);

    private static final Integer BATCH_SIZE = 200;

    private final RetryService retryService;

    private final List<RetryHandler> retryHandlers;

    private final ObjectMapper objectMapper;

    public RetryProcessor(RetryService retryService, List<RetryHandler> retryHandlers, ObjectMapper objectMapper) {
        this.retryService = retryService;
        this.retryHandlers = retryHandlers;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedRate = 60000)
    public void processNextRetryBatch() {
        LOG.info("Scheduling the next batch of {} retry entries...", BATCH_SIZE);
        List<RetryEntity> retryBatch = retryService.loadNextRetryEntities(BATCH_SIZE);
        retryBatch.forEach(this::processRetryEntity);
    }

    @SuppressWarnings("unchecked")
    private void processRetryEntity(RetryEntity retryEntity) {
        LOG.info("Processing retry entry {}", retryEntity);
        RetryHandler retryHandler = getRetryHandlerForType(retryEntity.getRetryType());
        Class<?> payloadType = GenericTypeResolver.resolveTypeArgument(retryHandler.getClass(), RetryHandler.class);
        Object deserializedPayload = deserialize(payloadType, retryEntity.getPayload());
        retryService.deleteRetryEntity(retryEntity);
        retryHandler.handleWithRetry(deserializedPayload);
    }

    private <T> T deserialize(Class<T> targetClass, String payload) {
        try {
            return objectMapper.readValue(payload, targetClass);
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not deserialize payload: " + payload + " for target " + targetClass, e);
        }
    }

    private RetryHandler<?> getRetryHandlerForType(String retryType) {
        return retryHandlers
                .stream()
                .filter(it -> retryType.equals(it.retryType()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No retryHandler found for type " + retryType));
    }

}
