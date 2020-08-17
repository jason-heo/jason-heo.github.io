---
layout: post
title: "Custom Spark Stream Source 개발하기"
categories: "bigdata"
---

최근 (2020/08)에 structured streaming을 위한 data source를 구현 테스트하게 되었다. 이때 참고했던 자료들 몇 개를 기록용으로 정리해둔다.

## 2020.08.17. 내용 추가

`MicroBatchReader`, `ContinuousReader`에 대한 작동 방식 및 예제 프로그램은 http://jason-heo.github.io/bigdata/2020/08/15/spark-stream-source-v2.html 에서 볼 수 있다.

## 개요

Spark은 data processing 엔진일 뿐 data storage가 아니다. Spark에서 data를 읽기 위해선 해당 data를 읽을 수 있는 data source가 구현되어 있어야 한다.

예를 들어, Spark에서 Parquet 파일을 읽을 수 있는 이유는 Parquet용 Data Source가 구현되어있기 때문이다. DataFrame 등의 자료 구조를 Elasticsearch에 저장할 수 있는 이유도 es-hadoop이라는 "Spark에서 ES에 데이터를 읽고/쓸 수 있는" data source가 존재하기 때문이다.

만약 내가 Spark에서 처리하고 싶은 Data가 있는데, Spark에서 이를 지원하지 않는다면 직접 Data Source API를 이용하여 구현해야한다.

