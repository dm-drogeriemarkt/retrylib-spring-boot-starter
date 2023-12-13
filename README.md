# dm retrylib

[<img src="https://opensourcelogos.aws.dmtech.cloud/dmTECH_opensource_logo.svg" height="20" width="130">](https://www.dmtech.de/de)
[![Build Status](https://github.com/dm-drogeriemarkt/retrylib-spring-boot-starter/actions/workflows/build.yml/badge.svg?branch=master)](https://github.com/dm-drogeriemarkt/retrylib-spring-boot-starter/actions?query=branch%3Amaster)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/de.dm.retrylib/retrylib-spring-boot-starter/badge.svg)](https://maven-badges.herokuapp.com/maven-central/retrylib-spring-boot-starter/structured-logging)

dm retrylib offers a Java-based in-memory retry mechanism needed for all situations where calls to external services can fail and should be retried periodically until they proceed successfully. 

If the JVM is terminated while there were still some invocations to retry, these will be logged with their JSON representation.

The lib is currently targeted for Spring-Boot-2.x-based projects with a minimum Java 8 baseline and exposes its functionality as a Spring Boot starter to offer easy integration into existing projects. 

## Usage 

First, include the dm retrylib Spring Boot Starter dependency in your pom.xml:

```xml
<project>
    <dependencies>
        <dependency>
            <groupId>de.dm.retrylib</groupId>
            <artifactId>retrylib-spring-boot-starter</artifactId>
            <version>3.0.2</version>
         </dependency>
    </dependencies>
</project>
```

Then implement the Java Interface `RetryHandler` with a Spring Bean:

```java
@Component
public class ExternalServiceRetryHandler implements RetryHandler<PayloadDto> {

    @Override
    public void handleWithRetry(PayloadDto payload) {
        externalService.call(payload);
    }

}
```

To use the dm retrylib, simply implement the Java Interface `RetryHandler` and expose it as a Spring bean, e. g. with `@Component`. As generic type you specify the payload class holding all the relevant data needed for the retry. 

Implement the `handleWithRetry()` method with the actual call to your external service taking the given payload needed for the method invocation. This method will be reinvoked if an exception occurs while calling the external service. Make sure the payload can be converted to JSON by the Jackson library (e. g. has public getters).
  

## Configuration properties

| Property name  | Description |
| ----------- | ----------- |
| retrylib.queueLimit | The maximum number of entries to be put into the retry queue. This property is used to initialize the in-memory retry queue. If this value is exceeded an Exception will be thrown and no further retry entries will be added to the queue. Default: `100000` |
| retrylib.retryIntervalInMillis | The interval in milliseconds that is used to process a retry batch. Default: `60000` (=1 minute) |


## Metrics / Alerting hints

The dm retrylib exposes the current amount of invocations to be retried as a Micrometer gauge named `retrylib.entitiesToRetry`. This metric can be used for creating an alert if the value doesn't fall to zero after some time. 

If during the shutdown of the JVM there are still some invocations to retry, they will be logged as a last resort with their payload in JSON format. You can create an alert for the log message pattern `# retryEntities remained during application shutdown.`.
Every single invocation is then logged as JSON an can be manually / programmatically processed afterwards.

## License

Copyright (c) 2018-2019 dmTECH GmbH, https://www.dmtech.de

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
