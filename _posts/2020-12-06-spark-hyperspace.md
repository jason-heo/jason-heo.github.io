---
layout: post
title: "Hyperspace: An Indexing Subsystem for Apache Spark"
categories: "bigdata"
---

테스트에 사용된 Spark 버전: 2.4.4

아래 내용은 잘못된 내용을 포함하고 있을 수 있습니다.

## 들어가며

2020년 Data AI Summit eu에서 매우 흥미로운 세션이 있었는데 Index에 관련된 이야기였다.

[세션 동영상 시청하기](https://databricks.com/session_eu20/hyperspace-an-indexing-subsystem-for-apache-spark)

Microsoft에서 Spark용 Index 시스템을 만들었고 이를 이용하면 TPC-H와 TPC-DS 벤치마크에서 최대 11배, 평균 2배의 성능향상이 있다는 내용이었다.

<a href="https://imgur.com/JWAt6Ek"><img src="https://i.imgur.com/JWAt6Ekl.png" title="source: imgur.com" /></a>

[이미지 출처](https://cloudblogs.microsoft.com/opensource/2020/06/30/hyperspace-indexing-subsystem-apache-spark-now-open-source/)

발표자분 중에는 Kim씨 성을 가진 분도 있었는데 영어도 잘하시고 부럽더라.

흥미있는 주제라서 테스트를 해봤는데, 테스트용 dataset이 좋은 게 별로 없어서 많은 테스트를 못해봤다.

## 소개 및 짧게 사용해본 사용기

Hyperspace를 사용하면 shuffle 및 sort merge join을 없앨 수 있기 때문에 질의 처리 시간이 많이 줄어든다.

data source가 뭐든 상관없다. parquet, csv, json 등등에서 모두 사용할 수 있다고 한다.

개발자들은 SparkSQL도 확장을 하여 Hyperspace index를 사용할 때 최적의 execution plan이 생성되도록 하였다 (멋지다)

OLTP 성 index라기보다는 OLAP을 위한 index로 보여진다.

문서를 보면 "Lookup or range selection filter" 필드에 index를 걸라고 나오는데, scan 시간이 얼마나 단축될지 모르겠다. index가 parquet에 저장되기 때문에 원본 data가 csv나 json 같은 row-oriented format에 저장된 경우에는 확실히 이득이 있겠지만 (테스트 결과 10배 정도 빨라졌다) 원본 data가 처음부터 parquet였다면 얼마나 빨라질지는 테스트못해봤다.

"find needle in haystack" 과 같은 질의를 빠르게 처리하기 위해선 value가 저장된 file offset 같은 것이 index 구조에 저장되어 있어야하는데 이런 것이 지원되는지 모르겠다.

다음주에는 실 데이터와 훨씬 큰 데이터 규모로 테스트를 해봐야겠다.

## test용 dataset 및 환경

- test용 dataset은 아래 url에서 다운로드하였다
    - https://data.world/cityofaustin/ecmv-9xxi
- file format은 csv이다
- 데이터 건수가 많지 않아서 csv 파일을 복붙해서 약 500만건으로 뻥튀기시켰다.
- dataset은 local filesystem에 1개의 파일로 저장했다
- Mac 장비에서 테스트했으며
- Spark 버전은 2.4.4이고
- scala 버전은 2.11이다

## Hyperspace 사용하기

[Quick start guide](https://microsoft.github.io/hyperspace/docs/ug-quick-start-guide/) 문서를 보면 친절하게 잘 설명되어 있다.

`spark-shell`에서는 아래와 같이 `--packages`를 지정하면 쉽게 사용할 수 있다.

```console
$ spark-shell --master=local[1] --packages com.microsoft.hyperspace:hyperspace-core_2.11:0.3.0
```

{% include adsense-content.md %}

## test code

- csv를 Dataframe으로 변환하기 위한 코드
    ```scala
    spark.conf.set("spark.sql.shuffle.partitions", 4)

    case class Input(
      restaurant_name: String,
      zip_code: Long,
      inspection_date: String,
      score: Int,
      address: String,
      facility_id: Long,
      process_description: String
    )

    val df = spark.
        read.
        option("multiLine", "true").
        schema(org.apache.spark.sql.Encoders.product[Input].schema).
        csv("file:///path/to/datasets/hyperspace/dataset.csv")
    ```
- Hyperspace index 생성하기
    ```scala
    import com.microsoft.hyperspace._

    val hs = new Hyperspace(spark)

    import com.microsoft.hyperspace.index._
    hs.createIndex(df, IndexConfig(indexName = "index", indexedColumns = Seq("zip_code"), includedColumns = Seq("score")))

    hs.indexes.show
    +-----+--------------+---------------+----------+--------------------+--------------------+------+
    | name|indexedColumns|includedColumns|numBuckets|              schema|       indexLocation| state|
    +-----+--------------+---------------+----------+--------------------+--------------------+------+
    |index|    [zip_code]|        [score]|       200|{"type":"struct",...|file:/path/to/...|ACTIVE|
    +-----+--------------+---------------+----------+--------------------+--------------------+------+
    ```
- 실제 index data는 Delta Lake에 저장되는 것으로 보인다 (따라서 파일 포맷은 parquet이다)
    ```console
    $ ls spark-warehouse/indexes/index/v__\=0/
    _SUCCESS
    part-00001-1136a1aa-f18d-4de0-9869-87f27c6693ac_00001.c000.snappy.parquet
    part-00006-1136a1aa-f18d-4de0-9869-87f27c6693ac_00006.c000.snappy.parquet
    part-00007-1136a1aa-f18d-4de0-9869-87f27c6693ac_00007.c000.snappy.parquet
    part-00008-1136a1aa-f18d-4de0-9869-87f27c6693ac_00008.c000.snappy.parquet
    part-00009-1136a1aa-f18d-4de0-9869-87f27c6693ac_00009.c000.snappy.parquet
    ...
    ```
- Hyperspace index를 사용한 SQL의 execution plan
    - `zip_code`와 `score` 필드만 filtering하고 select하는 질의를 수행했다
    - explain 결과를 보면 알겠지만 csv file이 아닌 Parquet file을 열고 있다
        ```scala
        // 질의 수행 시 Hyperspace를 활성화한다
        spark.enableHyperspace

        df.filter("zip_code = 78704").select("score").explain
        == Physical Plan ==
        *(1) Project [score#10]
        +- *(1) Filter (isnotnull(zip_code#8L) && (zip_code#8L = 78704))
           +- *(1) FileScan parquet [zip_code#8L,score#10] Batched: true, Format: Parquet, Location: InMemoryFileIndex[file:/path/to/spark-warehouse/indexes/index/v__=0/part-00050-1136a1aa-f18d-4..., PartitionFilters: [], PushedFilters: [IsNotNull(zip_code), EqualTo(zip_code,78704)], ReadSchema: struct<zip_code:bigint,score:int>
        ```
- 동일 질의에 대해서 Hyperspace를 diable시킨 결과
    ```scala
    spark.disableHyperspace

    df.filter("zip_code = 78704").select("score").explain
    scala> df.filter("zip_code = 78704").select("score").explain
    == Physical Plan ==
    *(1) Project [score#10]
    +- *(1) Filter (isnotnull(zip_code#8L) && (zip_code#8L = 78704))
       +- *(1) FileScan csv [zip_code#8L,score#10] Batched: false, Format: CSV, Location: InMemoryFileIndex[file:/path/to/datasets/hyperspace/dataset.csv], PartitionFilters: [], PushedFilters: [IsNotNull(zip_code), EqualTo(zip_code,78704)], ReadSchema: struct<zip_code:bigint,score:int>
    ```
- `GROUP BY` test
    - 이번엔 `GROUP BY`를 수행해봤다
    - 아쉽게도 Hyperspace index를 사용하지 못한다
        ```scala
        spark.enableHyperspace

        spark.sql("""
            SELECT zip_code,
                AVG(score)
            FROM logs
            GROUP BY zip_code
            ORDER BY AVG(score)
        """).explain
        == Physical Plan ==
        *(3) Project [zip_code#8L, avg(score)#597]
        +- *(3) Sort [aggOrder#598 ASC NULLS FIRST], true, 0
           +- Exchange rangepartitioning(aggOrder#598 ASC NULLS FIRST, 4)
              +- *(2) HashAggregate(keys=[zip_code#8L], functions=[avg(cast(score#10 as bigint))])
                 +- Exchange hashpartitioning(zip_code#8L, 4)
                    +- *(1) HashAggregate(keys=[zip_code#8L], functions=[partial_avg(cast(score#10 as bigint))])
                       +- *(1) FileScan csv [zip_code#8L,score#10] Batched: false, Format: CSV, Location: InMemoryFileIndex[file:/path/to/datasets/hyperspace/dataset.csv], PartitionFilters: [], PushedFilters: [], ReadSchema: struct<zip_code:bigint,score:int>
        ```
- JOIN TEST
    - 이번엔 JOIN을 돌려봤다
    - 이번엔 Hyperspace index를 잘 사용했다
    - 즉, shuffle과 Sort Merge Join이 사라졌다
        ```scala
        spark.enableHyperspace

        spark.sql("""
            SELECT t1.zip_code, MAX(t2.score)
            FROM logs t1 INNER JOIN logs t2 ON t1.zip_code = t2.zip_code
            GROUP BY t1.zip_code
            ORDER BY MAX(t2.score)
        """).explain
        *(4) Sort [max(score)#626 ASC NULLS FIRST], true, 0
        +- Exchange rangepartitioning(max(score)#626 ASC NULLS FIRST, 4)
           +- *(3) HashAggregate(keys=[zip_code#8L], functions=[max(score#621)])
              +- Exchange hashpartitioning(zip_code#8L, 4)
                 +- *(2) HashAggregate(keys=[zip_code#8L], functions=[partial_max(score#621)])
                    +- *(2) Project [zip_code#8L, score#621]
                       +- *(2) BroadcastHashJoin [zip_code#8L], [zip_code#619L], Inner, BuildLeft
                          :- BroadcastExchange HashedRelationBroadcastMode(List(input[0, bigint, true]))
                          :  +- *(1) Project [zip_code#8L]
                          :     +- *(1) Filter isnotnull(zip_code#8L)
                          :        +- *(1) FileScan parquet [zip_code#8L] Batched: true, Format: Parquet, Location: InMemoryFileIndex[file:/path/to/spark-warehouse/indexes/index/v__=0/part-00050-1136a1aa-f18d-4..., PartitionFilters: [], PushedFilters: [IsNotNull(zip_code)], ReadSchema: struct<zip_code:bigint>, SelectedBucketsCount: 200 out of 200
                          +- *(2) Project [zip_code#619L, score#621]
                             +- *(2) Filter isnotnull(zip_code#619L)
                                +- *(2) FileScan parquet [zip_code#619L,score#621] Batched: true, Format: Parquet, Location: InMemoryFileIndex[file:/path/to/spark-warehouse/indexes/index/v__=0/part-00050-1136a1aa-f18d-4..., PartitionFilters: [], PushedFilters: [IsNotNull(zip_code)], ReadSchema: struct<zip_code:bigint,score:int>, SelectedBucketsCount: 200 out of 200
        ```

## Index 구조

앞서 말한 것처럼 Index 파일은 Parquet file이다. Delta Lake를 사용하기 때문에 index 관리에 여러 장점이 있을 것으로 보인다.

```scala
val indexDf = spark.read.parquet("/path/to/spark-warehouse/indexes/index/v__=0")

indexDf.printSchema
root
 |-- zip_code: long (nullable = true)
 |-- score: integer (nullable = true)

scala> indexDf.show
+--------+-----+
|zip_code|score|
+--------+-----+
|   78701|   79|
|   78701|   75|
|   78701|   95|
|   78701|   87|
|   78701|   82|
|   78701|   96|
|   78701|   81|
|   78701|  100|
|   78701|   85|
|   78701|   84|
|   78701|   76|
|   78701|   92|
|   78701|   83|
|   78701|   94|
|   78701|   86|
|   78701|   97|
|   78701|   86|
|   78701|   78|
|   78701|   89|
|   78701|   97|
+--------+-----+
only showing top 20 rows
```
