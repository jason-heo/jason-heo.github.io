---
layout: post
title: "Spark에서 Kafka를 batch 방식으로 읽기"
categories: "bigdata"
---

Apache Spark의 공식 문서인 [Structured Streaming + Kafka Integration Guide](https://spark.apache.org/docs/3.1.2/structured-streaming-kafka-integration.html) 문서에서는 문서의 제목과는 다르게 batch 방식으로 Kafka를 연동하는 방법에 대해서도 설명을 하고 있다.

오늘은 batch 방식과 관련된 option들을 알아보려한다. (option 대부분은 streaming 방식에서도 사용할 수 있다)

### 목차

- [Streaming vs Batch 방식 비교](#streaming-vs-batch-방식-비교)
- [offset 지정 방식 1: Timestamp 방식](#offset-지정-방식-1-timestamp-방식)
- [offset 지정 방식 2: offset 방식](#offset-지정-방식-2-offset-방식)
- [`minPartitions`: task 개수](#minpartitions-task-개수)
- [offset 지정 방식과 `minPartitions`의 관계](#offset-지정-방식과-minpartitions의-관계)
- [Timestamp에 해당하는 offset 찾는 방법](#timestamp에-해당하는-offset-찾는-방법)
- [마무리](#마무리)

### Streaming vs Batch 방식 비교

아래의 많이 봤을 코드인데 Kafka `topic1`의 data를 streaming 방식으로 처리하는 예이다.

```scala
val df = spark
  .readStream
  .format("kafka")
  .option("kafka.bootstrap.servers", "host1:port1,host2:port2")
  .option("subscribe", "topic1")
  .load()
```

([출처](https://spark.apache.org/docs/3.1.2/structured-streaming-kafka-integration.html#creating-a-kafka-source-for-streaming-queries))

아래 코드는 동일 topic을 batch 방식을 처리하는 예이다.

```scala
// Subscribe to 1 topic defaults to the earliest and latest offsets
val df = spark
  .read
  .format("kafka")
  .option("kafka.bootstrap.servers", "host1:port1,host2:port2")
  .option("subscribe", "topic1")
  .load()
```

([출처](https://spark.apache.org/docs/3.1.2/structured-streaming-kafka-integration.html#creating-a-kafka-source-for-batch-queries))

`readStream()`이냐 `read()`냐의 차이만 있을 뿐 별다른 처리가 없다 (query를 수행할 때 약간의 차이가 있다)

`load()` 결과는 동일하게 `Dataset`이므로 business logic은 query의 type이 streaming, batch 구분없이 동일 코드를 작성할 수 있다.

그리고 주석에서 볼 수 있듯이 `earliest`부터 `latest`까지의 data를 읽게 된다.

Spark에서는 Kafka에서 Data를 읽기 위한 다양한 옵션을 제공하는데 이에 대해 알아보자.

### offset 지정 방식 1: Timestamp 방식

`startingOffsetsByTimestamp`과 `endingOffsetsByTimestamp`를 이용하면 Timestamp 기반으로 읽을 Data 범위를 지정할 수 있다.

여기서 Timestamp는 밀리초 단위의 timestamp이다. 즉, 이 글을 작성 중인 `2021년 12월 18일 23시 14분 53초`의 Timestamp는 `1639836893000`이다.

이때 `startingOffsetsByTimestamp`를 지정하는 방법은 다음과 같다. (`topic1`을 읽을 예정이고 `topic1`에는 partition이 2개있다고 가정한다)

```json
val df = spark
  .read
  .format("kafka")
  .option("kafka.bootstrap.servers", "host1:port1,host2:port2")
  .option("subscribe", "topic1,topic2")
  .option("startingOffsetsByTimestamp", """{"topic1": {"0": 1639836893000, "1": 1639836893000}}""")
```

(참고로 `endingOffsetsByTimestamp`는 지정하지 않았으므로 `latest`가 된다)

이쯤에서 한 가지 궁금한 것이 생길 것이다. Timestamp에 정확히 일치하는 메시지가 없는 경우 어떻게 작동할까?

<img style="width: 100%; max-width: 549px;" src="https://i.imgur.com/IfrMaMS.png" />

이에 대한 대답은 Spark Manual에 다음과 같이 적혀있다.

> Spark simply passes the timestamp information to `KafkaConsumer.offsetsForTimes`, and doesn't interpret or reason about the value
> For more details on KafkaConsumer.offsetsForTimes, please refer [javadoc](https://kafka.apache.org/21/javadoc/org/apache/kafka/clients/consumer/KafkaConsumer.html#offsetsForTimes-java.util.Map-) for details

javadoc을 방문해보면 다음과 같이 나와있다.

> The returned offset for each partition is the earliest offset whose timestamp is greater than or equal to the given timestamp

그렇다. 위 그림 상황에서는 `msg2`의 offset이 반환된다.

### offset 지정 방식 2: offset 방식

`startingOffsets`, `endingOffsets`도 지원한다. offset 방식은 timestamp 방식과 다르게 말그대로 offset을 지정하는 방식이다.

```json
val df = spark
  .read
  .format("kafka")
  .option("kafka.bootstrap.servers", "host1:port1,host2:port2")
  .option("subscribe", "topic1,topic2")
  .option("startingOffsetsByTimestamp", """{"topic1": {"0": 1234, "1": 5678}}""")
```

위 예에서는 `0`번 partition은 `1234`번 offset부터 읽는 것을, `1`번 partition은 `5678`번 offset부터 읽겠다는 것을 의미한다.

참고로 `-2`는 `earliest`를 의미하고,  `-1`은 `latest`를 의미한다.

아마 '읽고자하는 offset'을 지정할 수 있는 사람은 없을 것이다. 읽고자하는 offset를 Kafka API를 이용하여 조회한 뒤 `startingOffsets`에 전달하는 것이 편할 것이다.

본인이 선호하는 방식은 offset 방식이다. 계속해서 그 이유를 알아보자.

### `minPartitions`: task 개수

설정 이름은 `minPartitions`이지만 "task 개수"로 이해하면 쉽다. 여기서 중요한 것은 "partition"이라는 단어가 "Spark의 Parittion 개수"라는 것이다.  이 옵션을 지정하지 않은 경우에는 "Spark의 Partition 개수"는 "Kafka Topic Partition 개수"와 동일하다.

Spark은 Partition 개수만큼 task를 생성하고, core들에게 이들 task를 할당한다.

Streaming의 경우, micro batch 간격이 10초라고 하면 "10초 동안 쌓인 데이터"만 읽으면 된다. 10초 동안 10만건이 쌓였고, Kafka topic의 partition 개수가 10개라면 task당 메시지 건수는 1만건이 된다. 일반적인 workload에서 core 1개가 1만건 처리하는 건 크게 문제가 되지 않는다.

그런데 batch 방식에서 1시간치 데이터를 처리한다고 하자. 1시간은 3,600초이므로 3,600만개의 데이터를 처리해야한다. Kafka topic의 partition이 10개라고 하였으므로 task당 360만개의 메시지를 처리해야한다. 할당된 메모리가 적은 경우 문제가될 수 있다. 또한 Spark UI에서 처리 진행률을 확인하기 어렵다.

이때 사용할 수 있는 것이 `minPartitions`이다. `minPartitions: 3600`으로 지정한 경우 총 3,600개의 task가 생성되고 task 당 1만개의 메시지가 저장된다.

### offset 지정 방식과 `minPartitions`의 관계

앞에서 확인한 것처럼 batch 방식에서 넓은 범위를 읽을 때는 `minPartitions`를 지정하는 것이 좋아보인다.

그런데 `minPartitions`는 읽을 범위에 dependent한 값이다. 따라서 user friendly한 방법은 `minPartitions`를 user가 입력하지 않고, 자동으로 계산하는 것이다. 읽을 메시지 개수만 알고 있다면 `minPartitions`는 쉽게 계산할 수 있다.

그런데 offset을 Timestamp 기반으로 지정하는 경우 몇 개의 메시지를 읽게 될지 알 수 없다. 하지만, offset을 "offset 방식"으로 지정하는 경우 (말이 재귀적인데 즉, `startingOffsets`, `endingOffsets`를 사용하는 경우)에는 내가 읽어야할 메시지 개수를 정확히 알 수 있다. (partition별 `(endingOffsets - startingOffsets)`의 합계)

이것이 위에서 본인이 "본인이 선호하는 방식은 offset 방식이다"라고 말한 이유이다.

### Timestamp에 해당하는 offset 찾는 방법

Timestamp 방식보다는 `startingOffsets` 같은 방식의 장점이 커보인다. 그런데 user가 offset을 직접 입력하는 것은 user friendly하지 않다.

다행히 Kafka API를 사용하면 Timestamp에 해당하는 offset을 쉽게(?) 찾을 수 있다. 여러 방법이 있을 것 같은데 본인은 Kafka AdminClient API를 사용했다.

좀 더 구체적으로는 [`AdminClient.listOffsets()`](https://kafka.apache.org/26/javadoc/org/apache/kafka/clients/admin/KafkaAdminClient.html#listOffsets-java.util.Map-org.apache.kafka.clients.admin.ListOffsetsOptions-)를 이용하면 된다. `listOffsets()`는 `OffsetSpec`를 인자로 전달받는데, [`OffsetSpec.forTimestamp()`](https://kafka.apache.org/26/javadoc/org/apache/kafka/clients/admin/OffsetSpec.html#forTimestamp-long-)를 이용하면 Timestamp에 해당하는 offset을 찾을 수 있다. (이것도 정확히는 "The earliest offset whose timestamp is greater than or equal to the given timestamp"이다)

본인도 java doc만 가지고는 구현이 어려워서 구글링하면서 남들의 코드를 참고했다. AdminClinet 관련해서는 인터넷에 많은 자료들이 존재하고 대략 [AdminClientExample.java](https://github.com/gwenshap/kafka-examples/blob/master/AdminClientExample/src/main/java/org/example/AdminClientExample.java)를 보면서 부족한 것은 다시 구글링해보면 될 듯하다.

참고로 `OffsetSpec.forTimestamp(ts1)`에서 return된 offset부터 consume하더라도, 실제론 `ts1`보다 더 이전의 message가 소비될 수 있다.

아래의 이미지는 [What's the time? ...and why? (Mattias Sax, Confluent)](https://www.slideshare.net/ConfluentInc/whats-the-time-and-why-mattias-sax-confluent-kafka-summit-sf-2019)의 28 페이지에서 발췌한 그림이다.

<img style="width: 100%; max-width: 549px;" src="https://i.imgur.com/y8OKc7G.png" />

stream 처리에서는 out-of-order가 발생할 수 있기 때문에 `OffsetSpec.forTimestamp(10)`로 호출하더라도 timestamp가  `5`, `7`인 메시지가 소비된다.

하지만 "The earliest offset"를 return하므로 timestamp가 `18`인 message의 offset이 return되지 않는다. 따라서 timestamp `10`, `15` message가 유실되지는 않는다.

만약 partition의 Max Timestamp보다 더 큰값이 인자로 들어오는 경우 `-1`을 return한다. 그렇다. `latest`를 의미하는 offset이다.

그런데 `OffsetSpec.forTimestamp()` 방식의 Time Complexity가 궁금하다. 첫 번째 segment부터 sequential하게 scan한다면 O(n)이라서 속도가 느릴 것 같은데 어떻게 구현되었는지 궁금해진다.

Kafka 소스코드에서 [`LogSegment.findOffsetByTimestamp()`](https://github.com/a0x8o/kafka/blob/85b06cc913a47cc57d9d6bea0631e716c8ab73cd/core/src/main/scala/kafka/log/LogSegment.scala#L557-L585)를 보면 segment의 metadata 중에 timestamp를 이용하여 특정 segment는 바로 pruning할 수 있고, target timestamp를 포함한 segment인 경우 sequential scan을 하는 것 같다. 이렇게 되면 Time Complexity는 `O(1개 segment에 저장된 message 개수)`가 될 것 같다. (정확하게는 segment pruning 비용도 포함해야함)

### 마무리

Spark에서 batch 방식으로 Kafka 데이터를 읽을 수 있다면 여러 용도로 활용될 수 있을 것 같다. 혹은 use-case가 없더라도 streaming에서 사용되는 옵션들을 제대로 이해하는 계기가 될 것이다.

이 글을 적으면서 몇 가지 구현을 하다보니 Unit Test용 Kafka Broker가 있다면 좋겠다는 생각이 들었다. 시간되면 이에 대해 조사후 포스팅 해봐야겠다.

(내용 추가: Test 관련된 것은 [Kafka Unit Test with EmbeddedKafka](https://jason-heo.github.io/bigdata/2021/12/19/kafka-unit-test.html)에 작성해두었다)

{% include spark-reco.md %}
