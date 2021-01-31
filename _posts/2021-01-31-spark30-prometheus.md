---
layout: post
title: "Spark 3.0 on Kubernetes에서 prometheus 연동하기"
categories: "bigdata"
---

Spark 3.0에서는 Prometheus를 native하게 지원한다.

연동하는 방법은 아래 3개의 참고 자료를 보면 쉽게 따라할 수 있다.

- [Native Support of Prometheus Monitoring in Apache Spark 3.0](https://databricks.com/session_na20/native-support-of-prometheus-monitoring-in-apache-spark-3-0), SPARK+AI SUMMIT 2020 na
- Spark 3.0 Monitoring with Prometheus
    - [Spark 3.0 Monitoring with Prometheus](https://dzlab.github.io/bigdata/2020/07/03/spark3-monitoring-1/)
    - [Spark 3.0 Monitoring with Prometheus in Kubernetes](https://dzlab.github.io/bigdata/2020/07/03/spark3-monitoring-2/)

본 블로그에서는 위의 참고 자료를 작성하여 Spark on Kubernetes 환경에서 Prometheus를 연동한 예를 정리해봤다. Kubernetes 환경이 아닌 환경에서 Prometheus를 연동하는 예는 포함하고 있지 않다.

### 1) metrics.properties 파일 준비하기

`$SPARK_CONF_DIR`에 `metrics.properties` 파일을 만들고 아래의 내용을 입력한다.

```
*.sink.prometheusServlet.class=org.apache.spark.metrics.sink.PrometheusServlet
*.sink.prometheusServlet.path=/metrics/prometheus
master.sink.prometheusServlet.path=/metrics/master/prometheus
applications.sink.prometheusServlet.path=/metrics/applications/prometheus
```

참고로 `metrics.properties` 내용은 spark source code의 [`metrics.properties.template`](https://github.com/apache/spark/blob/master/conf/metrics.properties.template)를 참고하였다.

### 2) job submit 시 옵션 지정하기

`spark-submit` 시에 아래와 같은 옵션을 설정해야한다.

```console
$ spark-submit \
    --conf spark.ui.prometheus.enabled=true \
    --conf spark.kubernetes.driver.annotation.prometheus.io/scrape=true \
    --conf spark.kubernetes.driver.annotation.prometheus.io/path=/metrics/executors/prometheus \
    --conf spark.kubernetes.driver.annotation.prometheus.io/port=4040 \
	...
```

`spark.ui.prometheus.enabled`는 Prometheus metric을 활성화하겠다는 설정이다. Spark 3.0이 Prometheus를 지원하긴 하지만 이 옵션을 지정해야만 활성화된다. 다른 metric system과 충돌이 발생할 수 있어서 default로는 disable된 것 같다.

`spark.kubernetes.driver.annotation.*` 설정은 Kubernetes 환경에서 자동 discover를 위한 일종의 규약이다. Kubernetes가 아닌 환경에서는 불필요한 설정이다.

### 3) Web UI에 접속하여 metric 확인하기

`http://<spark-driver>/metrics/executors/prometheus`에 접속해보면 다음과 같은 내용이 출력되는 것을 볼 수 있다.

이 내용을 참고하면 어떤 metric이 제공되는지, 어떤 tag가 존재하는지 확인할 수 있으므로 Chart를 그릴 때 도움이 된다.

```
spark_info{version="3.0.1", revision="2b147c4cd50da32fe2b4167f97c8142102a0510d"} 1.0
metrics_executor_rddBlocks{application_id="spark-10180bcec2b14c2ab445e8b0f132a0dd", application_name="App Name", executor_id="driver"} 0
metrics_executor_memoryUsed_bytes{application_id="spark-10180bcec2b14c2ab445e8b0f132a0dd", application_name="App Name", executor_id="driver"} 10681
metrics_executor_diskUsed_bytes{application_id="spark-10180bcec2b14c2ab445e8b0f132a0dd", application_name="App Name", executor_id="driver"} 0
metrics_executor_totalCores{application_id="spark-10180bcec2b14c2ab445e8b0f132a0dd", application_name="App Name", executor_id="driver"} 0
metrics_executor_maxTasks{application_id="spark-10180bcec2b14c2ab445e8b0f132a0dd", application_name="App Name", executor_id="driver"} 0
metrics_executor_activeTasks{application_id="spark-10180bcec2b14c2ab445e8b0f132a0dd", application_name="App Name", executor_id="driver"} 0
metrics_executor_failedTasks_total{application_id="spark-10180bcec2b14c2ab445e8b0f132a0dd", application_name="App Name", executor_id="driver"} 0
metrics_executor_completedTasks_total{application_id="spark-10180bcec2b14c2ab445e8b0f132a0dd", application_name="App Name", executor_id="driver"} 0
...
```

### 3) Prometheus UI에 접속하여 chart 그리기

이제 Prometheus에 접속해서 metric을 입력하면 아래처럼 chart를 볼 수 있다.

<img src="https://i.imgur.com/rMufzor.png" />

(출처: Spark Summit 발표 자료)

{% include adsense-content.md %}

### 4) streaming 모니터링

아래의 설정을 추가하면 streaming job도 모니터링할 수 있다.

```
spark.sql.streaming.metricsEnabled=true
```

(설정 이름만 봐서는 Structured Streaming만 모니터링 할 수 있는 듯 한데 확인해보진 못했다.)

위 설정을 추가하면 다음과 같은 metric을 출력한다.

- latency
- inputRate-total
- processingRate-total
- states-rowsTotal
- states-usedBytes
- eventTime-watermark

테스트 결과 Structured Streaming에서도 metric이 잘 출력되지 않는다. 구글링을 해보면 [Spark 3.0 streaming metrics in Prometheus](https://stackoverflow.com/q/64436497/2930152) 질문이 검색되는데 이 사람도 나랑 같은 문제가 있다.

`/metrics/json`으로 접근하면 streaming metric들도 잘 나오는데 `/metrics/executors/prometheus`에는 나오지 않고 있다.

### 5) 작동 과정

Prometheus는 기본적으로 Pull 모델이다. Prometheus Server가 metric들을 땡겨(pull)오겠다는 의미이다. 반대로 Push 모델이 있는데 이때는 어플리케이션이 Prometheus 서버에 metric을 전송하게 된다. 처음 접할 때는 '어라? 왜 Pull 모델이지?'라고 할 수 있는데 Spark Summit 발표 자료를 보면 push 중이 app 자체가 죽은 경우 metric 전송이 중단되기 때문이라고 한다.

그렇다면 Prometheus는 pull할 URL을 어떻게 알 수 있을까? Kubernetes 환경에서 Prometheus에는 discover라는 기능이 있다. 즉, pod에 사전에 규약된 annotation이 존재하는 경우 prometheus가 metric의 endpoint를 discover할 수 있다.

Spark job 제출 시에 아래와 같은 conf를 지정한 것을 볼 수 있다.

```
    --conf spark.kubernetes.driver.annotation.prometheus.io/scrape=true \
    --conf spark.kubernetes.driver.annotation.prometheus.io/path=/metrics/executors/prometheus \
    --conf spark.kubernetes.driver.annotation.prometheus.io/port=4040 \
```

이 경우 driver pod을 describe하면 다음과 같은 annotation이 추가된 것을 볼 수 있는데 이것이 Prometheus가 discover하기 위한 규약이다.

```
Annotations: prometheus.io/path: /metrics/executors/prometheus
             prometheus.io/port: 4040
             prometheus.io/scrape: true
```
