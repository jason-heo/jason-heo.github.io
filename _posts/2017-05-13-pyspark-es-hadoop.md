---
layout: post
title: "pyspark에서 Elasticsearch에 indexing하기"
categories: "programming"
---


scala에서는 `df.saveToEs()`를 이용하여 Elasticsearch에 indexing을 할 수 있어서 편하지만, pyspark에는 `saveToEs()`가 없다!!!

그래서 열심히 검색해봤더니, `saveAsNewAPIHadoopFile()`을 이용하라는 [글](https://prasanthkothuri.wordpress.com/2016/06/17/integrating-hadoop-and-elasticsearch-part-2-querying-and-writing-to-elasticsearch-from-apache-spark/)을 찾을 수 있었다.

문제는 예제 code가 잘못되어서 사용이 불가능하다는 것.

`DataFrame`에 `saveAsNewAPIHadoopFile()`를 호출하면 찾을 수 없다는 에러가 나온다

```
AttributeError: 'DataFrame' object has no attribute 'saveAsNewAPIHadoopFile'
```

그렇다고 `df.rdd.saveAsNewAPIHadoopFile()`처럼 호출하면 또 다른 에러가 나온다.

```
ERROR Executor: Exception in task 0.0 in stage 2.0 (TID 2) net.razorvine.pickle.PickleException: expected zero arguments for construction of ClassDict (for pyspark.sql.types._create_row)
```

열심히 또 검색 후에 [답](https://qbox.io/blog/elasticsearch-in-apache-spark-python)을 찾았다!

DataFrame을 아래와 같이 `PythonRDD` type으로 변환을 해줘야 한다.

```
>>> type(df)
<class 'pyspark.sql.dataframe.DataFrame'>

>>> type(df.rdd)
<class 'pyspark.rdd.RDD'>

>>> df.rdd.saveAsNewAPIHadoopFile(...) # Got the same error message

>>> df.printSchema() # My schema
root
 |-- id: string (nullable = true)
  ...

  # Let's convert to PythonRDD
  >>> python_rdd = df.map(lambda item: ('key', {
  ... 'id': item['id'],
      ...
      ... }))

      >>> python_rdd
      PythonRDD[42] at RDD at PythonRDD.scala:43

      >>> python_rdd.saveAsNewAPIHadoopFile(...) # Now, success
```
