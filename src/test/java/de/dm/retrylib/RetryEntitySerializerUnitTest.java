package de.dm.retrylib;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RetryEntitySerializerUnitTest {

    private final ObjectMapper objectMapper = mock(ObjectMapper.class);

    private final RetryEntitySerializer retryEntitySerializer = new RetryEntitySerializer(objectMapper);

    @Test
    void serialize() throws Exception {
        String payload = "payload";
        RetryEntity retryEntity = new RetryEntity("key", ExternalServiceRetryHandler.class, payload);
        when(objectMapper.writeValueAsString(retryEntity)).thenReturn("{}");

        String serializedEntityAsJson = retryEntitySerializer.serialize(retryEntity);

        assertThat(serializedEntityAsJson, is("{}"));
    }

    @Test
    void serializeThrowsExceptionIfEntityCouldNotBeSerialized() throws Exception {
        String payload = "payload";
        RetryEntity retryEntity = new RetryEntity("key", ExternalServiceRetryHandler.class, payload);
        doThrow(JsonMappingException.from(mock(DeserializationContext.class), "provoked exception")).when(objectMapper).writeValueAsString(retryEntity);

        Assertions.assertThrows(IllegalArgumentException.class, () -> retryEntitySerializer.serialize(retryEntity));
    }

}