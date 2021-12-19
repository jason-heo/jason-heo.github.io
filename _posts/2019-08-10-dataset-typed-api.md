---
layout: post
title: "Spark DataFrame vs Dataset (부제: typed API 사용하기)"
categories: "bigdata"
---

(아래 글은 Spark 2.2 기준으로 작성된 것임을 알린다)

### 목차

- [1.](#1)
- [2.](#2)
- [3.](#3)
- [4.](#4)
- [5.](#5)
- [6.](#6)

### 1.

Spark에서 DataFrame이 나오면서 Structured한 프로그래밍을 사용할 수 있게 되었다. 여기서 Structured가 무엇인지 명확한 정의를 내리기 어려우나 구조가 잡힘으로서 필드마다 data type을 지정할 수 있게 되어 내부적인 효율성을 늘리게 되었다 (메모리 구조라던지, 실행 계획이라던지)

Dataset에서는 한발 더 나아가 typed API를 사용하여 컴파일 타임에 에러를 잡을 수도 있게 되었다.

![Imgur](https://i.imgur.com/JJ1uudf.png)
([출처](https://databricks.com/blog/2016/07/14/a-tale-of-three-apache-spark-apis-rdds-dataframes-and-datasets.html))

쉽게 설명하자면, 프로그램에 오류가 있을 때(여기서 프로그램 오류라는 의미는 잘못된 필드명을 사용한다던가, Int 타입에 String 연산을 한다던가하는 오류를 의미한다), 프로그램을 실행하기 이전에 컴파일을 할 때 오류가 잡힌다는 것이다. 돌리는데 1시간이 걸리는 프로그램이있다고 생각해보자. 하필이면 필드명을 잘못 입력했는데 이게 수행된지 50분쯤에 돌아가는 코드에서 필드명을 잘못 입력했다면 50분 뒤에나 에러를 알 수 있게 된다.

typed api를 사용해야 컴파일 타임에 에러를 발견할 수 있어서 오류를 빠르게 찾을 수 있고, 또한 IntelliJ 같은 IDE를 사용할 때 오류가 발생한 위치가 정확히 표현된다.

### 2.

그럼 untyped API와 typed API를 사용했을 때 어떤 차이점이 있는지 보자.

우선 filtering을 할 때의 type/untyped API의 사용법과 오류 시 어떻게 보여지는지 예이다.

```scala
  import spark.implicits._

  case class Person(name: String, age: Int, gender: String)

  val ds = Seq(
    Person("Kim", 20, "F"),
    Person("Lee", 30, "M"),
    Person("Park", 40, "F")
  ).toDS

  // untyped API
  ds.filter("gender = 'F'")  // 에러 없음
  ds.filter("age2 = 20")     // 잘못된 필드명을 입력했으나, 컴파일 타입에 에러를 못 찾는다
  ds.filter("gender = true") // String과 Boolean의 비교

  // typed API
  ds.filter(row => row.gender == "F")
  ds.filter(row => row.age2 == 20)     // 잘못된 필드를 인식하고 빨갛게 표시가 되었다
  ds.filter(row => row.gender == true) // String과 Boolean의 비교가 가능해서 에러로 표시는 안 됨
                                       // 하지만 IDE가 경고를 해주었음
```

이해를 빠르게 하기 위해 IntelliJ 입력한 결과를 캡쳐해보았다.

![Imgur](https://i.imgur.com/fxPeFUY.png)

### 3.

다음은 특정 필드만 SELECT하는 예이다.

```scala
  // untyped API
  ds.select("name", "age2").show // 이것도 컴파일 타임에 에러가 안나오고, 프로그램을 돌릴 때 에러가 발생한다

  // typed API
  ds.map(row => (row.name, row.age2)).show // age2가 잘못된 걸 잘 표시해줌
```

![Imgur](https://i.imgur.com/NbJD6tn.png)

filtering과 select에서는 typed API의 사용법이 어렵지 않을 뿐더러 장점이 잘 드러난다. 그런데 GroupBy부터는 내가 제대로 찾지 못한 것인지 typed API를 사용하기가 쉽지 않고, aggregation function도 일부만 제공되는 것 같다.

우선 간단한 groupBy 예부터 보자.

```scala
  import org.apache.spark.sql.functions._
  ds.groupBy("gender").agg(sum("age")).show
  // 출력 결과
  +------+--------+
  |gender|sum(age)|
  +------+--------+
  |     F|      60|
  |     M|      30|
  +------+--------+

  // typed API
  import org.apache.spark.sql.expressions.scalalang.typed
  ds.groupByKey(row => row.gender).agg(typed.sum(_.age)).show
  // 실행 결과
  +-----+--------------------------------------------+
  |value|TypedSumDouble($line14.$read$$iw$$iw$Person)|
  +-----+--------------------------------------------+
  |    F|                                        60.0|
  |    M|                                        30.0|
  +-----+--------------------------------------------+
```

앞서 말했다시피 Dataset의 groupBy 관련 typed API를 찾기가 쉽지 않았는데, 겨우겨우 [Databricks에서 제공하는 노트북](https://cdn2.hubspot.net/hubfs/438089/notebooks/spark2.0/Dataset.html) 하나를 발견해서 이걸 참고해서 위의 코드를 작성했다.

### 4.

Dataset에 `groupByKey`라는 게 있다는 걸 겨우 알게되긴 했지만 새로운 문제가 있었으니, `agg()`에서 사용 중인 `typed`에서 제공하는 aggregation 함수가 몇 개 안된다는 점이었다.

Spark 2.2 기준 [typed](https://github.com/apache/spark/blob/branch-2.2/sql/core/src/main/scala/org/apache/spark/sql/expressions/scalalang/typed.scala)에서 제공되는 함수를 보면 avg/count/sum 3개의 함수만 제공되는 듯 하다.

나는 UV (순 방문자) 수 계산을 위해  distinct count를 연산할 수 있는 함수가 필요한데 typed API에서는 이게 제공이 안되고 있다ㅠㅠ

참고로 untyped API에서는 아래처럼 `countDistinct()` 함수를 사용하면 된다.

```
ds.groupBy("gender").agg(countDistinct("age")).show
+------+-------------------+
|gender|count(DISTINCT age)|
+------+-------------------+
|     F|                  2|
|     M|                  1|
+------+-------------------+
```

Aggregation 함수가 부족한 것은 Custom Aggregator를 구현하면 될 것 같은데, 이에 대해선 [Type-Safe User-Defined Aggregate Functions](https://spark.apache.org/docs/2.2.0/sql-programming-guide.html#type-safe-user-defined-aggregate-functions)에 설명되어 있다. (본인도 테스트해보지 않았음)

<BR>
{% include adsense-content.md %}
<BR>

### 5.

Dataset `groupByKey()`의 문제가 하나 더 있는데, 여러 개의 필드로 groupBy를 하고 싶어도 key는 1개로 지정된다는 점이다.

아래 예를 보자.

```scala
  ds.groupByKey(row => (row.name, row.gender)).agg(typed.sum(_.age)).show
  +---------+--------------------------------------------+
  |      key|TypedSumDouble($line14.$read$$iw$$iw$Person)|
  +---------+--------------------------------------------+
  |[Park, F]|                                        40.0|
  | [Lee, M]|                                        30.0|
  | [Kim, F]|                                        20.0|
  +---------+--------------------------------------------+
```

출력 결과의 `key` 필드 1개에, `[Park, F]` 처럼 두 개의 값이 저장된 걸 볼 수 있다. `groupByKey()`에서 `row => (row.name, row.gender)` 처럼 `Tuple2`로 변환된 값을 key로 사용하기 때문이다. 이걸 일반적인 SQL의 결과로 변경하려면 최종 aggregation을 다시 `map()` 해야하는데 Tuple을 다루기 때문에 또 `_1` 처럼 번호를 지정해야 하기 때문에 가독성이 떨어진다.

이를 방지하려면 `groupByKey()`에 case class를 넘겨주는 방법도 있다.

```scala
  case class GroupByKey(name: String, gender: String)
  case class ResultRow(name: String, gender: String, sum_age: Double)

  ds.
    groupByKey(row => GroupByKey(row.name, row.gender)).
    agg(typed.sum(_.age)).
    map {
      case (key, sum_age) => ResultRow(key.name, key.gender, sum_age)
    }.show

  // 실행 결과
  +----+------+-------+
  |name|gender|sum_age|
  +----+------+-------+
  |Park|     F|   40.0|
  | Lee|     M|   30.0|
  | Kim|     F|   20.0|
  +----+------+-------+
```

혼자서 스스로 작성한 코드는 아니고, [Databricks에 올라온 질문](https://forums.databricks.com/questions/11815/improve-performance-of-groupbykey-for-a-large-data.html)을 참고했다.

위의 코드 정도만 되고 Dataset의 `groupByKey()`를 사용하지 못할 수준은 아니었는데, 앞서 말했듯이 `countDistinct()` 연산을 하기가 힘들어서 아직은 `groupByKey()`를 실세 프로젝트 코드에 써야할지는 고민이다.

### 6.

untyped API인 `groupBy()`를 사용하는 경우에는 compile time에 에러를 잡기는 불가능하다. run-time에라도 에러를 빨리 잡기 위해선 `groupBy()`의 결과인 Dataframe을 Dataset으로 변환하면 좋다.

```scala
  case class ResultRow(name: String, gender: String, sum_age: Double)

  ds.
    groupBy("name", "gender").
    agg(sum("age").as("sum_age")).
    as[ResultRow].
    write.parquet("/path/to/")
```

컴파일 타입에는 에러를 잡을 수 없지만, 만약 필드 명이 잘못된 오류가 있다면 런타입 중이라도 `as[ResultRow]` 단계에서 Exception이 발생한다. 이를 통해 Dataframe을 사용할 때보다 에러를 빨리 잡을 수 있다.

예를 들어, 필드명이 잘못된 상태로 parquet 파일이 생성해서 외부로 전달하는 경우 사용하는 쪽에서 에러를 인지할 수 있지만 Dataset으로 변환하는 경우는 parquet로 변환하기 전에 에러가 발생하므로 외부로 파일을 전달하기 전에 오류가 발생한 것을 알게 된다.

{% include spark-reco.md %}
