package de.dm.retrylib;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Aspect
public class RetryAspect {

    private static final Logger LOG = LoggerFactory.getLogger(RetryAspect.class);

    private final RetryService retryService;

    public RetryAspect(RetryService retryService) {
        this.retryService = retryService;
    }

    @Pointcut("execution(* *..RetryHandler+.handleWithRetry(..)))")
    public void retryableMethods() {
        // empty method for joinpoint definition
    }

    @Around(value = "retryableMethods()",
            argNames = "joinPoint")
    public Object runWithRetry(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (Throwable throwable) { //NOSONAR the ProceedingJoinPoint signature forces us to catch Throwable here
            Object[] invocationArguments = joinPoint.getArgs();
            Object payload = invocationArguments[0];
            RetryHandler retryHandler = (RetryHandler) joinPoint.getTarget();
            LOG.error("An exception occurred when processing retryable method. Scheduling call for retry.", payload, throwable);
            retryService.queueForRetry(retryHandler.retryType(), payload);
            if (throwable instanceof Error) {
                // Always propagate Errors further
                throw throwable;
            }
            //Return null since this annotation is meant to be used on void methods only
            return null;
        }
    }

}
