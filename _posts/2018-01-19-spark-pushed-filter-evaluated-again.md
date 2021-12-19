---
layout: post
title: "Storage로 push된 filter는 Spark Engine에서 한번 더 evaluation된다"
categories: "bigdata"
---

Spark Data Source API의 `PrunedFilterScan`을 상속받아 구현하는 경우 Spark SQL에서 `WHERE` 절에 입력한 조건을 전달받을 수 있기 때문에, Data Source에서 Spark으로 전달하는 Data량을 줄일 수 있다. 이를 통해 질의 처리 속도도 빨라진다.

하지만 한 가지 아쉬운 점이 있는데, 이렇게 Filter 조건을 만족하는 Record들만 Spark으로 전달된다 하더라도, Spark에서 다시 한번 Filter 조건에 맞는지 검사를 한다는 점이다. 이 때문에 질의처리 시간에 손해를 볼 수 밖에 없다.

다음과 같은 SQL을 실행하다고 생각하자.

```
SELECT a, b
FROM logs
WHERE c = 'cc'
```

이때의 EXPLAIN 결과를 보면 다음과 같다.

```
Scan ParquetRelation[a#4,b#3,c#10], PushedFilters: [EqualTo(c,cc)]
```

`EqualTo(c,cc)`를 보면 알 수 있듯이 Filter는 잘 Push되었다. 그런데, Scan 부분을 보면 `a, b, c`처럼 `SELECT`에 없는 `c`가 포함된 것을 볼 수 있다. 즉, 사용자 의도와 상관없이 Data Source에서 읽어서 Spark에 전달된다는 것을 볼 수 있다. Spark 내부적으로 다시 한번 정말로 `c` 필드의 값이 `cc`였는지 검사를 하고, SQL의 output에서는 `c`를 제거하고 `a, b`만 출력하게 된다.

정말로 그러한지 테스트를 해보자. 아래 코드에서 Relation을 만들고, DataFrame을 생성하는 것은 [이곳의 code](https://gist.github.com/marmbrus/f3d121a1bc5b6d6b57b9)를 참고했다.

설명 및 SQL 수행 결과를 주석으로 적어두었으니, 자세한 설명은 생략한다

```
import org.apache.spark.sql.{Row, SparkSession}
import org.apache.spark.sql.sources.{BaseRelation, Filter, PrunedFilteredScan}
import org.apache.spark.sql.types.{StructType}

case class MyRelation(start: Int,
                      end: Int)(
                      @transient val spark: SparkSession)
  extends BaseRelation with PrunedFilteredScan {

  import spark.implicits._

  def sqlContext: org.apache.spark.sql.SQLContext = null
  def schema = StructType('age.int :: Nil)

  /**
    * filter를 전달받지만, 사용하진 않는다
    * 무조건 start ~ to 까지 범위의 age를 갖는 record를 생성한다
    */
  def buildScan(requiredColumns: Array[String], filters: Array[Filter]) = {
    spark.sparkContext.parallelize(start to end).map(Row(_))
  }
}

object DsAPITest {

  def test(spark: SparkSession): Unit = {

    /**
      * 100개의 Record를 생성한다
      * age의 값은 1부터 100까지이다
      */
    spark.baseRelationToDataFrame(MyRelation(1, 100)(spark)).createOrReplaceTempView("tab")

    spark.sql("SELECT age FROM tab").show(false)

    /**
      * SQL 수행 결과 (어떤 값이 return되는지 확인)
        +---+
        |age|
        +---+
        |1  |
        |2  |
        |3  |
        |4  |
        |5  |
        |6  |
        |7  |
        |8  |
         ...
        |100|
        +---+
      */

    spark.sql("EXPLAIN SELECT COUNT(1) FROM tab WHERE age = 10").show(false)

    /**
      * EXPLAIN 결과 - COUNT(1)만 했을 뿐인데, age field를 Scan 요청하고 있다.
      +--------------------------------------------------------------------------------------------------------------------
      |plan
      +--------------------------------------------------------------------------------------------------------------------
      |== Physical Plan ==
            *HashAggregate(keys=[], functions=[count(1)])
      +- Exchange SinglePartition
         +- *HashAggregate(keys=[], functions=[partial_count(1)])
            +- *Project
               +- *Filter (isnotnull(age#0) && (age#0 = 10))
                  +- *Scan MyRelation(1,100) [age#0] PushedFilters: [IsNotNull(age), EqualTo(age,10)], ReadSchema: struct<>|
      +---------------------------------------------------------------------------------------------------------------------
      */


    spark.sql("SELECT COUNT(1) FROM tab WHERE age = 10").show(false)

    /**
      * SQL 수행 결과 - MyRelation에서는 age와 상관없이 항상 100개를 return했는데,
      *               SQL의 결과는 age=1 조건을 만족하는 것만 출력되었다.
      +--------+
      |count(1)|
      +--------+
      |1       |
      +--------+

      */
  }
}
```

{% include spark-reco.md %}
