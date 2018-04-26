# dm retrylib

[![Build Status](https://travis-ci.org/dm-drogeriemarkt/retrylib-spring-boot-starter.svg?branch=master)](https://travis-ci.org/dm-drogeriemarkt/retrylib-spring-boot-starter)

dm retrylib offers a Java-based retry mechanism with persistence needed for all situations where calls to external services can fail and should be retried periodically until they proceed successfully. 

It uses [ChronicleMap](https://github.com/OpenHFT/Chronicle-Map) under the hood which offers lightweight file-based persistence without the need of a dedicated database server.

The lib is currently targeted for Spring-Boot-1.x-based projects and exposes its functionality as a Spring Boot starter to offer easy integration in existing projects. 

## Usage 

First, include the dm retrylib Spring Boot Starter dependency in your pom.xml:

```xml
<project>
    <dependencies>
        <dependency>
            <groupId>de.dm.retrylib</groupId>
            <artifactId>retrylib-spring-boot-starter</artifactId>
            <version>0.9.1</version>
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

    @Override
    public String retryType() {
        return "externalServiceRetryType";   
    }
}
```

To use the dm retrylib, simply implement the Java Interface `RetryHandler` and expose it as a Spring bean, e. g. with `@Component`. As generic type you specify the payload class holding all the relevant data needed for the retry. 

Implement the `handleWithRetry()` method with the actual call to your external service taking the given payload needed for the method invocation. This method will be reinvoked if an exception occurs while calling the external service.

Implement the `retryType()` method returning a unique retry type string that is used by dm retrylib to find the correct RetryHandler implementation for a specific retry entry.  

## Configuration properties

| Property name  | Description |
| ----------- | ----------- |
| retrylib.health.queueErrorThreshold | The amount of unprocessed retry messages that can be in the map before the health status will be DOWN. Default: `9` |
| retrylib.health.queueWarnThreshold | The amount of unprocessed retry messages that can be in the map before the health status will be WARN. Default: `0` |
| retrylib.persistence.averageValueSize | The average value size of the serialized payload in bytes. Default: `600` |
| retrylib.persistence.fileName | The name of the file containing the retry entries. Default: `retryChronicleMap.dat` |
| retrylib.persistence.filePath | The path to the file containing the retry entries. Default: Value of `java.io.tmpdir` env variable |
| retrylib.persistence.maxEntries | The maximum entries that can be saved in persistence. If this value is exceeded an Exception will be thrown and no further retry entries will be added to the map. Default: `100000` |

## License

Copyright (c) 2018 dm-drogerie markt GmbH + Co. KG, https://dm.de

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