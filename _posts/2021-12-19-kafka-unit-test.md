---
layout: post
title: "Unit Test with Kafka"
categories: "bigdata"
---

그동안 Kafka AdminClient API를 사용하면서 Unit Test를 작성하지 않았었다. 지금이라도 Unit Test를 적용하고 싶어서 찾아본 내용을 정리한다.

### 목차

- [들어가며](#들어가며)
- [Kafka Unit Test 방법들](#kafka-unit-test-방법들)
- [`EmbeddedKafka` 사용법](#embeddedkafka-사용법)

### 들어가며

본인이 Kafka Unit Test로 작성하고 싶은 것은 본인 블로그의 [Spark에서 Kafka를 batch 방식으로 읽기](/bigdata/2021/12/18/spark-kafka-batch-mode.html)에 나온 것처럼 "Timestamp를 기반으로하여 `startingOffsets` 옵션을 지정"하는 것이다.

이를 위해 우선 "AdminClient API"를 사용할 수 있어야하고, test용 data를 준비할 때 Timestamp를 지정할 수 있어야한다.

### Kafka Unit Test 방법들

구글링해본 결과 크게 다음과 같이 3가지 방법이 있는 것 같다.

- 방법1: `MockProducer`, `MockConsumer` 사용
    - 관련 자료1: https://www.baeldung.com/kafka-mockproducer
    - 관련 자료2: https://www.baeldung.com/kafka-mockconsumer
- 방법 2: `EmbeddedKafka` 사용
    - 관련 자료1: https://github.com/embeddedkafka/embedded-kafka
    - 관련 자료2: [Unit Testing in Kafka](https://dzone.com/articles/unit-testing-of-kafka)
- 방법3: Kafka 소스코드의 test에서 제공하는 기능 활용
    - (이름을 맞게 지었는지 모르겠다)
    - 관련 자료1: https://gist.github.com/asmaier/6465468
    - 관련 자료2: https://gist.github.com/qudongfang/9fac0750e5715cb8c46b

간단하게 살펴본 결과 다음과 같은 특징이 있는 것으로 보인다

- 방법1: `MockProducer`, `MockConsumer` 사용
    - 별도의 서버를 운영할 필요가 없다보니 test 속도가 빠르다
- 방법2: `EmbeddedKafka` 사용
    - 실제 Kafka Broker를 실행하지 않고 in-memory에만 data를 저장하는 것 같다
    - 따라서 실제 서버를 TC에서 start/stop하는 것보다는 빠를 것 같다
- 방법3: Kafka 소스코드의 test에서 제공하는 기능 활용
    - Zookeeper는 mock server를 사용하는 것 같지만, Kafka Broker는 실제로 서버를 실행하는 것 같다
    - 따라서 속도가 느릴 것 같다
    - 장점은  `MockTime`을 지정할 수 있다는 점이다

우선 "방법 1"은 탈락이다.

### `EmbeddedKafka` 사용법

"방법 2" 즉, EmbeddedKafka를 테스트해봤는데 AdminClient가 잘 작동하였다. `kafka-console-consumer.sh`에서 소비가 잘 되는 걸 봐선 일반적인 kafka-client API와도 잘 작동할 것이다.

전체 코드는 다음과 같고, github에도 올려두었다. ([repository 바로가기](https://github.com/jason-heo/embedded-kafka-test))

```scala
package io.github.jasonheo

import net.manub.embeddedkafka.{EmbeddedKafka, EmbeddedKafkaConfig}
import org.apache.kafka.clients.admin.{AdminClient, ListOffsetsOptions, OffsetSpec, TopicDescription}
import org.apache.kafka.clients.consumer.{ConsumerRecord, KafkaConsumer}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.{Deserializer, Serializer, StringDeserializer, StringSerializer}
import org.scalatest.{Assertion, FlatSpec, Matchers}

import java.util.{Collections, Properties}
import scala.concurrent.duration.DurationInt
import scala.collection.JavaConverters._

class EmbeddedKafkaTest extends FlatSpec with Matchers with EmbeddedKafka {
  "AdminClient API" must "work well with EmbeddedKafka" in {
    implicit val config = EmbeddedKafkaConfig(kafkaPort = 9092, zooKeeperPort = 2182)

    implicit val serializer: Serializer[String] = new StringSerializer()
    implicit val deserializer: Deserializer[String] = new StringDeserializer()

    withRunningKafka {
      val topic = "topic1"

      val numPartitions = 2
      val replicaFactor = 1

      // 참고: 명시적으로 topic을 생성하지 않더라도 produce/consume이 가능했다
      // 본인의 경우 partition 개수를 2개로 지정하기 위하여 명시적으로 topic을 생성했다
      createCustomTopic(topic, Map[String, String](), numPartitions, replicaFactor)

      EmbeddedKafka.withProducer[String, String, Unit]((producer: KafkaProducer[String, String]) => {
        produceRecord(topic, producer, 0, 100L)
        produceRecord(topic, producer, 0, 110L)
        produceRecord(topic, producer, 1, 200L)
        produceRecord(topic, producer, 1, 210L)
        produceRecord(topic, producer, 1, 220L)
      })

      EmbeddedKafka.withConsumer[String, String, Assertion]((consumer: KafkaConsumer[String, String]) => {
        consumer.subscribe(Collections.singletonList(topic))

        // 참고 1: EmbeddedKafka의 README에서 아래 block은 `eventually`로 감싸져있었다
        // 참고 2: EmbeddedKafka의 README에서 poll()의 timeout이 1초였으나, 환경에 따라 1초가 부족한 듯하여 넉넉히 10으로 늘렸다
        //  - 이는 "10초 동안 blocking이 되는 것"을 의미하지 않는다
        //  - 다만, 뭔가 실수가 있는 경우 message가 인입되지 않으므로 10초 동안 멈췄다가 Test가 실패할 수 있다
        //  - 예) 존재하지 않는 partition 번호를 적은 경우 등
        val records: Iterable[ConsumerRecord[String, String]] = consumer.poll(java.time.Duration.ofMillis(10.seconds.toMillis)).asScala

        records.foreach(record => {
          // println() 결과
          //
          // partition=1, timestamp=200, offset=0, key=9128, value=8911
          // partition=1, timestamp=210, offset=1, key=7444, value=5715
          // partition=1, timestamp=220, offset=2, key=6943, value=9530
          // partition=0, timestamp=100, offset=0, key=8490, value=5978
          // partition=0, timestamp=110, offset=1, key=3768, value=4133
          println(
            s"partition=${record.partition()}, " +
            s"timestamp=${record.timestamp()}, " +
            s"offset=${record.offset()}, " +
            s"key=${record.key()}, " +
            s"value=${record.value()}"
          )
        })

        records.size should be(5)
      })

      val adminClient: AdminClient = getAdminClient()

      getNumPartitions(adminClient, topic) should be(2)

      getOffsetOfTimestamp(adminClient, topic, partitionNum=0, timestamp=105L) should be(1)
      getOffsetOfTimestamp(adminClient, topic, partitionNum=1, timestamp=215L) should be(2)

      Thread.sleep(100000)

      adminClient.close()
    }
  }

  private def produceRecord(topic: String,
                            producer: KafkaProducer[String, String],
                            partitionNum: Int,
                            timestamp: Long): Unit = {
    val rnd = scala.util.Random

    val key = rnd.nextInt(10000).abs.toString
    val msg = rnd.nextInt(10000).abs.toString

    val record = new ProducerRecord[String, String](topic, partitionNum, timestamp, key, msg)

    producer.send(record)
  }

  private def getAdminClient(): AdminClient = {
    val props: Properties = new Properties()
    props.put("bootstrap.servers", "localhost:9092")

    val adminClient: AdminClient = AdminClient.create(props)

    adminClient
  }

  private def getNumPartitions(adminClient: AdminClient, topic: String): Int = {
    val topicMetaData: java.util.Map[String, TopicDescription] = adminClient
      .describeTopics(Collections.singletonList(topic))
      .all
      .get

    val numPartitionsOfTopic: Int = topicMetaData
      .get(topic)
      .partitions()
      .size()

    numPartitionsOfTopic
  }

  private def getOffsetOfTimestamp(adminClient: AdminClient,
                                   topic: String,
                                   partitionNum: Int,
                                   timestamp: Long): Long = {
    import scala.collection.JavaConverters._

    val topicPartition = new TopicPartition(topic, partitionNum)

    val topicPartitionOffsets: Map[TopicPartition, OffsetSpec] = Map(
      topicPartition -> OffsetSpec.forTimestamp(timestamp)
    )

    val offsets = adminClient.listOffsets(topicPartitionOffsets.asJava, new ListOffsetsOptions()).all.get

    val offset: Long = offsets.get(topicPartition).offset()

    offset
  }
}
```

{% include spark-reco.md %}
