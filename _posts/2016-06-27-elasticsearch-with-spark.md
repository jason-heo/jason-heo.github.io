---
layout: post
title: "Elasticsearch와 Spark 연동"
categories: elasticsearch
---

들어가며
====

본 글에서는 Elasticsearch와 Spark를 연동하는 방법에 대해서 설명한다. Elasticsearch와 Spark는 서로의 장단점을 보완해줄 수 있는 패키지가 될 것이다.

Sample Data Loading
===================

적당한 Sample Data를 찾아봤는데, [Elasticsearch 문서](https://www.elastic.co/guide/en/elasticsearch/reference/current/_exploring_your_data.html)에서 Sample Data를 download 할 수 있고 Loading하는 방법도 잘 설명되어 있다.

다음과 같은 은행 계좌 정보 1천건을 upload할 수 있다.

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

본 문서에서는 위의 자료를 예로 설명한다. 간략히 설명하면, [accounts.zip](https://github.com/bly2k/files/blob/master/accounts.zip?raw=true)을 download 한 뒤 압축을 풀고, 아래의 명령으로 Bulk Loading을 하면 된다.

    curl -XPOST 'localhost:9200/bank/account/_bulk?pretty' --data-binary "@accounts.json"

Elasticsearch Hadoop jar download
=================================

이제는 Elasticsearch Hadoop (이하 es hadoop) 용 jar를 download하자. 실제 build 시에는 각자 사용하는 package manager에 따라 방법이 다를 것이고 여기서는 `spark-shell`을 이용할 것이므로 manual하게 jar를 download하기로 했다.

우선 [es hadoop download page](https://www.elastic.co/downloads/hadoop)를 방문하여 각자 ES 버전에 맞는 파일을 다운로드 한다. 본인의 경우 ES 2.3.2를 사용 중이므로 [이 파일](http://download.elastic.co/hadoop/elasticsearch-hadoop-2.3.2.zip)을 download하였다. 모든 버전을 테스트해본 것은 아니지만, ES 2.x대면 es hadoop 2.3.2를 받아도 큰 문제는 없는 것 같다.

별도의 설치 과정은 없다. 적당한 경로에 압축을 풀고 ls를 해 보면 다음과 같은 파일이 보인다.

    $ ls jars/elasticsearch-hadoop-2.3.2/dist/elasticsearch-*.jar | grep spark
    jars/elasticsearch-hadoop-2.3.2/dist/elasticsearch-spark-1.2_2.10-2.3.2-javadoc.jar
    jars/elasticsearch-hadoop-2.3.2/dist/elasticsearch-spark-1.2_2.10-2.3.2-sources.jar
    jars/elasticsearch-hadoop-2.3.2/dist/elasticsearch-spark-1.2_2.10-2.3.2.jar
    jars/elasticsearch-hadoop-2.3.2/dist/elasticsearch-spark-1.2_2.11-2.3.2-javadoc.jar
    jars/elasticsearch-hadoop-2.3.2/dist/elasticsearch-spark-1.2_2.11-2.3.2-sources.jar
    jars/elasticsearch-hadoop-2.3.2/dist/elasticsearch-spark-1.2_2.11-2.3.2.jar
    jars/elasticsearch-hadoop-2.3.2/dist/elasticsearch-spark_2.10-2.3.2-javadoc.jar
    jars/elasticsearch-hadoop-2.3.2/dist/elasticsearch-spark_2.10-2.3.2-sources.jar
    jars/elasticsearch-hadoop-2.3.2/dist/elasticsearch-spark_2.10-2.3.2.jar
    jars/elasticsearch-hadoop-2.3.2/dist/elasticsearch-spark_2.11-2.3.2-javadoc.jar
    jars/elasticsearch-hadoop-2.3.2/dist/elasticsearch-spark_2.11-2.3.2-sources.jar
    jars/elasticsearch-hadoop-2.3.2/dist/elasticsearch-spark_2.11-2.3.2.jar

파일명의 의미는 다음과 같다.

    elasticsearch-spark_2.11-2.3.2.jar

- `2.11`: Scala 버전을 의미한다
- `2.3.2`: Elasticsearch 버전을 의미한다.

Scala 버전에 맞지 않는 경우 제대로 작동을 하지 않는다. 자신이 사용하는 Scala 버전은 `spark-shell` 명령을 실행하면 알 수 있다.

    $ spark-shell
    Welcome to
          ____              __
         / __/__  ___ _____/ /__
        _\ \/ _ \/ _ `/ __/  '_/
       /___/ .__/\_,_/_/ /_/\_\   version 1.6.1
          /_/

    Using Scala version 2.10.5 (Java HotSpot(TM) 64-Bit Server VM, Java 1.8.0_65)
                        ^^^^^^
                        여기에 있음

ES와 Spark 연동
============

이제 본격적으로 Spark에서 ES의 Data를 조회해보자. 사실 [Elasticsearch Spark Support](https://www.elastic.co/guide/en/elasticsearch/hadoop/current/spark.html) 문서에 필요한 내용은 모두 있지만, Scala/Spark를 처음 접했을 때는 위 문서만 읽고 ES와 Spark을 연동하는데 무척 어려웠다.


기본 code
-------

    $ spark-shell  --jars jars/elasticsearch-hadoop-2.3.2/dist/elasticsearch-spark_2.10-2.3.2.jar
     
    import org.apache.spark.sql.SQLContext
    import org.elasticsearch.spark.sql._
    import org.apache.spark.SparkConf
     
    sc.stop() // 기존 SparkContext 종료
     
    val conf = new SparkConf().setAppName("jsheo-test")
     
    conf.set("spark.driver.allowMultipleContexts", "true")
    conf.set("es.index.auto.create", "true")
    conf.set("es.nodes.discovery", "true")
    conf.set("es.nodes", "127.0.0.1:9200") // 각자 사용하는 ES 주소를 적는다.
     
    // SparkContext랑 SQLConext 새로 만들기
    val sc = new org.apache.spark.SparkContext(conf);
     
    val sqlContext = new SQLContext(sc)

    // 앞선 Sample Data에서 bank/account에 자료를 입력했다.
    // bank는 ES Index를 의미하고
    // account는 Type을 의미한다.
    val df = sqlContext.read.format("org.elasticsearch.spark.sql").load("bank/account")

본격적으로 Query를 날려보자
-------------

사실 Spark를 잘 아는 사람이라면 위의 code만 보면 각자 필요한 질의를 ES에 수행할 수 있을 것이다. Spark를 모르는 사람을 위해 몇 가지 예를 더 보여준다.


    val df = sqlContext.read.format("org.elasticsearch.spark.sql").load("bank/account")
    df.registerTempTable("tab")
    sqlContext.sql("SELECT COUNT(*) FROM tab").show()
    +----+
    | _c0|
    +----+
    |1000|
    +----+

정확시 1000이 출력되었다.

더 많은 Record를 출력하기
-----------------

앞서 말했듣이 여기부터는 Spark를 아는 사람에게는 너무 쉬운 이야기이다. 하지만 본인처럼 Spark를 잘 모르는 사람을 위해서 좀 더 설명한다. `SELECT` 결과를 `show()`로 출력하면 아래와 같이 20건 밖에 출력되지 않는다.

    scala> sqlContext.sql("SELECT firstname, lastname, email FROM tab").show()
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

이 경우 `show()`의 첫 번째 인자에 원하는 개수를 입력하여 출력되는 Record 개수를 설정할 수 있다.

    scala> sqlContext.sql("SELECT firstname, lastname, email FROM tab").show(5)
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

또한 email을 보면 알 수 있듯이 SparkSQL은 기본으로 긴 문자열의 일부만 보여준다. 이 또한 `show()`의 두 번째 인자를 이용해서 조절할 수 있다.

    scala> sqlContext.sql("SELECT firstname, lastname, email FROM tab").show(5, false)
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

정확한 Cardinality 계산
------------------

ES의 최대 장점중 하나가 [Cardinality](https://www.elastic.co/guide/en/elasticsearch/reference/current/search-aggregations-metrics-cardinality-aggregation.html) 계산이 엄청 빠르다는 점이다. 하지만 Approximation이 개입되어 오차가 발생할 수 밖에 없다. Spark를 활용하는 경우 느리지만 어쨌든 정확한 Cardinality를 계산할 수 있다.

방법으로는 다음과 같이 i) `COUNT(DISTINCT col_name)`과 ii) `GROUP BY`와 sub-query를 사용하는 방법이 있는데, 테스트 결과 후자가 훨씬 빨랐다.

    sqlContext.sql("SELECT COUNT(DISTINCT city) FROM tab").show()
    sqlContext.sql("SELECT COUNT(*) FROM (SELECT city FROM tab GROUP BY city) t").show()

ES Index 간 JOIN
----

ES에서 제공되지 않는 JOIN도 쉽게 사용할 수 있다. `INNER JOIN`은 기본이고, `LEFT OUTER JOIN`/`RIGHT OUTER JOIN`, MySQL 5.x도 지원하지 않는 `FULL OUTER JOIN`까지 자유자재로 JOIN할 수 있다.


    val df1 = sqlContext.read.format("org.elasticsearch.spark.sql").load("bank/account")
    val df2 = sqlContext.read.format("org.elasticsearch.spark.sql").load("member/account")

    df1.registerTempTable("tab1")
    df2.registerTempTable("tab2")

    sqlContext.sql("SELECT ... FROM tab1 INNER JOIN tab2 ON ...").show()

cvs와 ES의 JOIN
--------------

기획자가 csv 파일을 주면서 '이 안에 있는 사람들의 데이터 좀 검색해 주세요'라고 하는 경우 어떻게 할까? 그냥 csv와 ES를 JOIN하면 된다.

우선 다음과 같은 `name.csv` 파일이 존재한다고 하자. 

    $ cat name.csv
    firstname,lastname
    Effie,Gates
    Kari,Skinner

spark-shell을 실행할 때 csv를 위한 package를 지정해야 한다.


    $ spark-shell  --jars jars/elasticsearch-hadoop-2.3.2/dist/elasticsearch-spark_2.10-2.3.2.jar --packages com.databricks:spark-csv_2.10:1.4.0
    import org.apache.spark.sql.SQLContext
    import org.elasticsearch.spark.sql._
    import org.apache.spark.SparkConf
     
    sc.stop()
     
    val conf = new SparkConf().setAppName("jsheo-test")
     
    conf.set("spark.driver.allowMultipleContexts", "true")
    conf.set("es.index.auto.create", "true")
    conf.set("es.nodes.discovery", "true")
    conf.set("es.nodes", "127.0.0.1:9200")
     
    val sc = new org.apache.spark.SparkContext(conf);
     
    val sqlContext = new SQLContext(sc)

    val df = sqlContext.read.format("org.elasticsearch.spark.sql").load("bank/account")
    df.registerTempTable("es_tab") 

    val csv_df = sqlContext.read.format("com.databricks.spark.csv").option("header", "true").option("inferSchema", "true").load("file:///path/to/name.csv")
    csv_df.registerTempTable("csv_tab")

    sqlContext.sql("SELECT t1.firstname, t2.lastname FROM es_tab AS t1 INNER JOIN csv_tab AS t2 ON t1.firstname = t2.firstname AND t1.lastname = t2.lastname").show()
    +---------+--------+
    |firstname|lastname|
    +---------+--------+
    |    Effie|   Gates|
    |     Kari| Skinner|
    +---------+--------+

csv는 local file system에 존재해도 되고, hdfs에 존재해도 된다. Amazon s3에 존재해도 되는 것 같은데, 이건 써보질 않아서...

es hadoop의 동시성
--------------

Spark는 분산처리를 함으로서 성능을 향상시킨다. ES를 사용할 때의 동시성은 shard의 개수이다. 즉, shard 1개인 Index에서 SELECT를 하면 1개의 thread로 질의가 수행되지만, shard가 100개인 경우 100개의 thread로 수행이 되므로 속도가 빠르다.


SELECT 결과를 ES에 저장하기
-------------------

이것의 활용도는 무궁무진하다. 예를 들어, ES 2.3에서 드디어 [reindex](https://www.elastic.co/guide/en/elasticsearch/guide/current/reindex.html) 기능이 추가되었다. 그런데, 이게 내가 설정을 잘못한 것인지 원래 그런 것인지 모르겠으나 성능이 기대만큼 좋게 나오지 않았다. 하지만 우리는 Spark를 사용할 수 있으므로 reindex를 shard 개수의 배수만큼 빠르게 할 수 있다.

SELECT의 결과를 ES에 저장하기 위해서는 `saveToEs()` 함수를 사용한다.

    $ spark-shell  --jars jars/elasticsearch-hadoop-2.3.2/dist/elasticsearch-spark_2.10-2.3.2.jar
     
    import org.apache.spark.sql.SQLContext
    import org.elasticsearch.spark.sql._
    import org.apache.spark.SparkConf
     
    sc.stop()
     
    val conf = new SparkConf().setAppName("jsheo-test")
     
    conf.set("spark.driver.allowMultipleContexts", "true")
    conf.set("es.index.auto.create", "true")
    conf.set("es.nodes.discovery", "true")
    conf.set("es.nodes", "127.0.0.1:9200")
     
    val sc = new org.apache.spark.SparkContext(conf);
     
    val sqlContext = new SQLContext(sc)

    val df = sqlContext.read.format("org.elasticsearch.spark.sql").load("bank/account")
    df.registerTempTable("tab")

    sqlContext.sql("SELECT * FROM tab LIMIT 10").saveToEs("migration/account")

    val df2 = sqlContext.read.format("org.elasticsearch.spark.sql").load("migration/account")
    df.registerTempTable("tab2")
    
    // 1,000개만 SELECT했기 때문에 COUNT(*)는 1,000이 된다.
    sqlContext.sql("SELECT COUNT(*) FROM tab2").show()
    +----+
    | _c0|
    +----+
    |1000|
    +----+

Spark를 잘 아는 사람은 눈치챘겠지만 Data Source가 뭐가 되었든 간에, Spark의 모든 DataFrame이나 RDD를 ES에 저장할 수 있다. 잠시 후 parquet를 ES에 저장하는 예도 보일 것이다

ES에 저장시 id 지정하기
---------------

ES에 Data를 저장할 때 어떤 필드를 id로 사용할 것인지 지정할 수 있다.

    $ spark-shell  --jars jars/elasticsearch-hadoop-2.3.2/dist/elasticsearch-spark_2.10-2.3.2.jar
     
    ... 
    conf.set("spark.driver.allowMultipleContexts", "true")
    conf.set("es.index.auto.create", "true")
    conf.set("es.nodes.discovery", "true")
    conf.set("es.nodes", "127.0.0.1:9200")

    conf.set("es.mapping.id", "email") // 이렇게 설정하면, email field가 ES 문서의 id가 된다.
    ...

이외에도 수많은 옵션이 있는데, [Elasticsearch Hadoop Configuration](https://www.elastic.co/guide/en/elasticsearch/hadoop/current/configuration.html) 문서에서 옵션들을 볼 수 있다. 왠만한 옵션은 다 있는 듯... 옵션만 잘 설정하면 성능도 잘 나오고 좋다. Storm이나 SparkStream과 같이 사용할 때, mapping등의 오류가 있는 경우 무한 loop에 빠진다거나 할 수 있는데, 깔끔하게 처리하는 방법은 없는 것 같다. 정제를 잘 해서 오류있는 document는 잘 제외처리해주는 게 제일 좋은 듯...

ES 문서를 parquet로 저장하기
--------------------

ES 문서를 parquet로 저장하여 ES Index를 Backup할 수도 있다. Backup 말고도 여러 용도로 사용할 수 있겠지... 각자의 workload마다 다르겠지만, 본인의 경우 100GB 넘는 Index를 hdfs 상의 parquet로 저장하는데 약 10분 정도 소요되었다.


    $ spark-shell  --jars jars/elasticsearch-hadoop-2.3.2/dist/elasticsearch-spark_2.10-2.3.2.jar
     
    import org.apache.spark.sql.SQLContext
    import org.elasticsearch.spark.sql._
    import org.apache.spark.SparkConf
     
    sc.stop()
     
    val conf = new SparkConf().setAppName("jsheo-test")
     
    conf.set("spark.driver.allowMultipleContexts", "true")
    conf.set("es.index.auto.create", "true")
    conf.set("es.nodes.discovery", "true")
    conf.set("es.nodes", "127.0.0.1:9200")
     
    val sc = new org.apache.spark.SparkContext(conf);
     
    val sqlContext = new SQLContext(sc)

    val df = sqlContext.read.format("org.elasticsearch.spark.sql").load("bank/account")
    df.registerTempTable("tab")

    sqlContext.sql("SELECT * FROM tab").write.parquet("file:///path/to/parquet/")

이제 ls 명령으로 parquet 디렉터리를 조회해보자.

    $ ls /path/to/parquet/
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

parquet file 10개가 생성되었는데, bank Index의 shard를 10개로 만들었기 때문이다.

parquet 를 ES에 저장하기
-----------------------

이제 Tutorial의 마지막이다. 앞에서 언급했듯이 parquet를 ES에 저장할 수도 있다. parquet 만이 아니라 어떠한 Data든 Spark의 DataFrame은 ES에 저장할 수 있다.


    $ spark-shell  --jars jars/elasticsearch-hadoop-2.3.2/dist/elasticsearch-spark_2.10-2.3.2.jar
     
    import org.apache.spark.sql.SQLContext
    import org.elasticsearch.spark.sql._
    import org.apache.spark.SparkConf
     
    sc.stop()
     
    val conf = new SparkConf().setAppName("jsheo-test")
     
    conf.set("spark.driver.allowMultipleContexts", "true")
    conf.set("es.index.auto.create", "true")
    conf.set("es.nodes.discovery", "true")
    conf.set("es.nodes", "127.0.0.1:9200")
     
    val sc = new org.apache.spark.SparkContext(conf);
     
    val sqlContext = new SQLContext(sc)

    val df = sqlContext.read.parquet("file:///path/to/parquet/")
    df.registerTempTable("tab")

    sqlContext.sql("SELECT * FROM tab").saveToEs("from_parquet/account")

