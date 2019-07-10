package de.dm.retrylib;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Aspect
class RetryAspect {

    private static final Logger LOG = LoggerFactory.getLogger(RetryAspect.class);

    private final RetryService retryService;

    RetryAspect(RetryService retryService) {
        this.retryService = retryService;
    }

    @Pointcut("execution(* *..RetryHandler+.handleWithRetry(..)))")
    public void retryableMethods() {
        // empty method for joinpoint definition
    }

    @Around(value = "retryableMethods()",
            argNames = "joinPoint")
    Object runWithRetry(ProceedingJoinPoint joinPoint) throws Error { //NOSONAR Always propagate Errors further
        try {
            return joinPoint.proceed();
        } catch (Error error) { //NOSONAR we have to catch Errors here because they must pe propagated further
            queueInvocationForRetry(joinPoint, error);
            // Always propagate Errors further
            throw error;
        } catch (Throwable throwable) { //NOSONAR the ProceedingJoinPoint signature forces us to catch Throwable here
            queueInvocationForRetry(joinPoint, throwable);
            // Return null since this annotation is meant to be used on void methods only
            return null;
        }
    }

    private void queueInvocationForRetry(ProceedingJoinPoint joinPoint, Throwable throwable) {
        Object[] invocationArguments = joinPoint.getArgs();
        Object payload = invocationArguments[0];
        RetryHandler retryHandler = (RetryHandler) joinPoint.getTarget();
        LOG.error("An exception occurred when processing retryable method. Scheduling call for retry.", payload, throwable);
        retryService.queueForRetry(retryHandler.getClass(), payload);
    }

}
