---
layout: post
title: "Spark 3.0.1에서 foreachBatch의 문제"
categories: "bigdata"
---

그동안은 현업에서 Spark 버전을 최신보다 약간 낮은 버전을 사용 중이었으나 최근 A 프로젝트부터는 Spark 최신 버전인 3.0.1을 사용 중에 있다.

그런데 Spark 2.4에서는 잘 작동하던 `foreachBatch`가 3.0.1에서는 잘 작동을 하지 않았다.

IntelliJ에서 코딩할 때는 오류 메시지가 없었지만 컴파일을 하면 아래와 같은 메시지가 출력되었다.

```
Error:(34, 25) overloaded method foreachBatch with alternatives:
(function: org.apache.spark.api.java.function.VoidFunction2[org.apache.spark.sql.Dataset[org.apache.spark.sql.Row],java.lang.Long])
org.apache.spark.sql.streaming.DataStreamWriter[org.apache.spark.sql.Row]
(function: (org.apache.spark.sql.Dataset[org.apache.spark.sql.Row], scala.Long) => Unit)
org.apache.spark.sql.streaming.DataStreamWriter[org.apache.spark.sql.Row] cannot be applied to
((org.apache.spark.sql.DataFrame, scala.Long) => org.apache.spark.sql.DataFrame)
askDF.writeStream.foreachBatch { (askDF: DataFrame, batchId: Long) =>
```

(에러 메시지 출처: https://stackoverflow.com/q/63137538/2930152)

분명히 [Spark 3.0.1 공식 문서](https://spark.apache.org/docs/latest/structured-streaming-programming-guide.html#foreachbatch)에도 `foreachBatch`에 대해서 아래와 같이 예제 코드가 있는데 작동하지 않는 원인은 확실히 모르겠다.

```scala
streamingDF.writeStream.foreachBatch { (batchDF: DataFrame, batchId: Long) =>
  // Transform and write batchDF 
}.start()
```

암튼 이것도 구글링해보니 역시 Stackoverflow에 답변이 있었다. 아래처럼 함수를 만들어서 `foreachBatch`에 전달하면 잘 돌아간다.

```scala
def myFunc(askDF: DataFrame, batchID: Long): Unit = {
    askDF.write.parquet("/src/main/scala/file.json")
}

askDF
    .writeStream
    .foreachBatch(myFunc _)
    .start()
```

(출처: https://stackoverflow.com/a/63176091/2930152)
