---
layout: post
title: "Spark에 Custom Data source 붙이기 (Spark Data Source API)"
categories: "bigdata"
---

Apache Spark은 이제 Data 분석에 있어서 거의 표준으로 자리를 잡아가며, 수 많은 사람이 사용을 하고 있는 분산 처리 엔진이 되었다. Spark에는 Data Source API라는 것이 있어서, 외부 Data Platform과 연동을 할 수 있게 지원하고 있다.

Spark에서 Elasticsearch를 연동한다던가, Cassandra를 연동하는 것들이 모두 Data Source API를 이용하여 개발된 것이다. (물론 Parquet처럼 internal API를 이용하여 개발된 Data source 연동 방식도 있다.)

만약 아직 Spark에 연동되지 않는 BigData Platform이 있다면, 아래의 문서를 참고하여 Spark과 연동할 수 있다.

2017년 말에 이를 이용하여 D모 Platform과 연동을 했었는데, 이때 읽었던 자료들을 정리해본다.

혹 Apache Kudu를 사용해본 개발자가 있다면, Spark on Kudu 소스 코드를 참조하는 게 제일 좋은 방법 같다.

- [Spark Data Source API. Extending Our Spark SQL Query Engine](https://hackernoon.com/extending-our-spark-sql-query-engine-5f4a088de986)
- [How to create a custom Spark SQL data source](How to create a custom Spark SQL data source)
- [Apache Kudu on Spark](https://github.com/apache/kudu/tree/master/java/kudu-spark/src/main/scala/org/apache/kudu/spark/kudu)
    - Kudu API 사용 경험이 있다면 이걸 강추한다
- [Exploring the Apache Spark™ DataSource API](http://www.spark.tc/exploring-the-apache-spark-datasource-api/)
- [The Pushdown of Everything](https://www.slideshare.net/SparkSummit/the-pushdown-of-everything-by-stephan-kessler-and-santiago-mola)
- [Filtering and Projection in Spark SQL External Data sources](Filtering and Projection in Spark SQL External Data Sources)
