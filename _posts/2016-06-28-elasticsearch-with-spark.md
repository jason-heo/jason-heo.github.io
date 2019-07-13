---
layout: post
title: "Elasticsearch와 Spark 연동 (2019.03 갱신)"
categories: "bigdata"
---

## 2019.03 내용 추가

본 글은 약 3년 전인 2016.06에 작성했던 글인데, 지금 읽어보니 당시 spark을 잘 몰랐던 부분도 있어서 부끄럽기도 하고, 그 사이 Elasticsearch와 Spark의 버전도 많이 올라갔다.

주말에 잠시 시간이 남아서 문서를 업데이트한다.

문서 작성에 사용된 버전

- Elasticsearch 6.6.2 (2019.03 현재 최신 버전)
- Spark 2.4.0 (2019.03 현재 최신 버전)

## 들어가며

본 글에서는 Elasticsearch와 Spark를 연동하는 방법에 대해서 설명한다. Elasticsearch와 Spark는 서로의 장단점을 보완해줄 수 있는 패키지가 될 것이다.

## Sample Data 로딩하기

Kibana tutorial 문서 중 [Loading Sample Data](https://www.elastic.co/guide/en/kibana/current/tutorial-load-dataset.html)를 보면 아래와 같은 1천건의 가상의 은행 계정 정보를 제공하고 있다.

```json
{
    "account_number": 0,
    "balance": 16623,
    "firstname": "Bradshaw",
    "lastname": "Mckenzie",
    "age": 29,
    "gender": "F",
    "address": "244 Columbus Place",
    "employer": "Euron",
    "email": "bradshawmckenzie@euron.com",
    "city": "Hobucken",
    "state": "CO"
}
```

본 문서에서는 위의 자료를 예로 설명한다. 아래와 같은 단계로 Elasticsearch에 Sample Data를 입수할 수 있다. (Elasticsearch는 localhost에 미리 설치되어 있다고 가정한다)

```sh

# download 및 압축 풀기
$ wget https://download.elastic.co/demos/kibana/gettingstarted/accounts.zip
$ unzip accounts.zip

# 입수 시작 (성능에 따라 다르지만 수초 내로 완료됨)
$ curl -XPOST \
    -H 'Content-Type: application/x-ndjson' \
    'http://localhost:9200/bank/account/_bulk?pretty' \
    --data-binary "@accounts.json"

# 입수가 잘 되었는지 확인
$ curl -XGET 'http://localhost:9200/bank/account/_search?pretty&size=0'
{
  "took" : 14,
  "timed_out" : false,
  "_shards" : {
    "total" : 5,
    "successful" : 5,
    "skipped" : 0,
    "failed" : 0
  },
  "hits" : {
    "total" : 1000, <= 1천건이 입수된 것을 나타낸다
    "max_score" : 0.0,
    "hits" : [ ]
  }
}
```

{% include adsense-content.md %}

ES와 Spark 연동
============

이제 본격적으로 Spark에서 ES의 Data를 조회해보자. 사실 [Elasticsearch Spark Support](https://www.elastic.co/guide/en/elasticsearch/hadoop/current/spark.html) 문서에 필요한 내용은 모두 있지만, Scala/Spark를 처음 접했을 때는 위 문서만 읽고 ES와 Spark을 연동하는데 무척 어려웠다.


기본 code
-------

`spark-shell` 실행 시, `--packages` 옵션으로 elasticsearch-spark 의존성을 추가해야한다.

```sh
$ spark-shell \
	--master=local[2] \
	--packages="org.elasticsearch:elasticsearch-spark-20_2.11:6.6.2"
```

scala 쉘이 뜨면 아래의 scala code를 입력해보자.

```scala 
// ES 연결을 위한 parameter
val esConf = Map(
    "es.nodes" -> "localhost:9200" // es hostname 지정
)

// ES에서 data를 로딩하여 DataFrame으로 반환
val df = spark.
    read.
    format("org.elasticsearch.spark.sql").
    options(esConf).
    load("bank/account")

// schema를 출력해보자
df.printSchema

root
 |-- account_number: long (nullable = true)
 |-- address: string (nullable = true)
 |-- age: long (nullable = true)
 |-- balance: long (nullable = true)
 |-- city: string (nullable = true)
 |-- email: string (nullable = true)
 |-- employer: string (nullable = true)
 |-- firstname: string (nullable = true)
 |-- gender: string (nullable = true)
 |-- lastname: string (nullable = true)
 |-- state: string (nullable = true)

```

### 본격적으로 Query를 날려보자

사실 Spark를 잘 아는 사람이라면 위의 code만 보면 각자 필요한 질의를 ES에 수행할 수 있을 것이다. Spark를 모르는 사람을 위해 몇 가지 예를 더 보여준다.


```scala
scala> df.createOrReplaceTempView("logs")
scala> spark.sql("SELECT COUNT(*) FROM logs").show()
+----+
| _c0|
+----+
|1000|
+----+
```

정확히 1000이 출력되었다.

### Record를 출력해보기

앞서 말했듣이 여기부터는 Spark를 아는 사람에게는 너무 쉬운 이야기이다. Spark를 잘 모르는 사람을 위해서 좀 더 설명한다.

```
// 레코드 출력
scala> spark.sql("SELECT firstname, lastname, email FROM logs").show()
+---------+----------+--------------------+
|firstname|  lastname|               email|
+---------+----------+--------------------+
|    Effie|     Gates|effiegates@digita...|
|   Rowena| Wilkinson|rowenawilkinson@a...|
|  Coleman|      Berg|colemanberg@exote...|
|     Kari|   Skinner|kariskinner@singa...|
|   Marion| Schneider|marionschneider@e...|
|     Vera|    Hansen|verahansen@zanill...|
|    Lydia|     Cooke|lydiacooke@comsta...|
|     Kane|      King|kaneking@tri@trib...|
| Bradford|   Nielsen|bradfordnielsen@e...|
|    Dixie|   Fuentes|dixiefuentes@port...|
|   Edwina|Hutchinson|edwinahutchinson@...|
|      May|     Ortiz| mayortiz@syntac.com|
|     Keri|    Kinney|kerikinney@retrot...|
|  Blanche|    Holmes|blancheholmes@mot...|
|  Louella|      Chan|louellachan@confe...|
|  Antonia|    Duncan|antoniaduncan@tal...|
|     Erma|      Kane|ermakane@stockpos...|
| Schwartz|  Buchanan|schwartzbuchanan@...|
| Lorraine|Mccullough|lorrainemcculloug...|
|    Marie| Whitehead|mariewhitehead@su...|
+---------+----------+--------------------+
only showing top 20 rows
```

이 경우 `show()`의 첫 번째 인자에 원하는 개수를 입력하여 출력되는 Record 개수를 설정할 수 있다.

```scala
scala> spark.sql("SELECT firstname, lastname, email FROM logs").show(5)
+---------+---------+--------------------+
|firstname| lastname|               email|
+---------+---------+--------------------+
|    Effie|    Gates|effiegates@digita...|
|   Rowena|Wilkinson|rowenawilkinson@a...|
|  Coleman|     Berg|colemanberg@exote...|
|     Kari|  Skinner|kariskinner@singa...|
|   Marion|Schneider|marionschneider@e...|
+---------+---------+--------------------+
only showing top 5 rows
```

또한 email을 보면 알 수 있듯이 SparkSQL은 기본으로 긴 문자열의 일부만 보여준다. 이 또한 `show()`의 두 번째 인자를 이용해서 조절할 수 있다.

```scala
scala> spark.sql("SELECT firstname, lastname, email FROM logs").show(5, false)
+---------+---------+-----------------------------+
|firstname|lastname |email                        |
+---------+---------+-----------------------------+
|Effie    |Gates    |effiegates@digitalus.com     |
|Rowena   |Wilkinson|rowenawilkinson@asimiline.com|
|Coleman  |Berg     |colemanberg@exoteric.com     |
|Kari     |Skinner  |kariskinner@singavera.com    |
|Marion   |Schneider|marionschneider@evidends.com |
+---------+---------+-----------------------------+
only showing top 5 rows
```

### 정확한 Cardinality 계산

ES의 최대 장점중 하나가 [Cardinality](https://www.elastic.co/guide/en/elasticsearch/reference/current/search-aggregations-metrics-cardinality-aggregation.html) 계산이 엄청 빠르다는 점이다. 하지만 Approximation이 개입되어 오차가 발생할 수 밖에 없다. Spark를 활용하는 경우 느리지만 어쨌든 정확한 Cardinality를 계산할 수 있다.

```scala
scala> spark.sql("SELECT COUNT(DISTINCT city) FROM logs").show()
+--------------------+
|count(DISTINCT city)|
+--------------------+
|                 999|
+--------------------+
```

### ES Index 간 JOIN

ES에서 제공되지 않는 JOIN도 쉽게 사용할 수 있다. `INNER JOIN`은 기본이고, `LEFT OUTER JOIN`/`RIGHT OUTER JOIN`, MySQL 5.x도 지원하지 않는 `FULL OUTER JOIN`까지 자유자재로 JOIN할 수 있다.

```scala
val df1 = spark.read.format("org.elasticsearch.spark.sql").load("bank/account")
val df2 = spark.read.format("org.elasticsearch.spark.sql").load("member/account")

df1.createOrReplaceTempView("tab1")
df2.createOrReplaceTempView("tab2")

spark.sql("SELECT ... FROM tab1 INNER JOIN tab2 ON ...").show()
```

text file과 ES의 JOIN
--------------

이것도 크게 대단한 내용은 아니다. Spark을 이용하면 다양한 data source로부터 data를 읽어서 DataFrame화 시킬 수 있는데, DataFrame간 join을 할 수 있다.

우선 다음과 같은 `name.csv` 파일이 존재한다고 하자. 

```sh
$ cat /tmp/name.csv
firstname,lastname
Effie,Gates
Kari,Skinner
```

이제 csv file과 Elasticsearch를 읽어서 JOIN해보자

```
val esConf = Map(
    "es.nodes" -> "localhost:9200"
)

val df = spark.
    read.
    format("org.elasticsearch.spark.sql").
    options(esConf).
    load("bank/account")

df.createOrReplaceTempView("es_tab") 

val csv_df = spark.read.format("csv").
	option("header", "true").
	option("inferSchema", "true").
	load("file:///tmp/name.csv")

csv_df.createOrReplaceTempView("csv_tab")

spark.sql("""
	SELECT t1.firstname, t2.lastname
    FROM es_tab AS t1 INNER JOIN csv_tab AS t2
        ON t1.firstname = t2.firstname AND t1.lastname = t2.lastname
""").show()
+---------+--------+
|firstname|lastname|
+---------+--------+
|    Effie|   Gates|
|     Kari| Skinner|
+---------+--------+
```

### es hadoop의 동시성

Spark는 분산처리를 함으로서 성능을 향상시킨다. ES를 사용할 때의 동시성은 shard의 개수이다. 즉, shard 1개인 Index에서 SELECT를 하면 1개의 thread로 질의가 수행되지만, shard가 100개인 경우 100개의 thread로 수행이 되므로 속도가 빠르다.

### SELECT 결과를 ES에 저장하기

이것의 활용도는 무궁무진하다. 예를 들어, ES 2.3에서 드디어 [reindex](https://www.elastic.co/guide/en/elasticsearch/guide/current/reindex.html) 기능이 추가되었다. 그런데, 이게 내가 설정을 잘못한 것인지 원래 그런 것인지 모르겠으나 성능이 기대만큼 좋게 나오지 않았다. 하지만 우리는 Spark를 사용할 수 있으므로 reindex를 shard 개수의 배수만큼 빠르게 할 수 있다.

SELECT의 결과를 ES에 저장하기 위해서는 `saveToEs()` 함수를 사용한다.

```scala
// saveToEs() 호출을 위한 import
import org.elasticsearch.spark.sql._
 
val esConf = Map(
    "es.nodes" -> "localhost:9200" // es hostname 지정
)

val df = spark.
    read.
    format("org.elasticsearch.spark.sql").
    options(esConf).
    load("bank/account")

df.limit(10).saveToEs("migrated/account", esConf)

val df2 = spark.read.format("org.elasticsearch.spark.sql").load("migrated/account")

// 10개만 INSERT했기 때문에 COUNT(*)는 10이 되어야 한다
df2.count
res5: Long = 10
```

Spark를 잘 아는 사람은 눈치챘겠지만 Data Source가 뭐가 되었든 간에, Spark의 모든 DataFrame이나 RDD를 ES에 저장할 수 있다. 잠시 후 parquet를 ES에 저장하는 예도 보일 것이다

### ES에 저장시 id 지정하기

ES에 Data를 저장할 때 어떤 필드를 id로 사용할 것인지 지정할 수 있다.

```scala
val esConf = Map(
    "es.nodes" -> "localhost:9200",
    "es.mapping.id", "email"
)
```

### es-hadoop의 다양한 옵션들

이외에도 수많은 옵션이 있는데, [Elasticsearch Hadoop Configuration](https://www.elastic.co/guide/en/elasticsearch/hadoop/current/configuration.html) 문서에서 옵션들을 볼 수 있다. 지금까지 위에서 설명한 내용은 정말 기초적인 내용들이고, 이제 대용량에서도 빠른 성능과, 입수 시 exactly once 처리 등을 위해선 위의 옵션들 하나하나를 이해하고 있으면 좋다.

### ES 문서를 parquet로 저장하기

ES 문서를 parquet로 저장하여 ES Index를 Backup할 수도 있다. Backup 말고도 여러 용도로 사용할 수 있겠지... 각자의 workload마다 다르겠지만, 본인의 경우 100GB 넘는 Index를 hdfs 상의 parquet로 저장하는데 약 10분 정도 소요되었다.

```scala
val esConf = Map(
    "es.nodes" -> "localhost:9200" // es hostname 지정
)

val df = spark.
    read.
    format("org.elasticsearch.spark.sql").
    options(esConf).
    load("bank/account")

df.write.parquet("file:///tmp/parquet/")
```

이제 ls 명령으로 parquet 디렉터리를 조회해보자.

```sh
$ ls /tmp/parquet/
_SUCCESS
_common_metadata
_metadata
part-r-00000-7c9d9d04-4560-41fc-80cd-7089b15a8444.gz.parquet
part-r-00001-7c9d9d04-4560-41fc-80cd-7089b15a8444.gz.parquet
part-r-00002-7c9d9d04-4560-41fc-80cd-7089b15a8444.gz.parquet
part-r-00003-7c9d9d04-4560-41fc-80cd-7089b15a8444.gz.parquet
part-r-00004-7c9d9d04-4560-41fc-80cd-7089b15a8444.gz.parquet
part-r-00005-7c9d9d04-4560-41fc-80cd-7089b15a8444.gz.parquet
part-r-00006-7c9d9d04-4560-41fc-80cd-7089b15a8444.gz.parquet
part-r-00007-7c9d9d04-4560-41fc-80cd-7089b15a8444.gz.parquet
part-r-00008-7c9d9d04-4560-41fc-80cd-7089b15a8444.gz.parquet
part-r-00009-7c9d9d04-4560-41fc-80cd-7089b15a8444.gz.parquet
```

parquet 를 ES에 저장하기
-----------------------

이제 Tutorial의 마지막이다. 앞에서 언급했듯이 parquet를 ES에 저장할 수도 있다. parquet 만이 아니라 어떠한 Data든 Spark의 DataFrame은 ES에 저장할 수 있다.

```scala
// saveToEs() 호출을 위한 import
import org.elasticsearch.spark.sql._
 
val df = spark.read.parquet("file:///tmp/parquet/")
df.createOrReplaceTempView("parquet")

val esConf = Map(
    "es.nodes" -> "localhost:9200" // es hostname 지정
)

spark.sql("SELECT * FROM parquet").saveToEs("from_parquet/account", esConf)
```
