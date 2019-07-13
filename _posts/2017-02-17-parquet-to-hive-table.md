---
layout: post
title: "Parquet File을 Hive Table로 만들기"
categories: "bigdata"
---

[Stackoverflow에 올라온 Q&A](http://stackoverflow.com/questions/33625617/creating-hive-table-using-parquet-file-metadata)를 약간 수정해보았다.

아래의 Scala code를 이용하면 Parquet File을 Hive Table로 만들 수 있다.

```
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.hive.HiveContext

val sqlContext = new org.apache.spark.sql.hive.HiveContext(sc)

val schema_info = sqlContext.parquetFile("/path/to/_common_metadata")

def get_create_stmt(table_name : String, schema_info : DataFrame) : String = {
  val col_definition = (for (c <- schema_info.dtypes) yield(c._1 + " " + c._2.replace("Type",""))).mkString(", ")

  var create_stmt = s"""CREATE EXTERNAL TABLE ${table_name}
                 (
                    ${col_definition}
                 ) STORED AS PARQUET LOCATION '/path/to/'"""

  create_stmt
}

// CREATE EXTERNAL TABLE ...
val create_stmt = get_create_stmt("my_tab", schema_info)

sqlContext.sql(create_stmt)
```


