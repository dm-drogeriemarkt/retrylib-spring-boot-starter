package de.dm.retrylib;

import org.springframework.stereotype.Component;

@Component
public class ExternalServiceRetryHandler implements RetryHandler {


    private final ExternalService externalService;

    public ExternalServiceRetryHandler(ExternalService externalService) {
        this.externalService = externalService;
    }

    @Override
    public void handleWithRetry(Object payload) {
        externalService.call(payload);
    }

}
