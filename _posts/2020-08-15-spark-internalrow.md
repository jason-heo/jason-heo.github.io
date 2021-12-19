---
layout: post
title: "Spark InternalRow 자료 구조 사용법"
categories: "bigdata"
---

`InternalRow`는 이름 그대로 Spark 내부에서 사용 중인 자료 구조로서, DataFrame의 `Row`를 생성하기 위한 자료 구조이다.

Spark 사용자로서 `InternalRow`를 사용할 일이 거의 없지만 Spark 내부 기능을 사용하는 경우는에 만나게 된다.

나도 그동안 Spark을 만 4년 넘게 사용하면서 `InternalRow`를 한번도 사용하지 않았지만, 최근 Custom Spark Streaming Source를 개발하면서 사용하게 되었다.

`InternalRow`에 대한 자료를 찾기가 어려워서 Spark 소스 코드를 참고하였다.

String, Integer 같은 Primitive Type은 만들기가 쉽지만 Map Type은 만들기가 좀 어려웠다.

참고 자료: [JacksonParser.scala](https://github.com/apache/spark/blob/branch-2.4/sql/catalyst/src/main/scala/org/apache/spark/sql/catalyst/json/JacksonParser.scala)

아래 소스 코드는 Spark 2.4에서 잘 돌아가는 것을 확인하였다.

```scala
// package를 org.apache.spark.sql로 사용하는 이유
// internalCreateDataFrame()이 private임

package org.apache.spark.sql

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.catalyst.util.{ArrayBasedMapData, DateTimeUtils, GenericArrayData}
import org.apache.spark.sql.types._
import org.apache.spark.unsafe.types.UTF8String

object InternalRowTest {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder().master("local[2]").getOrCreate()

    primitiveTypeTest(spark)

    arrayTypeTest(spark)

    timestampTypeTest(spark)

    mapTypeTest(spark)

    dynamicSchemaTest(spark)
  }

  def primitiveTypeTest(spark: SparkSession) = {
    val schema: StructType = StructType(
      Array(
        StructField("name", StringType),
        StructField("age", IntegerType)
      )
    )

    val internalRows: Seq[InternalRow] = Seq(
      InternalRow(UTF8String.fromString("Kim"), 10),
      InternalRow(UTF8String.fromString("Park"), null)
    )

    val rdd: RDD[InternalRow] = spark.sparkContext.parallelize(internalRows)

    val df: DataFrame = spark.internalCreateDataFrame(rdd, schema)

    df.show

    /*
    +----+----+
    |name| age|
    +----+----+
    | Kim|  10|
    |Park|null|
    +----+----+
     */

  }

  def arrayTypeTest(spark: SparkSession) = {
    val schema: StructType = StructType(
      Array(
        StructField("numbers", ArrayType(IntegerType))
      )
    )

    val internalRows: Seq[InternalRow] = Seq(
      InternalRow(new GenericArrayData(Array(10))),
      InternalRow(new GenericArrayData(Array(20, 30)))
    )

    val rdd: RDD[InternalRow] = spark.sparkContext.parallelize(internalRows)

    val df: DataFrame = spark.internalCreateDataFrame(rdd, schema)

    df.show

    /*
    +--------+
    | numbers|
    +--------+
    |    [10]|
    |[20, 30]|
    +--------+
     */
  }

  def timestampTypeTest(spark: SparkSession) = {
    val schema: StructType = StructType(
      Array(
        StructField("timestamp", TimestampType)
      )
    )

    val internalRows: Seq[InternalRow] = Seq(
      InternalRow(DateTimeUtils.fromMillis(System.currentTimeMillis)),
      InternalRow(DateTimeUtils.stringToTimestamp(UTF8String.fromString("1945-08-15T17:32:05.359+09:00")).get),
      InternalRow(DateTimeUtils.stringToTime("2020-01-01T12:34:56.789+09:00").getTime * 1000L)
    )

    val rdd: RDD[InternalRow] = spark.sparkContext.parallelize(internalRows)

    val df: DataFrame = spark.internalCreateDataFrame(rdd, schema)

    df.show(false)
    /*
    +-----------------------+
    |timestamp              |
    +-----------------------+
    |2020-08-15 17:26:48.262|
    |1945-08-15 17:32:05.359|
    |2020-01-01 12:34:56.789|
    +-----------------------+
     */
  }

  def mapTypeTest(spark: SparkSession) = {
    val schema: StructType = StructType(
      Array(
        StructField("map_data", MapType(StringType, StringType))
      )
    )

    val row1: ArrayBasedMapData = new ArrayBasedMapData(
      // key들
      new GenericArrayData(Array(UTF8String.fromString("name"), UTF8String.fromString("addr"))),

      // value들
      new GenericArrayData(Array(UTF8String.fromString("Kim"), UTF8String.fromString("Korea")))
    )

    val row2: ArrayBasedMapData = new ArrayBasedMapData(
      new GenericArrayData(Array(UTF8String.fromString("like"))),
      new GenericArrayData(Array(UTF8String.fromString("Spark")))
    )

    val internalRows: Seq[InternalRow] = Seq(
      InternalRow(row1),
      InternalRow(row2)
    )

    val rdd: RDD[InternalRow] = spark.sparkContext.parallelize(internalRows)

    val df: DataFrame = spark.internalCreateDataFrame(rdd, schema)

    df.show(false)
    /*
    +----------------------------+
    |map_data                    |
    +----------------------------+
    |[name -> Kim, addr -> Korea]|
    |[like -> Spark]             |
    +----------------------------+
     */
  }

  def dynamicSchemaTest(spark: SparkSession) = {
    val numFields: Int = 3

    val schema: StructType = StructType(Range(0, numFields).map(num => StructField(s"field_${num}", IntegerType)))

    val random = new scala.util.Random

    val row1: Seq[Any] = Range(0, numFields).map(_ => random.nextInt(10))
    val row2: Seq[Any] = Range(0, numFields).map(_ => random.nextInt(10))

    val internalRows: Seq[InternalRow] = Seq(
      InternalRow.fromSeq(row1),
      InternalRow.fromSeq(row2)
    )

    val rdd: RDD[InternalRow] = spark.sparkContext.parallelize(internalRows)

    val df: DataFrame = spark.internalCreateDataFrame(rdd, schema)

    df.show
    /*
    +-------+-------+-------+
    |field_0|field_1|field_2|
    +-------+-------+-------+
    |      5|      4|      0|
    |      4|      5|      9|
    +-------+-------+-------+
     */
  }
}
```

{% include spark-reco.md %}
