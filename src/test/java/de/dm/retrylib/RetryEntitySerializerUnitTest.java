package de.dm.retrylib;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RetryEntitySerializerUnitTest {

    private final ObjectMapper objectMapper = mock(ObjectMapper.class);

    private final RetryEntitySerializer retryEntitySerializer = new RetryEntitySerializer(objectMapper);

    @Test
    public void serialize() throws Exception {
        String payload = "payload";
        RetryEntity retryEntity = new RetryEntity("key", ExternalServiceRetryHandler.class, payload);
        when(objectMapper.writeValueAsString(retryEntity)).thenReturn("{}");

        String serializedEntityAsJson = retryEntitySerializer.serialize(retryEntity);

        assertThat(serializedEntityAsJson, is("{}"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void serializeThrowsExceptionIfEntityCouldNotBeSerialized() throws Exception {
        String payload = "payload";
        RetryEntity retryEntity = new RetryEntity("key", ExternalServiceRetryHandler.class, payload);
        when(objectMapper.writeValueAsString(retryEntity)).thenThrow(JsonMappingException.from(mock(DeserializationContext.class), "provoked exception"));

        retryEntitySerializer.serialize(retryEntity);
    }

}