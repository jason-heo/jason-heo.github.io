---
layout: post
title: "Kafka & Spark Batch Processing"
categories: "bigdata"
---

Kafka는 Spark에서 Ingestion을 할 때 주로 Streaming Processing의 Data Source로 사용이 된다. 하지만, Batch Processing에서도 사용될 수 있다는 사실.

### Spark 1.x의 DStream과 유사한 Batch

- https://medium.com/@sathishjayaram/batch-processing-of-multi-partitioned-kafka-topics-using-spark-with-example-b686676d33f1
    - `KafkaUtils.createRDD()`를 사용하는 방법 안내
    - github 링크를 클릭하면 코드가 보인다

### Spark 2.x의 Structured Streaming과 유사한 Batch

- https://dzone.com/articles/kafka-gt-hdfss3-batch-ingestion-through-spark
    - Batch 읽기에 대한 자료
    - 읽을 범위 지정은 `startingOffsets`과 `endingOffsets` 옵션을 사용한다
- https://sparkbyexamples.com/spark-batch-processing-produce-consume-kafka-topic/
    - Batch 읽기만이 아니라 쓰기에 대한 설명도 포함된 자료
