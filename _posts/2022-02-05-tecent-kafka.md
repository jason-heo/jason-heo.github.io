---
layout: post
title: "How Tencent PCG Uses Apache Kafka to Handle 10 Trillion+ Messages Per Day"
categories: "bigdata"
---

오늘 작성한 블로그 포스트는 별 영양가가 없지만, 그래도 공부한 기록을 남기기 위해 작성해봤다.

[How Tencent PCG Uses Apache Kafka to Handle 10 Trillion+ Messages Per Day](https://www.confluent.io/blog/tencent-kafka-process-10-trillion-messages-per-day/)라는 글을 읽어봤다. 주내용은 multiple kafka cluster를 논리적으로 1개의 cluster처럼보여주는 federated kafka를 만들었다는 것 같다.

<img style="width: 100%; max-width: 800px;" src="https://i.imgur.com/sgF9BWs.png" />

이를 통해 kafka의 몇 가지 제약을 해결했다고 해서 읽어봤는데 잘 이해가 안 된다.

예를 들어 kafka의 확장 이슈 중 하나로 본문에 나온 것처럼 "expanding the capacity of a cluster (i.e., adding brokers) requires significant data rebalancing"가 있다. 

그런데 저자들에 따르면 "we can easily deploy two additional clusters without shuffling any existing data"라고 하는데 rebalance 문제를 어떻게 해결했다는 것인지 이해가 안된다.

궁금해서 관련된 Youtube 영상이라던가 다른 글을 검색해봤는데 나오지 않는다. tencent cloud에서 [CKafka](https://intl.cloud.tencent.com/ko/products/ckafka)라는 걸 제공하는데 여기에 녹아든 기술인 것인지.
