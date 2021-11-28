---
layout: post
title: "Spark 3.2.에서 Session Window를 지원하다"
categories: "bigdata"
---

Spark 3.1까지만해도 Spark에서는 Session Window를 제대로 지원하지 않았다. 그런데 Spark 3.2부터 드디어 event time 기반의 Session Window를 지원하기 시작했다.

### Spark 3.1까지의 Session Window 구현 방법

[`StructuredSessionization.scala`](https://github.com/apache/spark/blob/branch-3.1/examples/src/main/scala/org/apache/spark/examples/sql/streaming/StructuredSessionization.scala)에 있는 것처럼 `mapGroupsWithState`나 `flatMapGroupsWithState`를 이용해서 개발자가 직접 Session Window를 구현해야했다.

그런데 예제에 나와있는 것은 제대로된 Session Window가 아니다. Stream에서 event time 기반의 Session Window를 구현하려면 lateness, out-of-order를 고려해야하는데 이에 대한 고려가 전혀 안되어있다.

[Is proper event-time sessionization possible with Spark Structured Streaming?](https://stackoverflow.com/q/51810460/2930152)라는 Stack Overflow의 질문처럼 Spark 3.1 이하 버전에서 Session Window를 구현하기는 꽤나 어려운 작업이다.

### Spark 3.2부터 Session Window 지원

2021.10.13.에 Spark 3.2.0이 출시되었다. [release note](https://spark.apache.org/releases/spark-release-3-2-0.html)를 보면 "EventTime based sessionization (session window)"라는 내용이 눈에 들어온다.

이슈 번호는 [SPARK-10816](https://issues.apache.org/jira/browse/SPARK-10816)으로서 2015년에 생성된 오래된 이슈인데 드디어 지원이 시작되었다.

(개발자는 자랑스럽게도 한국의 [임정택님](https://www.linkedin.com/in/heartsavior/?originalSubdomain=kr)이시다.)

사용방법은 [Native Support of Session Window in Spark Structured Streaming](https://databricks.com/blog/2021/10/12/native-support-of-session-window-in-spark-structured-streaming.html)라는 Databricks 블로그에 올라와있다.

```python
# session window
windowedCountsDF = \
  eventsDF \
    .withWatermark("eventTime", "10 minutes") \
    .groupBy("deviceId", session_window("eventTime", "5 minutes")) \
    .count()
```

이제는 `mapGroupsWithState()`를 이용해서 복잡한 로직을 구현하지 않아도 Session Window를 쉽게 사용할 수 있다.
