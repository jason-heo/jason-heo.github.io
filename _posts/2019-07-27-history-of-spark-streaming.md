---
layout: post
title: "Spark Streaming의 History"
categories: "bigdata"
---

Spark Streaming에서는 다음과 같은 Streaming Processing 방식이 있다.

1. "Classic" Spark Streaming
    - Spark 1.x에서 사용
    - DStream (혹은 Discretized Stream)
    - "Classic" Streaming이라는 용어는 없으나, 예전이라는 것을 강조하기 위해 사용했음 (https://youtu.be/oMZwVRdDBJI)
1. Structured Streaming
    - Spark 2.0에서 도입
    - "Classic" Streaming의 단점 보완
1. Continuous Processing in Structured Streaming
    - Spark 2.3에서 도입
    - sub-millisecond end-to-end latency

각 지원 프로세싱 방식의 특징과 문제점을 이해하고, 그 다음 버전에서는 어떻게 해결했는지를 이해하는 게 중요하다. 이를 위해 몇 가지 블로그 포스팅을 준비해보았다.

1. Introduction to Structured Streaming
  - https://youtu.be/oMZwVRdDBJI?t=575
  - DStreaming의 문제점: 9분 35초
      - 이 부분을 이해하는 게 제일 중요하다 생각한다
1. Introduction to Structured Streaming
  - https://www.slideshare.net/datamantra/introduction-to-structured-streaming-80809652
1. Structured Streaming에서의 Water marking
  - https://databricks.com/blog/2017/05/08/event-time-aggregation-watermarking-apache-sparks-structured-streaming.html
1. Deep dive into Structured Streaming
  - https://www.youtube.com/watch?v=rl8dIzTpxrI
1. Continuous Processing Mode
  - https://medium.com/@sayatsatybaldiyev/apache-spark-continuous-processing-streaming-87b92f329ea5
  - Spark Streaming에서 micro batch를 실행하기 위해 최소 10ms~100ms 정도가 소요된다고 한다
1. Introducing Low-latency Continuous Processing Mode
  - https://databricks.com/blog/2018/03/20/low-latency-continuous-processing-mode-in-structured-streaming-in-apache-spark-2-3-0.html


{% include spark-reco.md %}
