---
layout: post
title: "Performance Troubleshooting Using Apache Spark Metrics (SparkMeasure)"
categories: "bigdata"
---

SPARK+AI SUMMIT 2019 Europe에서 [Performance Troubleshooting Using Apache Spark Metrics](https://databricks.com/session_eu19/performance-troubleshooting-using-apache-spark-metrics)라는 세션이 있었다.

발표자는 [Luca Canali](http://canali.web.cern.ch/)라는 CERN 소속 Data Engineer인데, 나는 Luca의 발표를 2016년 벨기에 브뤼셀에서 열린 Spark Summit에서 직접 들었던 기억이 있다. (당시의 발표는 [Apache Spark 2.0 Performance Improvements Investigated With Flame Graphs](https://databricks.com/session/apache-spark-2-0-performance-improvements-investigated-with-flame-graphs)이다)

Luca는 Spark 모니터링에 관련된 Library를 많이 만들고 있고 컨퍼런스에서도 지속적으로 발표하고 있다.

암튼 원래 세션으로 다시 돌아와서 본 세션에서는 주로 [SparkMeasure](https://github.com/LucaCanali/sparkMeasure)라는 Library를 이용하여 Spark을 모니터링하는 것에 대해 설명한다.

### SparkMeasure 사용법

`--packages` 옵션으로 의존성을 쉽게 추가할 수 있다. 다음은 Spark 3.0에서 `spark-shell`을 수행할 때의 옵션이다.

```console
$ spark-shell --packages ch.cern.sparkmeasure:spark-measure_2.12:0.17
```

`2.12` 부분이 Scala의 버전인데 Spark 버전에 따라 Scala 버전이 다르다.

우선 간단한 Dataset을 하나 만들어보자.

```scala
case class Person(name: String)

val ds = Seq(Person("Kim")).toDS

ds.show
+----+
|name|
+----+
| Kim|
+----+

ds.count
res9: Long = 1
```

그럼 이번에는 SparkMeasure를 이용하여 Dataset 질의 과정에서 각종 Metric들을 확인해보자.

```scala
val stageMetrics = ch.cern.sparkmeasure.StageMetrics(spark)

stageMetrics.runAndMeasure(ds.count)
```

SparkMeasure의 출력 결과는 다음과 같다.

```
Time taken: 43 ms

Scheduling mode = FIFO
Spark Context default degree of parallelism = 4
Aggregated Spark stage metrics:
numStages => 2
numTasks => 2
elapsedTime => 18 (18 ms)
stageDuration => 17 (17 ms)
executorRunTime => 5 (5 ms)
executorCpuTime => 3 (3 ms)
executorDeserializeTime => 2 (2 ms)
executorDeserializeCpuTime => 2 (2 ms)
resultSerializationTime => 0 (0 ms)
jvmGCTime => 0 (0 ms)
shuffleFetchWaitTime => 0 (0 ms)
shuffleWriteTime => 2 (2 ms)
resultSize => 2648 (2.0 KB)
diskBytesSpilled => 0 (0 Bytes)
memoryBytesSpilled => 0 (0 Bytes)
peakExecutionMemory => 0
recordsRead => 0
bytesRead => 0 (0 Bytes)
recordsWritten => 0
bytesWritten => 0 (0 Bytes)
shuffleRecordsRead => 1
shuffleTotalBlocksFetched => 1
shuffleLocalBlocksFetched => 1
shuffleRemoteBlocksFetched => 0
shuffleTotalBytesRead => 59 (59 Bytes)
shuffleLocalBytesRead => 59 (59 Bytes)
shuffleRemoteBytesRead => 0 (0 Bytes)
shuffleRemoteBytesReadToDisk => 0 (0 Bytes)
shuffleBytesWritten => 59 (59 Bytes)
shuffleRecordsWritten => 1
res10: Long = 1
```

위 결과에서 볼 수 있듯이 Spark 수행 과정에서 발생한 Metric들을 알 수 있다.

### Architecture

<img src="https://i.imgur.com/MDtlbRY.png" />

([출처](https://github.com/LucaCanali/sparkMeasure#architecture-diagram))

### SparkMeasure 활용

위 결과만 보면 별 것 아닌 것 같다. 그런데 이것을 Grafana로 연결해서 확인하면 이야기가 달라진다.

위의 Architecture를 보면 오른쪽 하단에 "Metrics Output" 라는 파란색 박스가 있고 그 안에 Prometheus 아이콘이 보인다.

Prometheus를 연동하는 정확한 방법을 찾지는 못했지만, [이 글](https://github.com/LucaCanali/sparkMeasure/blob/master/docs/Flight_recorder_DBwrite.md)을 보면 SparkMeasure는 sink 기능을 지원하는데 이를 이용하여 InfluxDB에 Metric들을 저장할 수 있다고 한다.

그리고 InfluxDB에 저장된 데이터를 Grafana에 연동하면 아래와 같은 Dashboard를 만들 수 있다.

<img src="https://i.imgur.com/BE9RDsk.png" />

([출처](https://www2.slideshare.net/databricks/performance-troubleshooting-using-apache-spark-metrics))

### 마무리

SparkMeasure를 최종적으로 Grafana까지 연동해보고 싶었지만 더 진행하지는 않았다. 왜냐하면 Spark 3.0에서는 Prometheus를 지원하기 때문이다. 이에 대해서는 다음 번 블로그 글에 올릴 예정이다.

{% include spark-reco.md %}
