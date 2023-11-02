package de.dm.retrylib;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@TestPropertySource("classpath:test.properties")
@ExtendWith(SpringExtension.class)
class RetrylibIntegrationTest {

    @MockBean
    private ExternalService externalService;

    @Autowired
    private ExternalServiceRetryHandler externalServiceRetryHandler;

    @Test
    void processesRetryEntitiesIfCallsToExternalServiceFail() throws Exception {
        String firstPayload = "firstPayload";
        String secondPayload = "secondPayload";
        String thirdPayload = "thirdPayload";

        doThrow(new RuntimeException("First provoked Exception"))
                .doThrow(new RuntimeException("Second provoked Exception"))
                .doThrow(new RuntimeException("Third provoked Exception"))
                .doNothing()
                .when(externalService).call(any());

        externalServiceRetryHandler.handleWithRetry(firstPayload);
        externalServiceRetryHandler.handleWithRetry(secondPayload);
        externalServiceRetryHandler.handleWithRetry(thirdPayload);

        Thread.sleep(3000L);

        verify(externalService, times(2)).call(firstPayload);
        verify(externalService, times(2)).call(secondPayload);
        verify(externalService, times(2)).call(thirdPayload);
    }
}
