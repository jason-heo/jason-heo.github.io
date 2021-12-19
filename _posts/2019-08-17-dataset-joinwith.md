---
layout: post
title: "joinWith()를 이용한 Dataset JOIN"
categories: "bigdata"
---


[지난 글](/bigdata/2019/08/10/dataset-typed-api.html)에서는 Dataset의 typed API를 사용하여 select/filter/groupBy를 하는 방법을 설명했다.

이번 글에서는 Dataset을 JOIN할 때 사용하는 typed API인 `joinWith()`에 대해서 알아본다.

(본 글은 Spark 2.4 기준으로 작성된 것임을 알린다)

typed API를 사용하면 컴파일 타임에 에러 검사를 할 수 있기 때문에 좋다. select/filter/groupBy까지는 알게되었는데 JOIN 시에도 typed API를 사용할 수 없을까?하여 검색을 해보니, Jacek가 작성한 [Type-Preserving Joins — joinWith Operators](https://jaceklaskowski.gitbooks.io/mastering-spark-sql/spark-sql-joins.html#joinWith)라는 글을 찾을 수 있었다.

[`joinWith()`의 함수 signature](https://github.com/apache/spark/blob/branch-2.4/sql/core/src/main/scala/org/apache/spark/sql/Dataset.scala#L1076)는 아래와 같이 생겼는데,

```scala
joinWith[U](other: Dataset[U], condition: Column, joinType: String): Dataset[(T, U)]
```

이것만 봐서는 사용 방법을 알기 어려웠다. (글을 작성 중인 지금은 이해가 된다)

Jacek가 작성한 글에도 간단한 사용법이 나오지만, `joinWith()` 결과를 단순히 `printSchema()`만 하다보니 정화한 사용법을 알기 어려웠다. Spark 공식 문서에서도 사용법을 찾기 어려웠다.

그러던 중 Medium에 올라온 [Joining Spark Datasets](https://medium.com/datamindedbe/joining-spark-datasets-a1e356996e52)라는 글을 봤는데, 설명이 잘 되어 있어서 이해하기 쉬웠다.

`joinWith()`의 기본적인 사용법은 아래와 같다. (위 글에서 발췌)

```scala
articles
  .joinWith(views, 
            articles("id") === views("articleId"),    
            "left")
  .map { 
    case (a, null) => AuthorViews(a.author, 0)
    case (a,v) => AuthorViews(a.author, v.viewCount) 
  }
```

원래 이 글을 작성할 때는 본인 스스로 예제 Data를 만들어서 `joinWith()`의 사용 방법을 설명할까 했었는데 위의 Medium 글에 설명이 워낙 잘 되어 있어서 그냥 링크만 남기기로 했다. (예제 중에 오타가 있는 듯 한데 `"left"`과 `"full"`이 `"inner"`로 잘못 작성된 것 같다)

{% include spark-reco.md %}
