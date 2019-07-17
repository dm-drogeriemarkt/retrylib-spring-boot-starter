package de.dm.retrylib;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

class RetryEntitySerializer {

    private final ObjectMapper objectMapper;

    RetryEntitySerializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    String serialize(RetryEntity retryEntity) {
        try {
            return objectMapper.writeValueAsString(retryEntity);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Could not serialize " + retryEntity + " to JSON.");
        }
    }

}
