---
layout: post
title: "Spark Custom Stream Source 만들기 (MicroBatchReader, ContinuousReader)"
categories: "bigdata"
---

Streaming Source를 직접 만드는 방법에 자료가 많지 않다. 게다가 Data Source API V2로 작성된 예제는 더 없다.

그래서 Spark4에서 샘플로 제공하는 rate source의 소스 코드인 [RateStreamProvider.scala](https://github.com/apache/spark/blob/branch-2.4/sql/core/src/main/scala/org/apache/spark/sql/execution/streaming/sources/RateStreamProvider.scala)를 분석 후 `MicroBatchReader`와 `ContinuousReader`를 개발해봤다.

Spark 2.4 기반으로 작성 및 테스트 되었다. Data Source API V2가 Spark 2.3부터 지원했으므로 2.3에서도 돌릴 수 있을 것 같은데, 패키지 경로가 좀 다른 듯 하여 일부 수정이 필요할 듯 하다. Spark 3.0에서도 돌아갈 것 같은데 변경없이 바로 돌아갈지 수정이 필요할지는 확실치 않다.

소스 코드는 https://github.com/jason-heo/spark-stream-source-v2 에서 볼 수 있다.

### 목차

- [상위 구조](#상위-구조)
- [참고: `MicroBatchReader` vs `ContinuousReader`](#참고-microbatchreader-vs-continuousreader)
- [`MicroBatchReader` 구현](#microbatchreader-구현)
- [참고 - `InternalRow` 자료 구조 사용법](#참고---internalrow-자료-구조-사용법)
- [`MicroBatchReader` 호출 예](#microbatchreader-호출-예)
- [`ContinuousReader` 구현](#continuousreader-구현)
- [`ContinuousReader` 호출 예](#continuousreader-호출-예)

### 상위 구조

`MicroBatchReadSupport`와 `ContinuousReadSupport`를 상속받은 후 `MicroBatchReader`와 `ContinuousReader`를 구현하면 된다.

Data source에 따라 둘 중 하나만 상속받아 구현해도 무방하다.

```scala
class RandomIntStreamProvider extends DataSourceV2
  with MicroBatchReadSupport with ContinuousReadSupport with DataSourceRegister with Logging {

  override def createMicroBatchReader(schema: Optional[StructType],
                                      checkpointLocation: String,
                                      options: DataSourceOptions): MicroBatchReader = {
    if (schema.isPresent) {
      throw new IllegalArgumentException("The random-int source does not support a user-specified schema.")
    }

    new RandomIntMicroBatchReader(options, checkpointLocation)
  }

  override def createContinuousReader(schema: Optional[StructType],
                                      checkpointLocation: String,
                                      options: DataSourceOptions): ContinuousReader = {
    new RandomIntContinuousReader(options)
  }

  override def shortName(): String = "random-int"
}
```

### 참고: `MicroBatchReader` vs `ContinuousReader`

- `MicroBatchReader`는 micro batch가 trigger 될 때 data를 읽어오기 시작한다. 따라서 데이터를 읽어오는데 지연이 약간 발생하게 된다. groupBy 질의를 수행할 수 있다
- `ContinuousReader`는 `MicroBatchReader`의 지연을 없애는 reader이다. trigger와 상관없이 executor의 worker가 thread가 background로 계속해서 data를 읽어서 buffer에 저장한다
    - trigger가 발생하면 executor는 buffer에 쌓인 데이터를 소비한다
    - 따라서 `MicroBatchReader`에서 발생할 수 있는 지연이 없다
    - 하지만 groupBy 질의가 되지 않는다

### `MicroBatchReader` 구현

`MicroBatchReader`를 구현하기 위해서는 아래 3개 class를 상속 후 구현해야한다

- `MicroBatchReader`
    - `override def setOffsetRange(_start: Optional[Offset], _end: Optional[Offset]): Unit`
        -  이 함수를 이해하기가 힘들었다
        - micro batch마다 두 번씩 호출되는데 `_start`는 직전 end offset이라서 어렵지 않지만
        - `_end`가 어렵다
        - `_end`는 처음 호출 시에는 `Optional.empty`가 넘어오지만, 두 번째 호출 시에는 `getEndOffset()`의 return value가 전달된다
    - `override def getEndOffset: Offset`
        - 이것도 중요하다
        - `getEndOffset`의 return value가 직전 호출의 return value와 달라아먄 micro batch가 시작한다
    - 그외 함수들은 크게 어렵지 않아서 설명 생략
- `RandomIntBatchInputPartition`
- `RandomIntMicroBatchInputPartitionReader`
    - `override def next(): Boolean`
        - `true`를 return하는한  micro batch read를 계속 진행한다
        - `false`를 return해야 micro batch read가 종료되고 이후 읽어온 data를 처리하게 된다
    - `override def get(): InternalRow`
        - `next()`가 true인 경우, 읽어온 row를 return한다
        - 다음 `next()`가 호출되기 전까지 `get()`은 항상 동일한 레코드를 return해야 한다
        - 따라서 데이터를 읽어오는 행위는 `next()`에서 하는 것이 좋을 듯 하다

### 참고 - `InternalRow` 자료 구조 사용법

`InternalRow` 자료 구조를 handling하는 방법은 [Spark InternalRow 자료 구조 사용법](https://jason-heo.github.io/bigdata/2020/08/15/spark-internalrow.html) 에 설명해두었다.

primitive type인 경우 큰 어려움은 없지만, Array나 Map으로 가면 사용법이 좀 어려워진다

### `MicroBatchReader` 호출 예

```scala
package io.github.jasonheo

import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.streaming.StreamingQuery

object Main {
  def main(args: Array[String]): Unit = {
    Logger.getLogger("io.github.jasonheo.sources").setLevel(Level.INFO)

    val spark = SparkSession.builder().master("local[4]").getOrCreate()

    spark.sparkContext.setLogLevel("WARN")

    val df = spark
      .readStream
      .format("io.github.jasonheo.sources.RandomIntStreamProvider")
      .option("numPartitions", "2") // partition 개수, Task로 할당된다. executor 개수가 넉넉한 경우 읽기 병렬성은 높일 수 있다
      .load()

    df.printSchema()

    import scala.concurrent.duration._

    val query: StreamingQuery = df
      .writeStream
      .format("console")
      .trigger(org.apache.spark.sql.streaming.Trigger.ProcessingTime(5.seconds))
      .outputMode("append")
      .start()

    query.awaitTermination()
  }
}
```

수행 결과는 다음과 같다. 처음 `MicroBatchReader`를 만들 때 함수들의 호출 순서와 arguement를 알 수 없어서 로그 메시지를 최대한 많이 붙여두었다. 아래 로그 메시지만 잘 분석하면 `MicroBatchReader`를 이해하는데 많은 도움이 된다.

```
root
 |-- partition_id: integer (nullable = false)
 |-- offset: long (nullable = false)
 |-- random_int: integer (nullable = false)

20/08/17 11:17:38 INFO RandomIntMicroBatchReader: setOffsetRange(start_='Optional.empty', end_='Optional.empty') called
20/08/17 11:17:38 INFO RandomIntMicroBatchReader: in getNumNewMsg(), numNewsRows='1'
20/08/17 11:17:38 INFO RandomIntMicroBatchReader: getEndOffset() called
20/08/17 11:17:38 INFO RandomIntMicroBatchReader: getEndOffset() returns '1'
20/08/17 11:17:38 INFO RandomIntMicroBatchReader: setOffsetRange(start_='Optional.empty', end_='Optional[1]') called
20/08/17 11:17:38 INFO RandomIntMicroBatchReader: planInputPartitions() called
20/08/17 11:17:39 INFO RandomIntMicroBatchInputPartitionReader: [partition-1] next() called
20/08/17 11:17:39 INFO RandomIntMicroBatchInputPartitionReader: [partition-0] next() called
20/08/17 11:17:39 INFO RandomIntMicroBatchInputPartitionReader: [partition-0] get() called
20/08/17 11:17:39 INFO RandomIntMicroBatchInputPartitionReader: [partition-1] get() called
20/08/17 11:17:39 INFO RandomIntMicroBatchInputPartitionReader: [partition-1] next() called
20/08/17 11:17:39 INFO RandomIntMicroBatchInputPartitionReader: [partition-0] next() called
20/08/17 11:17:39 INFO RandomIntMicroBatchInputPartitionReader: [partition-1] close() called
20/08/17 11:17:39 INFO RandomIntMicroBatchInputPartitionReader: [partition-0] close() called
-------------------------------------------
Batch: 0
-------------------------------------------
+------------+------+----------+
|partition_id|offset|random_int|
+------------+------+----------+
|           0|     1|         2|
|           1|     1|         8|
+------------+------+----------+

20/08/17 11:17:40 INFO RandomIntMicroBatchReader: deserializeOffset() returns '1'
20/08/17 11:17:40 INFO RandomIntMicroBatchReader: setOffsetRange(start_='Optional[1]', end_='Optional.empty') called
20/08/17 11:17:40 INFO RandomIntMicroBatchReader: in getNumNewMsg(), numNewsRows='2'
20/08/17 11:17:40 INFO RandomIntMicroBatchReader: getEndOffset() called
20/08/17 11:17:40 INFO RandomIntMicroBatchReader: getEndOffset() returns '3'
20/08/17 11:17:40 INFO RandomIntMicroBatchReader: deserializeOffset() returns '1'
20/08/17 11:17:40 INFO RandomIntMicroBatchReader: commit(end='1') called
20/08/17 11:17:40 INFO RandomIntMicroBatchReader: deserializeOffset() returns '1'
20/08/17 11:17:40 INFO RandomIntMicroBatchReader: setOffsetRange(start_='Optional[1]', end_='Optional[3]') called
20/08/17 11:17:40 INFO RandomIntMicroBatchReader: planInputPartitions() called
20/08/17 11:17:40 INFO RandomIntMicroBatchInputPartitionReader: [partition-1] next() called
20/08/17 11:17:40 INFO RandomIntMicroBatchInputPartitionReader: [partition-1] get() called
20/08/17 11:17:40 INFO RandomIntMicroBatchInputPartitionReader: [partition-0] next() called
20/08/17 11:17:40 INFO RandomIntMicroBatchInputPartitionReader: [partition-1] next() called
20/08/17 11:17:40 INFO RandomIntMicroBatchInputPartitionReader: [partition-0] get() called
20/08/17 11:17:40 INFO RandomIntMicroBatchInputPartitionReader: [partition-1] get() called
20/08/17 11:17:40 INFO RandomIntMicroBatchInputPartitionReader: [partition-0] next() called
20/08/17 11:17:40 INFO RandomIntMicroBatchInputPartitionReader: [partition-1] next() called
20/08/17 11:17:40 INFO RandomIntMicroBatchInputPartitionReader: [partition-0] get() called
20/08/17 11:17:40 INFO RandomIntMicroBatchInputPartitionReader: [partition-0] next() called
20/08/17 11:17:40 INFO RandomIntMicroBatchInputPartitionReader: [partition-1] close() called
20/08/17 11:17:40 INFO RandomIntMicroBatchInputPartitionReader: [partition-0] close() called
-------------------------------------------
Batch: 1
-------------------------------------------
+------------+------+----------+
|partition_id|offset|random_int|
+------------+------+----------+
|           0|     2|         9|
|           0|     3|         1|
|           1|     2|         1|
|           1|     3|         2|
+------------+------+----------+

20/08/17 11:17:45 INFO RandomIntMicroBatchReader: deserializeOffset() returns '3'
20/08/17 11:17:45 INFO RandomIntMicroBatchReader: setOffsetRange(start_='Optional[3]', end_='Optional.empty') called
20/08/17 11:17:45 INFO RandomIntMicroBatchReader: in getNumNewMsg(), numNewsRows='0'
20/08/17 11:17:45 INFO RandomIntMicroBatchReader: getEndOffset() called
20/08/17 11:17:45 INFO RandomIntMicroBatchReader: getEndOffset() returns '3'
20/08/17 11:17:50 INFO RandomIntMicroBatchReader: deserializeOffset() returns '3'
20/08/17 11:17:50 INFO RandomIntMicroBatchReader: setOffsetRange(start_='Optional[3]', end_='Optional.empty') called
20/08/17 11:17:50 INFO RandomIntMicroBatchReader: in getNumNewMsg(), numNewsRows='1'
20/08/17 11:17:50 INFO RandomIntMicroBatchReader: getEndOffset() called
20/08/17 11:17:50 INFO RandomIntMicroBatchReader: getEndOffset() returns '4'
20/08/17 11:17:50 INFO RandomIntMicroBatchReader: deserializeOffset() returns '3'
20/08/17 11:17:50 INFO RandomIntMicroBatchReader: commit(end='3') called
20/08/17 11:17:50 INFO RandomIntMicroBatchReader: deserializeOffset() returns '3'
20/08/17 11:17:50 INFO RandomIntMicroBatchReader: setOffsetRange(start_='Optional[3]', end_='Optional[4]') called
20/08/17 11:17:50 INFO RandomIntMicroBatchReader: planInputPartitions() called
20/08/17 11:17:50 INFO RandomIntMicroBatchInputPartitionReader: [partition-0] next() called
20/08/17 11:17:50 INFO RandomIntMicroBatchInputPartitionReader: [partition-1] next() called
20/08/17 11:17:50 INFO RandomIntMicroBatchInputPartitionReader: [partition-0] get() called
20/08/17 11:17:50 INFO RandomIntMicroBatchInputPartitionReader: [partition-1] get() called
20/08/17 11:17:50 INFO RandomIntMicroBatchInputPartitionReader: [partition-0] next() called
20/08/17 11:17:50 INFO RandomIntMicroBatchInputPartitionReader: [partition-1] next() called
20/08/17 11:17:50 INFO RandomIntMicroBatchInputPartitionReader: [partition-0] close() called
20/08/17 11:17:50 INFO RandomIntMicroBatchInputPartitionReader: [partition-1] close() called
-------------------------------------------
Batch: 2
-------------------------------------------
+------------+------+----------+
|partition_id|offset|random_int|
+------------+------+----------+
|           0|     4|         1|
|           1|     4|         9|
+------------+------+----------+

20/08/17 11:17:55 INFO RandomIntMicroBatchReader: deserializeOffset() returns '4'
20/08/17 11:17:55 INFO RandomIntMicroBatchReader: setOffsetRange(start_='Optional[4]', end_='Optional.empty') called
20/08/17 11:17:55 INFO RandomIntMicroBatchReader: in getNumNewMsg(), numNewsRows='1'
20/08/17 11:17:55 INFO RandomIntMicroBatchReader: getEndOffset() called
20/08/17 11:17:55 INFO RandomIntMicroBatchReader: getEndOffset() returns '5'
20/08/17 11:17:55 INFO RandomIntMicroBatchReader: deserializeOffset() returns '4'
20/08/17 11:17:55 INFO RandomIntMicroBatchReader: commit(end='4') called
20/08/17 11:17:55 INFO RandomIntMicroBatchReader: deserializeOffset() returns '4'
20/08/17 11:17:55 INFO RandomIntMicroBatchReader: setOffsetRange(start_='Optional[4]', end_='Optional[5]') called
20/08/17 11:17:55 INFO RandomIntMicroBatchReader: planInputPartitions() called
20/08/17 11:17:55 INFO RandomIntMicroBatchInputPartitionReader: [partition-1] next() called
20/08/17 11:17:55 INFO RandomIntMicroBatchInputPartitionReader: [partition-1] get() called
20/08/17 11:17:55 INFO RandomIntMicroBatchInputPartitionReader: [partition-1] next() called
20/08/17 11:17:55 INFO RandomIntMicroBatchInputPartitionReader: [partition-1] close() called
20/08/17 11:17:55 INFO RandomIntMicroBatchInputPartitionReader: [partition-0] next() called
20/08/17 11:17:55 INFO RandomIntMicroBatchInputPartitionReader: [partition-0] get() called
20/08/17 11:17:55 INFO RandomIntMicroBatchInputPartitionReader: [partition-0] next() called
20/08/17 11:17:55 INFO RandomIntMicroBatchInputPartitionReader: [partition-0] close() called
-------------------------------------------
Batch: 3
-------------------------------------------
+------------+------+----------+
|partition_id|offset|random_int|
+------------+------+----------+
|           0|     5|         8|
|           1|     5|         1|
+------------+------+----------+
...
...
```

{% include adsense-content.md %}

### `ContinuousReader` 구현

`MicroBatchReader`를 이해했다면 `ContinuousReader` 구현도 크게 어렵지 않다. 다만 Offset 관리 방법이 `MicroBatchReader`와 달라서 이 부분이 어렵게 느껴진다.

Offset 관련된 class는 `PartitionOffset`, `Offset` class를 상속받아 구현해야한다.


```scala
case class RandomIntContinuousPartitionOffset(partitionId: Int, offset: Long) extends PartitionOffset

case class RandomIntContinuousOffset(partitionOffsetMap: Map[Int, Long]) extends Offset {
  implicit val defaultFormats: DefaultFormats = DefaultFormats

  override val json = Serialization.write(partitionOffsetMap)
}
```

`PartitionOffset`은 partition별 offset을 저장하는 class이고, `Offset`은 모든 parittion들의 offset 정보를 모은 class이다.

그래서 `RandomIntPartitiionOffset`은 `(partitionId: Int, offset: Long)` 처럼 특정 `partitionId`의 `offset`을 저장하며,

`RandomIntContinuousOffset`는 `(partitionOffsetMap: Map[Int, Long])` 처럼 partitionId별로 offset을 저장하는 `Map[Int, Long]` 자료 구조를 사용 중이다.

또 한 가지 주의할 것은 `ContinuousInputPartitionReader.next()`는 항상 `true`를 return해야한다는 점이다. `next()`가 `false`를 return하면 stream query가 종료된다.

### `ContinuousReader` 호출 예

코드 자체는 `MicroBatchReader`와 동일하고 Trigger 부분만 `Trigger.Continuous(5.seconds)`로 변경되었다.

```
package io.github.jasonheo

import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.streaming.StreamingQuery

object Main {
  def main(args: Array[String]): Unit = {
    Logger.getLogger("io.github.jasonheo.sources").setLevel(Level.INFO)

    val spark = SparkSession.builder().master("local[4]").getOrCreate()

    spark.sparkContext.setLogLevel("WARN")

    val df = spark
      .readStream
      .format("io.github.jasonheo.sources.RandomIntStreamProvider")
      .option("numPartitions", "2") // partition 개수, Task로 할당된다. executor 개수가 넉넉한 경우 읽기 병렬성은 높일 수 있다
      .load()

    df.printSchema()

    import scala.concurrent.duration._

    val query: StreamingQuery = df
      .writeStream
      .format("console")
      .trigger(org.apache.spark.sql.streaming.Trigger.Continuous(5.seconds))
      .outputMode("append")
      .start()

    query.awaitTermination()
  }
}
```

실행 결과는 다음과 같다. 아래 로그를 보면서 함수 호출 방식을 이해하면 `ContinuousReader`의 작동 방식을 이해하는데 도움이 된다.

```
NFO RandomIntContinuousReader: setStartOffset(_start='Optional.empty') called
20/08/17 11:20:37 INFO RandomIntContinuousReader: planInputPartitions() called
20/08/17 11:20:38 INFO RandomIntContinuousInputPartitionReader: [partition-0] getOffset() called
20/08/17 11:20:38 INFO RandomIntContinuousInputPartitionReader: [partition-1] getOffset() called
20/08/17 11:20:38 INFO RandomIntContinuousInputPartitionReader: [partition-1] getOffset() returns 'RandomIntContinuousPartitionOffset(1,0)'
20/08/17 11:20:38 INFO RandomIntContinuousInputPartitionReader: [partition-0] getOffset() returns 'RandomIntContinuousPartitionOffset(0,0)'
20/08/17 11:20:38 INFO RandomIntContinuousInputPartitionReader: [partition-1] next() called
20/08/17 11:20:38 INFO RandomIntContinuousInputPartitionReader: [partition-0] next() called
20/08/17 11:20:38 INFO RandomIntContinuousReader: mergeOffsets(offsets='[Lorg.apache.spark.sql.sources.v2.reader.streaming.PartitionOffset;@30b06314') called
20/08/17 11:20:38 INFO RandomIntContinuousReader: mergeOffsets() returns {"1":0,"0":0}
-------------------------------------------
Batch: 0
-------------------------------------------
+------------+------+----------+
|partition_id|offset|random_int|
+------------+------+----------+
+------------+------+----------+

20/08/17 11:20:38 INFO RandomIntContinuousReader: deserializeOffset(json='{"1":0,"0":0}') called
20/08/17 11:20:38 INFO RandomIntContinuousReader: deserializeOffset() returns {"1":0,"0":0}
20/08/17 11:20:38 INFO RandomIntContinuousReader: commit(end='{"1":0,"0":0}') called
20/08/17 11:20:40 INFO RandomIntContinuousReader: mergeOffsets(offsets='[Lorg.apache.spark.sql.sources.v2.reader.streaming.PartitionOffset;@22876dc1') called
20/08/17 11:20:40 INFO RandomIntContinuousReader: mergeOffsets() returns {"0":0,"1":0}
-------------------------------------------
Batch: 1
-------------------------------------------
+------------+------+----------+
|partition_id|offset|random_int|
+------------+------+----------+
+------------+------+----------+

20/08/17 11:20:40 INFO RandomIntContinuousReader: deserializeOffset(json='{"0":0,"1":0}') called
20/08/17 11:20:40 INFO RandomIntContinuousReader: deserializeOffset() returns {"0":0,"1":0}
20/08/17 11:20:40 INFO RandomIntContinuousReader: commit(end='{"0":0,"1":0}') called
20/08/17 11:20:40 INFO RandomIntContinuousInputPartitionReader: [partition-0] get() called
20/08/17 11:20:40 INFO RandomIntContinuousInputPartitionReader: [partition-0] getOffset() called
20/08/17 11:20:40 INFO RandomIntContinuousInputPartitionReader: [partition-0] getOffset() returns 'RandomIntContinuousPartitionOffset(0,1)'
20/08/17 11:20:40 INFO RandomIntContinuousInputPartitionReader: [partition-0] next() called
20/08/17 11:20:41 INFO RandomIntContinuousInputPartitionReader: [partition-1] get() called
20/08/17 11:20:41 INFO RandomIntContinuousInputPartitionReader: [partition-1] getOffset() called
20/08/17 11:20:41 INFO RandomIntContinuousInputPartitionReader: [partition-1] getOffset() returns 'RandomIntContinuousPartitionOffset(1,1)'
20/08/17 11:20:41 INFO RandomIntContinuousInputPartitionReader: [partition-1] next() called
20/08/17 11:20:43 INFO RandomIntContinuousInputPartitionReader: [partition-0] get() called
20/08/17 11:20:43 INFO RandomIntContinuousInputPartitionReader: [partition-0] getOffset() called
20/08/17 11:20:43 INFO RandomIntContinuousInputPartitionReader: [partition-0] getOffset() returns 'RandomIntContinuousPartitionOffset(0,2)'
20/08/17 11:20:43 INFO RandomIntContinuousInputPartitionReader: [partition-0] next() called
20/08/17 11:20:45 INFO RandomIntContinuousReader: mergeOffsets(offsets='[Lorg.apache.spark.sql.sources.v2.reader.streaming.PartitionOffset;@12fbfe6b') called
20/08/17 11:20:45 INFO RandomIntContinuousReader: mergeOffsets() returns {"1":1,"0":2}
-------------------------------------------
Batch: 2
-------------------------------------------
+------------+------+----------+
|partition_id|offset|random_int|
+------------+------+----------+
|           1|     1|         0|
|           0|     1|         6|
|           0|     2|         5|
+------------+------+----------+

20/08/17 11:20:45 INFO RandomIntContinuousReader: deserializeOffset(json='{"1":1,"0":2}') called
20/08/17 11:20:45 INFO RandomIntContinuousReader: deserializeOffset() returns {"1":1,"0":2}
20/08/17 11:20:45 INFO RandomIntContinuousReader: commit(end='{"1":1,"0":2}') called
20/08/17 11:20:45 INFO RandomIntContinuousInputPartitionReader: [partition-1] get() called
20/08/17 11:20:45 INFO RandomIntContinuousInputPartitionReader: [partition-1] getOffset() called
20/08/17 11:20:45 INFO RandomIntContinuousInputPartitionReader: [partition-1] getOffset() returns 'RandomIntContinuousPartitionOffset(1,2)'
20/08/17 11:20:45 INFO RandomIntContinuousInputPartitionReader: [partition-1] next() called
20/08/17 11:20:45 INFO RandomIntContinuousInputPartitionReader: [partition-0] get() called
20/08/17 11:20:45 INFO RandomIntContinuousInputPartitionReader: [partition-0] getOffset() called
20/08/17 11:20:45 INFO RandomIntContinuousInputPartitionReader: [partition-0] getOffset() returns 'RandomIntContinuousPartitionOffset(0,3)'
20/08/17 11:20:45 INFO RandomIntContinuousInputPartitionReader: [partition-0] next() called
20/08/17 11:20:48 INFO RandomIntContinuousInputPartitionReader: [partition-1] get() called
20/08/17 11:20:48 INFO RandomIntContinuousInputPartitionReader: [partition-1] getOffset() called
20/08/17 11:20:48 INFO RandomIntContinuousInputPartitionReader: [partition-1] getOffset() returns 'RandomIntContinuousPartitionOffset(1,3)'
20/08/17 11:20:48 INFO RandomIntContinuousInputPartitionReader: [partition-1] next() called
20/08/17 11:20:49 INFO RandomIntContinuousInputPartitionReader: [partition-0] get() called
20/08/17 11:20:49 INFO RandomIntContinuousInputPartitionReader: [partition-0] getOffset() called
20/08/17 11:20:49 INFO RandomIntContinuousInputPartitionReader: [partition-0] getOffset() returns 'RandomIntContinuousPartitionOffset(0,4)'
20/08/17 11:20:49 INFO RandomIntContinuousInputPartitionReader: [partition-0] next() called
20/08/17 11:20:50 INFO RandomIntContinuousReader: mergeOffsets(offsets='[Lorg.apache.spark.sql.sources.v2.reader.streaming.PartitionOffset;@62a7bb24') called
20/08/17 11:20:50 INFO RandomIntContinuousReader: mergeOffsets() returns {"1":3,"0":4}
-------------------------------------------
Batch: 3
-------------------------------------------
+------------+------+----------+
|partition_id|offset|random_int|
+------------+------+----------+
|           1|     2|         9|
|           1|     3|         1|
|           0|     3|         7|
|           0|     4|         9|
+------------+------+----------+
...
...
```

{% include spark-reco.md %}
