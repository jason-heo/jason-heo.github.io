---
layout: post
title: "Spark S. Stream에서 source로부터 읽어온 내용은 자동 cache된다"
categories: "bigdata"
---

Spark에서 action이 발생하는 순간 data를 읽어간다. 이미 한번 읽혀진 data더라도 cache를 하지 않으면 처음부터 다시 읽는다 (이하 re-read)

다들 이렇게 알고 있을 것이다. 하지만 반은 맞고 반은 틀린 말이다.

본 글은 Spark Data Source API v2를 이용하여 custom data source를 만든 후 data 유실 관련 실험 중 발견된 내용을 정리한 글이다.

## 기초: Batch processing에서는 action이 발생할 때마다 re-read한다

본 내용은 많은 사람들이 이미 알고 있지만 streaming 처리로 넘어가기 전에 action 시 어떤 일이 발생하는지 리뷰를 해보자.

아래와 같은 코드를 작성후 실행한다.

- test code
    ```scala
    Seq("1", "2")
      .toDF("value")
      .write
      .mode("overwrite")
      .parquet("/tmp/parquet-test")

    val df = spark.read.parquet("/tmp/parquet-test")

    df.show

    println(df.count)

    df.show
    ```
- 수행 결과
    ```
    +-----+
    |value|
    +-----+
    |    1|
    |    2|
    +-----+

    2

    +-----+
    |value|
    +-----+
    |    1|
    |    2|
    +-----+
    ```

당연한 결과가 출력되었다. 그런데 여기서 중요한 점은 `df.show`, `df.count` 등의 action이 발생할 때마다 parquet file을 새로 읽는다는 점이다.

action마다 re-read하는지 어떻게 알 수 있을까? Spark의 log level을 `INFO`로 하면 매 action마다 다음과 같은 내용이 출력되는 걸 알 수 있다.

```
22/02/19 15:28:13 INFO FileScanRDD: Reading File path: file:///tmp/parquet-test/<filename>.snappy.parquet, range: 0-448, partition values: [empty row]

+-----+
|value|
+-----+
|    1|
|    2|
+-----+

22/02/19 15:28:13 INFO FileScanRDD: Reading File path: file:///tmp/parquet-test/<filename>.snappy.parquet, range: 0-448, partition values: [empty row]

2

22/02/19 15:28:14 INFO FileScanRDD: Reading File path: file:///tmp/parquet-test/<filename>.snappy.parquet, range: 0-448, partition values: [empty row]
+-----+
|value|
+-----+
|    1|
|    2|
+-----+
```

이는 Spark의 아주 기본적인 처리 흐름이다. action 마다 re-read를 하므로 중간에 business logic 들이 처음부터 실행된다 (이하 re-compute). 이를 방지하기 위해서 `df.cache` 이후에 action을 여러 번 실행하는 것이 좋다.

## Structured Streaming에서는 action이 발생해도 re-read하지 않는다

Structured Streaming(이하 S. Stream)에서는 어떨까? 본인의 경우 S. Stream을 본격적으로 사용한 2년전부터 최근까지 batch 처리와 동일하게 re-read하는 줄 알고 있었다.

그러다 최근에 직접 개발한 Data source의 유실 여부 테스트 중에 알게된 것인데 S. Stream의 경우 re-read를 하지 않았다.

쉽게 테스트할 수 있는 Kafka source로 확인을 해보자.

```scala
val df = spark
  .readStream
  .format("kafka")
  .option("kafka.bootstrap.servers", "localhost:9092")
  .option("subscribe", topicName)
  .load

val query = df
  .selectExpr("CAST(CAST(value AS STRING) AS NUMERIC) AS value")
  .repartition(2)
  .writeStream
  .trigger(ProcessingTime(s"5 seconds"))
  .foreachBatch(printDf _)
  .start()

def printDf(df: DataFrame, batchId: Long): Unit = {
  df.show

  df.show
}
```

`df.cache` 없이 action을 2번 호출했다. 당연하겠지만 동일한 내용이 출력된다. 아직 micro batch가 종료되지 않았기 때문에 re-read되었다 하더라도 동일 offset을 읽었을 것이다. 따라서 이것만 봐서는 re-read를 한 것인지, re-read없이 cache된 데이터를 읽은 것인지 확인이 불가능하다.

이번에는 Spark의 log level을 INFO로 설정한 뒤에 실행해보자. 많은 로그 메시지가 출력되는데 그 중에 아래와 같은 내용을 볼 수 있다.

- 첫 번째 action 실행 중 출력된 로그
    ```
    22/02/19 14:50:23 INFO MemoryStore: Block rdd_55_1 stored as values in memory (estimated size 288.0 B, free 3.6 GiB)
    22/02/19 14:50:23 INFO BlockManagerInfo: Added rdd_55_1 in memory on <ip address>:60599 (size: 288.0 B, free: 3.6 GiB)
    22/02/19 14:50:23 INFO Executor: Finished task 0.0 in stage 19.0 (TID 14). 2741 bytes result sent to driver
    ```
- 두 번째 action에서 출력된 로그
    ```
    22/02/19 14:50:23 INFO Executor: Running task 0.0 in stage 21.0 (TID 15)
    22/02/19 14:50:23 INFO Executor: Running task 1.0 in stage 21.0 (TID 16)
    22/02/19 14:50:23 INFO BlockManager: Found block rdd_55_1 locally
    22/02/19 14:50:23 INFO BlockManager: Found block rdd_55_0 locally
    22/02/19 14:50:23 INFO Executor: Finished task 1.0 in stage 21.0 (TID 16). 2879 bytes result sent to driver
    ```

첫 번째 action에서는 "Block rdd_55_1 store"라고 나오고, 두 번째 action에서는 "Found block rdd_55_1"가 출력되었다. 그렇다. 처음 data를 읽은 후 이를 cache하고 두 번째에는 cache된 데이터를 읽은 것이다. 즉, 내가 명시적으로 `df.cache`를 하지도 않았는데, kafka로부터 읽어온 data를 cache하고 있다.

Spark manual 중 [ForeachBatch](https://spark.apache.org/docs/3.2.0/structured-streaming-programming-guide.html#foreachbatch)를 보면 다음과 같은 내용이 나온다.

> Write to multiple locations - If you want to write the output of a streaming query to multiple locations, then you can simply write the output DataFrame/Dataset multiple times. However, each attempt to write can cause the output data to be recomputed (including possible re-reading of the input data)

re-read가 당연하게 발생할 것으로 예상했는데 아니었다. possible이 붙은 이유를 모르겠다. 물론 executor가 죽는 경우에는 cache가 사라지므로 re-read가 발생하게 될 가능성이 있다.

(`df.cache`의 경우 replica factor가 2인데, 위 상황에서의 rdd cache는 replica factor가 몇인지 모르겠다)

## Discretized Streaming에서 Custom Receiver 사용 시는?

Discretized Streaming (이하 D. Stream)에서 data source를 개발할 때는 [Custom Receiver](https://spark.apache.org/docs/3.2.0/streaming-custom-receivers.html)를 사용한다.

receiver용 thread가 background에서 data를 계속 읽어가면서 block에 저장한다. `df.cache`와 비슷하게 Spark UI의 Storage 탭에 가면 cache된 block 내용을 볼 수 있다. 그리고 이 block들이 Spark의 기본 처리 단위인 Task가 된다.

action을 여러 번 수행하더라도 block을 여러 번 읽게 되므로 data source로부터 re-read하는 일은 없다.

## 참고

Spark 3.2.0으로 테스트했다 (아마 과거 버전도 동일하게 cache가 되지 않을까 싶다)
