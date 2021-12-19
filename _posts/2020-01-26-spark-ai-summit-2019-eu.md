---
layout: post
title: "Spark AI Summit 2019 Europe에 관심가는 세션들"
categories: "bigdata"
---

{% include spark-ai-summit.md %}

과거 컨퍼런스 대비 Kubernetes에 대한 세션이 많이 줄었다. Delta Lake에 대한 세션은 여전히 많다. (내돈 내고, 광고 보는 느낌?)

- https://databricks.com/session_eu19/apache-spark-at-scale-in-the-cloud
- https://databricks.com/session_eu19/building-reliable-data-lakes-at-scale-with-delta-lake
- https://databricks.com/session_eu19/near-real-time-data-warehousing-with-apache-spark-and-delta-lake
- https://databricks.com/session_eu19/apache-sparks-built-in-file-sources-in-depth
    - (유튜브 영상 댓글에도 나와있지만) in-depth한 내용이 없다
    - 하지만 좋은 내용이 많음 (질의 처리 최적화 측면)
    - Spark을 잘 모르는 사람은 시청해볼만 하다
- https://databricks.com/session_eu19/improving-apache-spark-by-taking-advantage-of-disaggregated-architecture
- https://databricks.com/session_eu19/building-data-intensive-analytic-application-on-top-of-delta-lakes
- https://databricks.com/session_eu19/how-to-automate-performance-tuning-for-apache-spark
- https://databricks.com/session_eu19/physical-plans-in-spark-sql
    - https://databricks.com/session_eu19/physical-plans-in-spark-sql-continues
- https://databricks.com/session_eu19/streaming-analytics-for-financial-enterprises
    - 시청 중간에 pass
    - 기술적인 이야기가 별로 없는 것 같고, 발음이 잘 안 들린다
    - Apache Ignite 이야기가 나와서 잠시 찾아봤다
    - 우리는 Redis를 key-value store 정도로 사용 중이어서 Ignite의 기능은 아직 필요없지만 언젠가 필요할 때가 올지도 모르겠다
- https://databricks.com/session_eu19/using-apache-spark-to-solve-sessionization-problem-in-batch-and-streaming
    - 재미있을 줄 알았는데 귀에 잘 안들어온다
    - 아직은 나의 현업에서 세션 처리에 대한 요구 사항이 없어서일듯
- https://databricks.com/session_eu19/dynamic-partition-pruning-in-apache-spark
- https://databricks.com/session_eu19/apache-spark-side-of-funnels
- https://databricks.com/session_eu19/spark-operator-deploy-manage-and-monitor-spark-clusters-on-kubernetes
- https://databricks.com/session_eu19/power-your-delta-lake-with-streaming-transactional-changes
- https://databricks.com/session_eu19/scalable-time-series-forecasting-and-monitoring-using-apache-spark-and-elasticsearch-at-adyen
- https://databricks.com/session_eu19/spark-sql-bucketing-at-facebook
    - 괜찮았음
    - (partitioning만 써봤지) 아직 bucketing은 안 써봤는데 왜 필요한지에 대한 개념은 이해했다
    - 위에 "file sources in-depth" 세션이 in-depth하지 않다고 적었는데, 사실 위 세션에 bucketing 이야기 나올 때 bucketing의 정확한 개념은 모르고 있었음
- https://databricks.com/session_eu19/performance-troubleshooting-using-apache-spark-metrics
- https://databricks.com/session_eu19/petabytes-exabytes-and-beyond-managing-delta-lakes-for-interactive-queries-at-scale
- https://databricks.com/session_eu19/the-internals-of-stateful-stream-processing-in-spark-structured-streaming
- https://databricks.com/session_eu19/the-parquet-format-and-performance-optimization-opportunities
- https://databricks.com/session_eu19/acid-orc-iceberg-and-delta-lake-an-overview-of-table-formats-for-large-scale-storage-and-analytics
- https://databricks.com/session_eu19/data-warehousing-with-spark-streaming-at-zalando
- https://databricks.com/session_eu19/stream-stream-stream-different-streaming-methods-with-apache-spark-and-kafka
- https://databricks.com/session_eu19/apache-spark-core-practical-optimization
    - https://databricks.com/session_eu19/apache-spark-core-practical-optimization-continues
- https://databricks.com/session_eu19/data-democratization-at-nubank
- https://databricks.com/session_eu19/reliable-performance-at-scale-with-apache-spark-on-kubernetes
- https://databricks.com/session_eu19/simplify-and-scale-data-engineering-pipelines-with-delta-lake
- https://databricks.com/session_eu19/optimizing-delta-parquet-data-lakes-for-apache-spark
- https://databricks.com/session_eu19/using-production-profiles-to-guide-optimizations

{% include spark-reco.md %}