2017년 말 경에 Data Source를 붙여본 경험이 있었고 이때 읽은 자료는 [Spark에 Custom Data source 붙이기](http://jason-heo.github.io/bigdata/2018/01/06/spark-data-source-api-v1.html)에 정리해두었다.

위의 문서는 Data Source API v1에 대한 예이고, Spark 2.3부터는 Spark 2.3부터는 Data Source API v2를 제공하고 있다. v1의 한계점과 v2 기능에 대한 소개는 [Spark Data Source API V2 소개](http://jason-heo.github.io/bigdata/2018/01/08/spark-data-source-api-v2.html)에 정리해두었다.

## DStreaming 용 Data Source 구현하기

DStreaming 방식에서는 Custom Receiver를 구현하면 된다.

https://spark.apache.org/docs/2.4.0/streaming-custom-receivers.html

장점: 개념이나 구현이 어렵지 않고, read 병렬성도 쉽게 조절 가능하다.

단점: 아쉽게도 structured streaming에서 사용이 불가능하다.

## Structured Stream read용 Data Source 구현하기

- [Spark Custom Stream Sources](https://hackernoon.com/spark-custom-stream-sources-ec360b8ae240)
    - 제일 쉽게 접근 가능한 자료
    - multi threading에 관한 코드도 잘 작성되어 있어서 쉽게 확장이 가능하다
    - batch 처리용 data source 개발할 때 hackernoon에 있는 자료를 참고했었는데, 그때 그 개발자가 작성한 글이다
    - 단계별로 잘 설명되어있다
- 그런데 위 자료를 보면 알겠지만, 데이터를 읽는 병렬성이 1이다
    - `ListBuffer`에 data를 채우는 thread 개수를 늘리면 되겠지만 그래도 한계는 있다
    - driver가 실행 중인 장비의 core 개수 이상의 병렬성을 얻을 수 없다
    - 그리고 해당 장비가 driver 전용으로 사용된다는 걸 보장할 수도 없다
- [`KafkaSource.scala`](https://github.com/apache/spark/blob/branch-2.2/external/kafka-0-10-sql/src/main/scala/org/apache/spark/sql/kafka010/KafkaSource.scala)
    - Spark 2.2 기준의 이야기이다
    - read 병렬성 문제에 대한 해결책은 KafkaSource를 참고하면 된다
    - `[KafkaSource.getBatch()` 함수](https://github.com/apache/spark/blob/7c7d7f6a878b02ece881266ee538f3e1443aa8c1/external/kafka-0-10-sql/src/main/scala/org/apache/spark/sql/kafka010/KafkaSource.scala#L292-L303)를 보면 알겠지만, `getBatch()` 호출 시에 [`KafkaSourceRDD`](https://github.com/apache/spark/blob/branch-2.2/external/kafka-0-10-sql/src/main/scala/org/apache/spark/sql/kafka010/KafkaSourceRDD.scala)를 생성한다
    - 즉, micro batch가 시작될 때 kafka topic partition 개수 만큼의 spark partition을 만들어서 data를 읽어온다
    - 따라서, executor 개수만 충분하다면 읽기 병렬성을 높일 수 있다
- 읽기 성능 문제
    - DStream의 custom receiver는 micro batch가 시작하지 않더라도 exectuor에서 custom receiver를 계속 수행하면서 data를 읽어서 buffer에 채우고 있다
    - 즉, micro batch가 시작되었을 때는 이미 data를 읽었기 때문에 지연이 짧다
    또한 hackernoon의 예제 코드에서도 driver의 background thread에서 지속적으로 data를 채우고 있기 때문에 trigger가 되었을 때는 데이터가 이미 존재하고 있다
    - 하지만, KafkaSource의 경우 trigger가 된 시점에서야 Kafka로부터 Data를 읽어오기 때문에 약간의 지연이 발생할 수 밖에 없다
    - 또한 KafkaSource의 경우 Kafka에서 읽어온 자료 구조가 RDD인데, Structured Streaming에서는 DataFrame으로 return하므로 RDD를 DataFrame으로 변환하는 비용까지 합치면 지연이 더 늘어날 수 밖에 없다
    - 이런 지연은 분명히 Kafka Streams나 Flink 대비 Spark의 약점일 수 밖에 없다
- Continuous Stream
    - Spark 2.3부터 continuous stream을 지원하기 시작했다
    - trigger 시점에 data를 읽는 방식이 아니라 custom receiver 방식과 유사하게 작동하는 것으로 보인다
    - continuous stream부터 Stream Source API에도 변화가 생겼다
    - Stream Source v2라고도 불러도 될 듯 하다
    - 이에 대한 것은 [`RateStreamProvider.scala`](https://github.com/apache/spark/blob/branch-2.4/sql/core/src/main/scala/org/apache/spark/sql/execution/streaming/sources/RateStreamProvider.scala)를 보면 좋다
    - class 선언부는 다음과 같이 생겼다
        ```scala

		class RateStreamProvider extends DataSourceV2
		  with MicroBatchReadSupport with ContinuousReadSupport with DataSourceRegister {
		...
		}
		```
    - 즉, `MicroBatchReadSupport`와 `ContinuousReadSupport`과 마찬자지로 명시적으로 batch read와 continuous read용 API가 나뉘어 있다
    - batch read를 지원할 때는 `MicroBatchReadSupport`만 구현하면 되고, continuous read를 지원할 때는 `ContinuousReadSupport`를 구현하면 된다. 원한다면 둘 다 구현하면 된다
    - `ContinuousReadSupport`를 이용하면 지연없이 데이터를 빠르게 읽어올 수 있다
- revisit 'read 병렬성 문제'
    - `ContinuousReader`
        - read 병렬성을 executor 개수만큼 높일 수 있다
        - partition 개수가 executor 개수보다 많으면 된다
        - `planInputPartitions()`에서 partition 개수를 return하면 된다
        - "rate" Source 사용 시 `option("numPartitions", "10")` 처럼 partition 개수를 여러 개 지정한 뒤에  Spark UI에서 Active Tasks를 보면, trigger랑 상관없이 executor가 partition 개수만큼 계속 실행 중이다
        - 즉, DStream에서 custom receiver의 작동 방법과 동일하다
    - `MicroBatchReader`
        - 이건 아직 read 병렬성에 대해서 잘 모르겠다
        - partition 개수만큼 자동으로 executor들을 할당해주면 좋은 데 그렇게 작동하는지 확실치 않음
        - `ContinuousReader`처럼 작동하면 좋은데 그렇지 않다면 아직은 `StreamSourceProvider` 대비 큰 장점은 없어보인다
        - `MicroBatchReader`에도 분명 `planInputPartitions()`가 있긴 한데, trigger마다 partition 개수 만큼의 executor들이 작동을 하게 되는 것인지 아닌지 확실치 모르겠다
- Spark 2.4에서의 `KafkaSourceProvider`
    - `KafkaSourceProvider`의 구현 방법이 Spark 2.2와 Spark 2.4의 구현이 완전 바뀌었다
        - [Spark 2.2의 `KafkaSourceProvider` 소스 코드](https://github.com/apache/spark/blob/branch-2.4/external/kafka-0-10-sql/src/main/scala/org/apache/spark/sql/kafka010/KafkaSourceProvider.scala)
        - [Spark 2.4의 `KafkaSourceProvider` 소스 코드](https://github.com/apache/spark/blob/branch-2.2/external/kafka-0-10-sql/src/main/scala/org/apache/spark/sql/kafka010/KafkaSourceProvider.scala)
    - class 선언부만 확인해보자
        - Spark 2.2
            ```scala
			private[kafka010] class KafkaSourceProvider extends DataSourceRegister
				with StreamSourceProvider // <==== 요 부분이 읽는 것과 관련
				with StreamSinkProvider
				with RelationProvider
				with CreatableRelationProvider
				with Logging {
			```
		- Spark 2.4
			```scala
			private[kafka010] class KafkaSourceProvider extends DataSourceRegister
				with StreamSourceProvider
				with StreamSinkProvider
				with RelationProvider
				with CreatableRelationProvider
				with StreamWriteSupport
				with ContinuousReadSupport
				with MicroBatchReadSupport // <=== 요 부분이 읽는 것과 관련
				with Logging {
			```
    - 그렇다, Spark 2.4로 가면서 앞에서 이야기했던 `MicroBatchReadSupport`를 구현 중이다
    - 그렇다면, 이걸 잘 분석해보면 내가 궁금해하는 `MicroBatchReader`에서의 읽기 병렬성이 해도될 듯 하다
    - 코드를 읽어보면 kafka topic partition 마다 `KafkaMicroBatchInputPartitionReader` instance를 만들고, 그 안에서 kafka consumer를 만든다
        - Spark 2.2의 `StreamSourceProvider` 방식에서 봤던 `KafkaSourceRD`도 안 보인다
        - 이걸 봐선 정황상으로는 topic partition들을 executor들에게 할당하고 개별 executor에서 kafka로부터 데이터를 읽어들이는 것 같다
        - 정확한 것은 아무래도 테스트를 해봐야겠다
