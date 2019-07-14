---
layout: post
title: "Spark AI Summit 2019 Europe에서 관심가는 세션"
categories: "programming"
---

Spark AI Summit은 Spark 개발자들이 많은 회사인 Databricks에서 개최하는 컨퍼런스로서 Spark만을 다루는 컨퍼런스이다. (처음엔 Spark Summit이었는데 2017년인가부터 Spark AI Summit으로 이름을 변경하였다)

1년에 두 번 (미국 샌프란시스코와 유럽) 개최된다. 한국에서 Spark AI Summit에 참여하려면 비용이 꽤나 드는데 (참가비, 항공권, 숙박비) 다행(?)인 점은 모든 세션의 영상과 슬라이드를 인터넷에 공개한다는 점이다. (본인도 2016년 유럽 컨퍼런스에 다녀왔었는데 돈이 ㅎㄷㄷ)

아래 내용은 [2018 Europe 세션](https://databricks.com/sparkaisummit/europe/schedule) 중에서 본인이 관심있는 세션을 정리한 것이다. 틈틈히 시간날 때 봐야겠다 (라고 다짐을 하지만 얼마나 볼 수 있을지 모르겠다. 아싸리 비싼 돈 내고 다녀오면 돈이 아까워서라도 챙겨보겠지만)

제목들을 훑어보니 역시 AI가 대세이고, real-time, kubernetes, analytics 등의 키워드가 눈에 띈다.

- https://databricks.com/session/cerns-next-generation-data-analysis-platform-with-apache-spark
    - 세션 후기 (2019.06.08.)
    - LHC에서 충돌 시점에는 1초에 1PB라는 어마어마한 양의 데이터가 저장된다고 한다
    - Jupyter에 [Spark Monitor](https://krishnan-r.github.io/sparkmonitor/)라는 플러그인을 사용 중. 이거 좋아보이는데, 제플린에 비슷한 것은 없으려나. 일단 단순 검색으로는 안 보인다
- https://databricks.com/session/deep-dive-into-query-execution-in-spark-sql-2-3-2
    - 세션 후기 (2019.06.09.)
    - Spark 계의 유명한 컨설턴트인 Jacek Laskowski의 세션
    - 발표 자료 링크가 없는데 검색해보니, [THE INTERNALS OF STRUCTURED QUERY EXECUTION](http://blog.jaceklaskowski.pl/spark-workshop/slides/spark-sql-internals-of-structured-query-execution.html#/home) 요거더라
    - 근데 생각보단 내용은 보통 수준
- https://databricks.com/session/accelerating-apache-spark-with-fpgas-a-case-study-for-10tb-tpcx-hs-spark-benchmark-acceleration-with-fpga
    - 세션 후기 (2019.06.15.)
    - 영어가 잘 안 들리고 발표 자료도 없어서 pass
- https://databricks.com/session/extending-structured-streaming-made-easy-with-algebra
    - 세션 후기 (2019.06.15.)
    - Spark을 확장한다길래 뭔가 Spark 내부 구조를 잘 이해해서 새로운 기능을 추가하는 것에 대한 세션으로 이해했는데,
    - Spark UDAF (User Defined Aggregation Function)에 관련된 자료 읽어보는 게 좋을 듯
- https://databricks.com/session/scaling-advanced-analytics-at-shell
    - 세션 후기 (2019.06.23.)
    - 녹음 상태가 별로다
    - 한 5분 정도 시청해봤는데 내가 관심있어할 만한 주제가 아니었다
    - 이후론 동영상 시청보단 슬라이드 위주로 봤는데, 컴퓨터 비전에 관련된 주제 아닌가 싶다. 왜 제목에 Analytics를 넣어서 사람을 헷갈리게 했지?
- https://databricks.com/session/a-framework-for-evaluating-the-performance-and-the-correctness-of-the-spark-sql-engine
    - 세션 후기 (2019.06.23.)
    - 이것도 한 6분 정도 시청하다가 중단
    - Spark의 성능 테스트에 관련된 발표이다
    - 성능 저하를 탐지한 사례들이 있어서, 끝까지 들으면 재미있을 거 같기도 하지만, 큰 관심분야가 아니라서 시청 중지
- https://databricks.com/session/apple-talk
    - 세션 후기 [2019.06.23)
    - 동영상이 없고, 슬라이드만 있다. 동영상을 좀 보고 싶은데 아쉽다.
    - Apple Siri 쪽에서 Spark을 많이 사용하고, contribution도 많이 한단다
    - DataSet에 대한 간단한 예제는 참고할만하군 (나는 그동안 너무 DataFrame만 사용했던 듯. 근데 DataSet의 성능이 좀 낮은 건 걸린다)
    - Spark 2.4에서 Parquet Nested Type의 Schema Pruning이 잘 되나보다.
- https://databricks.com/session/hudi-near-real-time-spark-pipelines-at-petabyte-scale
- https://databricks.com/session/experience-of-running-spark-on-kubernetes-on-openstack-for-high-energy-physics-workloads
- https://databricks.com/session/spark-sql-adaptive-execution-unleashes-the-power-of-cluster-in-large-scale-2
- https://databricks.com/session/bucketing-in-spark-sql-2-3
- https://databricks.com/session/spark-based-reliable-data-ingestion-in-datalake
- https://databricks.com/session/stateful-structure-streaming-and-markov-chains-join-forces-to-monitor-the-biggest-storage-of-physics-data
- https://databricks.com/session/sparklens-understanding-the-scalability-limits-of-spark-applications
- https://databricks.com/session/towards-a-unified-data-analytics-optimizer
- https://databricks.com/session/modular-apache-spark-transform-your-code-in-pieces
- https://databricks.com/session/validating-big-data-jobs-stopping-failures-before-production-on-apache-spark
- https://databricks.com/session/abris-avro-bridge-for-apache-spark
- https://databricks.com/session/deep-dive-into-stateful-stream-processing-in-structured-streaming-2
- https://databricks.com/session/spark-schema-for-free
- https://databricks.com/session/oasis-collaborative-data-analysis-platform-using-apache-spark
- https://databricks.com/session/apache-spark-on-k8s-and-hdfs-security
- https://databricks.com/session/an-introduction-to-higher-order-functions-in-spark-sql
- https://databricks.com/session/writing-and-deploying-interactive-applications-based-on-spark
- https://databricks.com/session/open-etl-for-real-time-decision-making
- https://databricks.com/session/intelligent-applications-with-apache-spark-and-kubernetes-from-prototype-to-production
- https://databricks.com/session/fpga-as-a-service-to-accelerate-your-big-data-workloads-with-fpga
- https://databricks.com/session/lessons-from-the-field-episode-ii-applying-best-practices-to-your-apache-spark-applications

### 번외

사실 작년 7월에도 Spark AI Summit North America 2018의 관심가는 세션을 정리해두었었는데, 11개월이 지난 현재까지 딱 1개 밖에 시청을 못했다;;

위에 내용이 최신이니 나느 Europe 2018 거부터 볼 거고 North America 2018은 언제볼지 모르지만, 혹 관심있는 분이 계실까봐 적어본다.

- https://databricks.com/session/using-apache-spark-to-tune-spark
- https://databricks.com/session/deep-dive-into-the-apache-spark-scheduler
- https://databricks.com/session/oversubscribing-apache-spark-resource-usage-for-fun-and
- https://databricks.com/session/apache-spark-data-source-v2
- https://databricks.com/session/deep-learning-for-recommender-systems
- https://databricks.com/session/99-problems-but-databricks-apache-spark-aint-one
- https://databricks.com/session/tunein-how-to-get-your-hadoop-spark-jobs-tuned-while-you-are-sleeping
- https://databricks.com/session/a-deep-dive-into-stateful-stream-processing-in-structured-streaming
- https://databricks.com/session/extending-spark-sql-api-with-easier-to-use-array-types-operations
- https://databricks.com/session/scalable-monitoring-using-prometheus-with-apache-spark-clusters
- https://databricks.com/session/automated-debugging-of-big-data-analytics-in-apache-spark-using-bigsift
- https://databricks.com/session/sparser-faster-parsing-of-unstructured-data-formats-in-apache-spark
- https://databricks.com/session/sos-optimizing-shuffle-i-o
- https://databricks.com/session/metrics-driven-tuning-of-apache-spark-at-scale

