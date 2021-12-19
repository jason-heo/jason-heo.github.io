---
layout: post
title: "Spark Log Level 변경하기"
categories: "bigdata"
---

## 1. code 적으로 Log Level을 변경하는 방법

### 방법 1

```scala
spark.sparkContext.setLogLevel("WARN")
```

(출처: `spark-shell` 실행시에 나오는 메시지)

### 방법 2

```scala
import org.apache.log4j.{Level, Logger}

Logger.getRootLogger.setLevel(Level.WARN)
```

(출처: https://stackoverflow.com/a/34306251/2930152)

### 방법 3

```scala
import org.apache.log4j.{Level, Logger}

Logger.getLogger("org").setLevel(Level.WARN)
Logger.getLogger("akka").setLevel(Level.WARN)
```

(출처: https://stackoverflow.com/a/27815509/2930152)


### 참고

"방법 3"을 이용하면 특정 패키지의 Log Level을 변경할 수 있다.

예를 들어, Spark 관련된 code는 WARN Level을 사용하고, 내가 작성한 code는 INFO Level을 사용할 수 있다.

```scala
import org.apache.log4j.{Level, Logger}

// 기본은 WARN Level로 설정
Logger.getRootLogger.setLevel(Level.WARN)

// io.github.jasonheo만 INFO Level로 설정
Logger.getLogger("io.github.jasonheo").setLevel(Level.INFO)
```

## 2. logj4.properties를 이용하는 방법

아무래도 하드코딩을 하는 것보단 `log4j.properties`를 사용하는 것이 좋다.

### 단계 1) `log4j.properties` 파일 작성

```
# WARN Level로 설정한다
log4j.rootLogger=WARN, console

log4j.appender.console=org.apache.log4j.ConsoleAppender
log4j.appender.console.target=System.err
log4j.appender.console.layout=org.apache.log4j.PatternLayout
log4j.appender.console.layout.ConversionPattern=%d{yy/MM/dd HH:mm:ss} %p %c{1}: %m%n

# 특정 패키지의 Level만 INFO로 설정할 수 있다
log4j.logger.io.github.jasonheo=INFO
```

### 단계 2) `spark-shell` 혹은 `spark-submit` 실행 시 `log4j.properties` 지정하기

이 부분은 yarn cluster mode냐 client mode냐에 따라 약간 다르다.

yarn cluster mode에서는 driver와 executor 모두 원격의 Yarn NodeManager에서 수행된다. 따라서 `--files` 옵션을 이용하여 `logj4.properties`를 upload해야한다. uploade된 경로는 자동으로 classpath에 포함되므로 `-Dlog4j.configuration`에서는 `log4j.properties`를 상대 경로로 지정하면 된다.

yarn client mode에서는 driver가 local machine에서 수행되고 executor는 Yarn NodeManager에서 수행된다. 따라서 driver에서는 `log4j.properties`를 local file system의 절대 경로를 지정하면 되고 executor에서는 yarn cluster와 동일한 방법으로 지정하면 된다.

yarn cluster mode의 샘플 명령은 다음과 같다.

```
spark-submit \
    --master yarn \
    --deploy-mode cluster \
    --conf "spark.driver.extraJavaOptions=-Dlog4j.configuration=file:log4j.properties" \
    --conf "spark.executor.extraJavaOptions=-Dlog4j.configuration=file:log4j.properties" \
    --files "/absolute/path/to/your/log4j.properties" \
    --class com.github.atais.Main \
    "SparkApp.jar"
```

(출처: https://stackoverflow.com/a/55596389/2930152)

{% include spark-reco.md %}
