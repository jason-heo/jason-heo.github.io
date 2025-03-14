---
layout: post
title: "Spark AI Summit 2019 North America에 관심가는 세션들"
categories: "bigdata"
---

{% include spark-ai-summit.md %}

- https://databricks.com/session/building-robust-production-data-pipelines-with-databricks-delta-2
- https://databricks.com/session/data-driven-transformation-leveraging-big-data-at-showtime-with-apache-spark
- https://databricks.com/session/productizing-structured-streaming-jobs
    - 2019-10-10
    - Test/Deploy 등 운영 관점에서 Structured Streaming에 대한 설명
    - Spark Streaming의 Unit Test를 하기가 좀 까다로운 편인데, Test에 관련된 내용은 괜찮았던 듯
    - Delta Lake에 대한 이야기가 많이 나오는데 좀 찾아봐야겠다
        - 최근 HDFS에 있는 파일을 streaming 처리해야할 일이 있었는데, 만약 input이 Delta 였으면 streaming 처리하기 쉬었을 것 같다
- https://databricks.com/session/scaling-apache-spark-at-facebook
- https://databricks.com/session/vectorized-query-execution-in-apache-spark-at-facebook
- https://databricks.com/session/self-service-apache-spark-structured-streaming-applications-and-analytics
- https://databricks.com/session/the-rule-of-10000-spark-jobs-learning-from-exceptions-and-serializing-your-knowledge
- https://databricks.com/session/a-deep-dive-into-query-execution-engine-of-spark-sql
    - https://databricks.com/session/a-deep-dive-into-query-execution-engine-of-spark-sql-continues
- https://databricks.com/session/bridging-the-gap-between-datasets-and-dataframes
    - 2019-10-01
    - 발표 내용 좋았음
    - https://issues.apache.org/jira/browse/SPARK-14083 이게 빨리 resolve되면 좋겠다
- https://databricks.com/session/designing-structured-streaming-pipelines-how-to-architect-things-right
- https://databricks.com/session/understanding-query-plans-and-spark-uis
    - 2019-10-09
    - 보통이긴 했는데, SQL 실행 계획에 친숙하지 않는 사람은 읽어볼만 하겠다
    - 아직 정식 release되지 않은 Spark 3.0의 UI를 볼 수 있던 건 괜찮았던 듯 (뭐 직접 build해보면 되지만 요즘은 귀찮아서;;)
    - Delta Lake에 대한 이야기가 요즘 많이 언급되는데 좀 조사를 해 봐야겠다
    - parquet를 wrapping한 거라 별 특이사항이 없다고 생각했었는데, schema/metadata 관리 측면에서는 이점이 있는 듯하다
- https://databricks.com/session/apache-spark-core-deep-dive-proper-optimization
    - https://databricks.com/session/apache-spark-core-deep-dive-proper-optimization-continues
- https://databricks.com/session/how-to-extend-apache-spark-with-customized-optimizations
    - 2019-10-09
    - 이것도 재미있었다
    - Spark 2.2부터 SparkSQL을 확장할 수 있는 인터페이스가 뚫렸다 (issue: SPARK-18127)
    - 이젠 SQL syntax를 추가하거나, Optimizer를 확장하는 것이 쉬워진 듯 하다
        - 이게 없던 당시에는 기능을 추가하려면 Spark 소스 코드를 고쳐야했었다
        - 예를 들어 SAP Hana Vora (https://github.com/SAP/HANAVora-Extensions) 같이 말이지
- https://databricks.com/session/apache-spark-listeners-a-crash-course-in-fast-easy-monitoring
    - 2019-10-09
    - 읽어보면 좋을 사람들: Lister가 뭐고 왜 필요한지 모르는 분들
- https://databricks.com/session/cooperative-task-execution-for-apache-spark
- https://databricks.com/session/etl-made-easy-with-azure-data-factory-and-azure-databricks
- https://databricks.com/session/lessons-learned-using-apache-spark-for-self-service-data-prep-in-saas-world
- https://databricks.com/session/apache-spark-on-k8s-best-practice-and-performance-in-the-cloud
    - 2019-10-10
    - 아직 k8s에 대한 나의 지식이 부족해서일까, 관심이 없어서일가? 동영상 중간에 시청 종료
    - 첫 번째 발표자는 영어를 매우 잘 하네. 궁금해서 프로필 찾아봤더니 학사/석사는 중국에서 받았던데
- https://databricks.com/session/smart-join-algorithms-for-fighting-skew-at-scale
- https://databricks.com/session/apache-arrow-based-unified-data-sharing-and-transferring-format-among-cpu-and-accelerators
- https://databricks.com/session/in-memory-storage-evolution-in-apache-spark

{% include spark-reco.md %}
