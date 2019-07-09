package de.dm.retrylib;

import org.aspectj.lang.ProceedingJoinPoint;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RetryAspectUnitTest {

    private RetryAspect retryAspect;

    private RetryService retryService = mock(RetryService.class);
    private RetryHandler retryHandler = mock(RetryHandler.class);

    @Before
    public void setUp() {
        retryAspect = new RetryAspect(retryService);
    }

    @Test
    public void runWithRetryProceedsJoinPoint() throws Throwable {
        ProceedingJoinPoint proceedingJoinPoint = mock(ProceedingJoinPoint.class);

        retryAspect.runWithRetry(proceedingJoinPoint);

        verify(proceedingJoinPoint).proceed();
    }


    @Test(expected = UnknownError.class)
    public void runWithRetryRequeuesInvocationOnError() throws Throwable {
        ProceedingJoinPoint proceedingJoinPoint = mock(ProceedingJoinPoint.class);
        doThrow(new UnknownError("Testerror")).when(proceedingJoinPoint).proceed();
        String payload = "payload";
        Object[] invocationArguments = new Object[1];
        invocationArguments[0] = payload;
        when(proceedingJoinPoint.getArgs()).thenReturn(invocationArguments);
        when(proceedingJoinPoint.getTarget()).thenReturn(retryHandler);

        retryAspect.runWithRetry(proceedingJoinPoint);

        verify(proceedingJoinPoint).proceed();
        verify(retryService).queueForRetry(retryHandler.getClass(), payload);
    }

    @Test
    public void runWithRetryRequeuesInvocationOnThrowable() throws Throwable {
        ProceedingJoinPoint proceedingJoinPoint = mock(ProceedingJoinPoint.class);
        doThrow(new Throwable("TestThrowable")).when(proceedingJoinPoint).proceed();
        String payload = "payload";
        Object[] invocationArguments = new Object[1];
        invocationArguments[0] = payload;
        when(proceedingJoinPoint.getArgs()).thenReturn(invocationArguments);
        when(proceedingJoinPoint.getTarget()).thenReturn(retryHandler);

        Object returnValue = retryAspect.runWithRetry(proceedingJoinPoint);
        assertThat(returnValue, CoreMatchers.nullValue());

        verify(proceedingJoinPoint).proceed();
        verify(retryService).queueForRetry(retryHandler.getClass(), payload);
    }

    @Test
    public void retryableMethodsPointcutExistsAndCanBeInvoked() {
        retryAspect.retryableMethods();
    }

}