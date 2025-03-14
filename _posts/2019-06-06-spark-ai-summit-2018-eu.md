---
layout: post
title: "Spark AI Summit 2018 Europe에서 관심가는 세션"
categories: "bigdata"
---

{% include spark-ai-summit.md %}

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
    - 세션 후기 (2019.06.23)
    - 동영상이 없고, 슬라이드만 있다. 동영상을 좀 보고 싶은데 아쉽다.
    - Apple Siri 쪽에서 Spark을 많이 사용하고, contribution도 많이 한단다
    - DataSet에 대한 간단한 예제는 참고할만하군 (나는 그동안 너무 DataFrame만 사용했던 듯. 근데 DataSet의 성능이 좀 낮은 건 걸린다)
    - Spark 2.4에서 Parquet Nested Type의 Schema Pruning이 잘 되나보다.
- https://databricks.com/session/hudi-near-real-time-spark-pipelines-at-petabyte-scale
- https://databricks.com/session/experience-of-running-spark-on-kubernetes-on-openstack-for-high-energy-physics-workloads
    - 2019.09.07
    - 보다가 중단
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
    - 세션 후기 (2019.09.07)
    - 오 이거 좋다
    - https://github.com/typelevel/frameless 는 바로 써 먹을 수 있겠다
- https://databricks.com/session/oasis-collaborative-data-analysis-platform-using-apache-spark
- https://databricks.com/session/apache-spark-on-k8s-and-hdfs-security
- https://databricks.com/session/an-introduction-to-higher-order-functions-in-spark-sql
- https://databricks.com/session/writing-and-deploying-interactive-applications-based-on-spark
- https://databricks.com/session/open-etl-for-real-time-decision-making
- https://databricks.com/session/intelligent-applications-with-apache-spark-and-kubernetes-from-prototype-to-production
- https://databricks.com/session/fpga-as-a-service-to-accelerate-your-big-data-workloads-with-fpga
- https://databricks.com/session/lessons-from-the-field-applying-best-practices-to-your-apache-spark-applications
    - https://databricks.com/session/lessons-from-the-field-episode-ii-applying-best-practices-to-your-apache-spark-applications

{% include spark-reco.md %}
