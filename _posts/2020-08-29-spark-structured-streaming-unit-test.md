---
layout: post
title: "Spark Structured Streaming에서의 Unit Test"
categories: "bigdata"
---

{% include test-for-data-engineer.md %}

## 안내

아래 글에 사용된 Spark Version: 2.4.2

(2022.02.26 내용 추가)

Spark 3.x에서는 몇 가지 interface가 변경되었다.

자세한 것은 [`StreamingQueryManager.scala`](https://github.com/apache/spark/blob/a4b37757d444182006369d2e4a0b7faaf1d38917/sql/core/src/main/scala/org/apache/spark/sql/streaming/StreamingQueryManager.scala#L230-L241)를 참고해보자.

또한 `MemorySinkV2`가 없어졌고 `MemorySink`를 사용하면 된다.

### 목차

- [개요](#개요)
- [MemoryStream](#memorystream)
- [StreamingQuery.processAllAvailable()](#streamingqueryprocessallavailable)
- [Custom Clock 사용하기](#custom-clock-사용하기)
- [MemorySinkV2](#memorysinkv2)
- [마무리](#마무리)

### 개요

Spark Structured Streaming에서의 Unit Test는 다음과 같은 이유로 번거롭다.

- Data Source 정의
- Trigger
- Processing Time
- Output 검사

본 포스팅에서는 아래의 방법을 사용하여 Spark Structured Streaming의 Unit Test 방식을 설명한다.

1. `MemoryStream`을 사용하여 Data 입력을 쉽게한다
1. `StreamingQuery.processAllAvailable()`을 이용하여 원하는 시점에 Trigger를 할 수 있도록 한다
1. custom clock을 사용하여, Processing Time을 현재 시각이 아닌 임의의 시각으로 저장하도록 한다
1. `MemorySinkV2`를 이용하여 output을 검사하도록 한다

### MemoryStream

Structured Streaming의 Unit Test를 할 때 Data source를 Kafka로 한다고 가정해보자. Data를 Kafka에 넣기 위해 Producer를 만들어야 하므로 테스트하기 번거롭다.

하지만 `MemoryStream`을 이용하여 Unit Test에 사용할 Data를 쉽게 관리할 수 있다. 입력되는 data를 test 내에서 쉽게 확인할 수 있고, data 입력과 처리를 code 상에서 sequential하게 표현할 수 있으므로 test의 가독성 또한 좋다.

구체적인 사용법은 아래에서 설명된다.

### StreamingQuery.processAllAvailable()

당신이 사용하는 stream의 trigger가 5분이라고 해보자. 그렇다면 test할 때마다 최소 5분이 지나야 micro batch가 작동하게 된다.

하지만, `StreamingQuery.processAllAvailable()`을 이용하여 마음대로 trigger를 할 수 있다.

예제 코드를 보자

```scala
case class AccessLog(url: String, timestamp: Timestamp)

import spark.implicits._

implicit val ctx = spark.sqlContext

val memoryStream: MemoryStream[String] = MemoryStream[String]

val schema = org.apache.spark.sql.Encoders.product[AccessLog].schema

// memoryStream에는 `value` 필드만 존재한다
// `value` 필드에 존재하는 json string을 dataframe으로 변환하는 과정
val transformedDf: DataFrame = memoryStream.toDF
  .select(from_json('value, schema) as "data")
  .select("data.*")

val query = transformedDf.writeStream.format("console").start()

memoryStream.addData("""{"url": "url-01", "timestamp": "2020-08-23T00:00:00+09:00"}""")
query.processAllAvailable()

memoryStream.addData("""{"url": "url-02", "timestamp": "2020-08-23T00:00:00+09:00"}""")
query.processAllAvailable()
```

출력 결과는 다음과 같다.

```
-------------------------------------------
Batch: 0
-------------------------------------------
+------+-------------------+
|   url|          timestamp|
+------+-------------------+
|url-01|2020-08-23 00:00:00|
+------+-------------------+

-------------------------------------------
Batch: 1
-------------------------------------------
+------+-------------------+
|   url|          timestamp|
+------+-------------------+
|url-02|2020-08-23 00:00:00|
+------+-------------------+
```

### Custom Clock 사용하기

Unit Test를 하다보면 Processing Time을 내 맘대로 설정하고 싶은 욕구가 생긴다. 하지만, Spark Structured Streaming에는 이런 기능이 없다.

그런데, Spark의 소스 코드에서 Unit Test를 잘 들여다봤더니, custom clock을 전달하는 방법이 있었다.

`startQuery()`의 마지막 인자를 보면 `triggerClock`이 있는데 이걸 이용하여 임의의 시각을 지정할 수 있다.

```scala
  private[sql] def startQuery(
      userSpecifiedName: Option[String],
      userSpecifiedCheckpointLocation: Option[String],
      df: DataFrame,
      extraOptions: Map[String, String],
      sink: BaseStreamingSink,
      outputMode: OutputMode,
      useTempCheckpointLocation: Boolean = false,
      recoverFromCheckpointLocation: Boolean = true,
      trigger: Trigger = ProcessingTime(0),
      triggerClock: Clock = new SystemClock()): StreamingQuery = {
```

(출처: [`StreamingQueryManager.scala`](https://github.com/apache/spark/blob/242c01d858372ff1e41f6a21c76d1d6451871961/sql/core/src/main/scala/org/apache/spark/sql/streaming/StreamingQueryManager.scala#L311-L321)

`startQuery()`는 private method 이기 때문에 package를 `org.apache.spark.sql`로 변경해야한다.

`spark-shell` 내에서 package를 변경할 수 있는 방법이 없어보이므로, 아래의 code는 컴파일 후 실행해야한다.

```scala
package org.apache.spark.sql.jasonheo

import java.sql.Timestamp

import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.execution.streaming.{ConsoleSinkProvider, MemoryStream, StreamingQueryWrapper}
import org.apache.spark.sql.functions.from_json
import org.apache.spark.sql.streaming.OutputMode
import org.apache.spark.util.ManualClock

object CustomClock {
  case class AccessLog(url: String, timestamp: Timestamp)

  def main(args: Array[String]): Unit = {
    val spark: SparkSession = SparkSession
      .builder()
      .config("spark.sql.shuffle.partitions", 1)
      .master("local[2]")
      .getOrCreate()

    spark.sparkContext.setLogLevel("WARN")

    import spark.implicits._

    implicit val ctx = spark.sqlContext

    val memoryStream: MemoryStream[String] = MemoryStream[String]

    val schema = org.apache.spark.sql.Encoders.product[AccessLog].schema

    val transformedDf: DataFrame = memoryStream.toDF
      .select(from_json('value, schema) as "data")
      .select("data.*")

    // 밀리초 단위로 timestamp를 설정한다
    // Processing Time을 실행 시점이 아닌 `1598711982000`으로 설정한다
    val customClock = new ManualClock(1598711982000L)

    // `startQuery()` 함수를 사용하면 임의의 clock을 설정할 수 있다.
    val streamQuery = transformedDf.sparkSession
      .streams
      .startQuery(
        userSpecifiedName = Some("structured-streaming-unit-test"),
        userSpecifiedCheckpointLocation = Some("/tmp/dir"),
        df = transformedDf,
        extraOptions = Map[String, String](),
        sink = new ConsoleSinkProvider,
        outputMode = OutputMode.Update,
        recoverFromCheckpointLocation = false,
        triggerClock = customClock
      )
      .asInstanceOf[StreamingQueryWrapper]
      .streamingQuery

    ///////////////////
    // micro batch 0
    ///////////////////
    memoryStream.addData("""{"url": "url-01", "timestamp": "2020-08-23T00:00:00+09:00"}""")

    println(s"customClock.getTimeMillis()='${customClock.getTimeMillis()}'")
    streamQuery.processAllAvailable()

    ///////////////////
    // micro batch 1
    ///////////////////
    memoryStream.addData("""{"url": "url-02", "timestamp": "2020-08-23T00:00:00+09:00"}""")

    // 현재 설정된 시각으로부터 5초 이후로 이동
    customClock.advance(5000)
    println(s"customClock.getTimeMillis()='${customClock.getTimeMillis()}'")

    streamQuery.processAllAvailable()
  }
}
```

출력 결과

```
customClock.getTimeMillis()='1598711982000'
-------------------------------------------
Batch: 0
-------------------------------------------
+------+-------------------+
|   url|          timestamp|
+------+-------------------+
|url-01|2020-08-23 00:00:00|
+------+-------------------+

customClock.getTimeMillis()='1598711987000'
-------------------------------------------
Batch: 1
-------------------------------------------
+------+-------------------+
|   url|          timestamp|
+------+-------------------+
|url-02|2020-08-23 00:00:00|
+------+-------------------+
```

### MemorySinkV2

stream query의 정합성 검사는 output을 통해 검사해야한다. console output을 이용하여 정합성을 검사하기엔 번거롭다.

. 이때는 output을 `MemorySinkV2`에 저장한 뒤에 검사하면 된다.

`MemorySinkV2.allData`는 data type이 `Seq[Row]`이므로 DataFrame의 Row API를 이용하여 필드 값에 접근할 수 있다.

```scala
val memorySink = new MemorySinkV2

val streamQuery = transformedDf.sparkSession
  .streams
  .startQuery(
    userSpecifiedName = Some("structured-streaming-unit-test"),
    userSpecifiedCheckpointLocation = Some("/tmp/checkpoint-dir"),
    df = transformedDf,
    extraOptions = Map[String, String](),
    sink = memorySink,
    outputMode = OutputMode.Update,
    recoverFromCheckpointLocation = false,
    triggerClock = customClock
  )
  .asInstanceOf[StreamingQueryWrapper]
  .streamingQuery

///////////////////
// micro batch 0
///////////////////
memoryStream.addData("""{"url": "url-01", "timestamp": "2020-08-23T00:00:00+09:00"}""")

streamQuery.processAllAvailable()

println("Batch 0")
memorySink.allData.foreach(println)

// micro batch 마다 clear를 한다. 그렇지 않은 경우 직전 micro batch의 output이 포함되게 된다
memorySink.clear

///////////////////
// micro batch 1
///////////////////
memoryStream.addData("""{"url": "url-02", "timestamp": "2020-08-23T00:00:00+09:00"}""")

// 현재 설정된 시각으로부터 5초 이후로 이동
customClock.advance(5000)
streamQuery.processAllAvailable()

println("Batch 1")
memorySink.allData.foreach(println)
```

출력 결과

```
Batch 0
[url-01,2020-08-23 00:00:00.0]
Batch 1
[url-02,2020-08-23 00:00:00.0]
```

### 마무리

최종 Unit Test code는 https://github.com/jason-heo/spark-sstream-unit-test 에서 볼 수 있다.

{% include spark-reco.md %}

{% include test-for-data-engineer.md %}
