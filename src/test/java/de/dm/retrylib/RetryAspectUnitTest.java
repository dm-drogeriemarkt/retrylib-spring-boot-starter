package de.dm.retrylib;

import org.aspectj.lang.ProceedingJoinPoint;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class RetryAspectUnitTest {

    private RetryAspect retryAspect;

    private RetryService retryService = mock(RetryService.class);
    private RetryHandler retryHandler = mock(RetryHandler.class);

    @BeforeEach
    void setUp() {
        retryAspect = new RetryAspect(retryService);
    }

    @Test
    void runWithRetryProceedsJoinPoint() throws Throwable {
        ProceedingJoinPoint proceedingJoinPoint = mock(ProceedingJoinPoint.class);

        retryAspect.runWithRetry(proceedingJoinPoint);

        verify(proceedingJoinPoint).proceed();
    }


    @Test
    void runWithRetryRequeuesInvocationOnError() throws Throwable {
        ProceedingJoinPoint proceedingJoinPoint = mock(ProceedingJoinPoint.class);
        doThrow(new UnknownError("Testerror")).when(proceedingJoinPoint).proceed();
        String payload = "payload";
        Object[] invocationArguments = new Object[1];
        invocationArguments[0] = payload;
        when(proceedingJoinPoint.getArgs()).thenReturn(invocationArguments);
        when(proceedingJoinPoint.getTarget()).thenReturn(retryHandler);

        Assertions.assertThrows(UnknownError.class, () -> retryAspect.runWithRetry(proceedingJoinPoint));

        verify(proceedingJoinPoint).proceed();
        verify(retryService).queueForRetry(retryHandler.getClass(), payload);
    }

    @Test
    void runWithRetryRequeuesInvocationOnThrowable() throws Throwable {
        ProceedingJoinPoint proceedingJoinPoint = mock(ProceedingJoinPoint.class);
        doThrow(new Throwable("TestThrowable")).when(proceedingJoinPoint).proceed();
        String payload = "payload";
        Object[] invocationArguments = new Object[1];
        invocationArguments[0] = payload;
        when(proceedingJoinPoint.getArgs()).thenReturn(invocationArguments);
        when(proceedingJoinPoint.getTarget()).thenReturn(retryHandler);

        Object returnValue = retryAspect.runWithRetry(proceedingJoinPoint);
        MatcherAssert.assertThat(returnValue, CoreMatchers.nullValue());

        verify(proceedingJoinPoint).proceed();
        verify(retryService).queueForRetry(retryHandler.getClass(), payload);
    }

    @Test
    void retryableMethodsPointcutExistsAndCanBeInvoked() {
        retryAspect.retryableMethods();
    }

}