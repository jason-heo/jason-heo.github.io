---
layout: post
title: "Scala case class를 Spark의 StructType으로 변환하기"
categories: "bigdata"
---

`Encoders`를 이용하면 `case class`를 `StructType`으로 쉽게 변환할 수 있다.

우선 간단한 변환 결과를 보자.

```scala
scala> import org.apache.spark.sql.Encoders
scala> case class Person(name: String, age: Int)

scala> println(Encoders.product[Person].schema)
StructType(StructField(name,StringType,true), StructField(age,IntegerType,false))
```

case class인 `Person`의 멤버 변수들을 `StructField`로 한 `StructType`이 생성된 것을 볼 수 있다.

이걸 어디에 써먹을 수 있을까? Spark에서 json이나 csv 파일을 읽어을 때는 `StructType` 형식의 schema를 지정해야 한다. 그런데, 읽어들은 data를 Dataset으로 변환하기 위해서는 case class를 지정해야 한다.

이처럼 schema 지정과 Dataset 변환을 위해 `StructType`과 `case class`를 둘다 유지하는 것은 번거로울 뿐더러 에러가 발생하기 쉽다. 아래 코드를 보자.

```scala
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}
import org.apache.spark.sql.Encoders

case class Person(name: String, age: Int)

val structTypeSchema = StructType(
    List(
      StructField("name", StringType, true),
      StructField("age", IntegerType, false)
    )
)

val ds = spark.
  read.
  schema(structTypeSchema).
  csv("file:///tmp/test.csv").as[Person]
```

case class인 `Person` 이외에 schema를 지정하기 위한 `structTypeSchema`라는 변수도 선언을 했다. `Person`의 경우 필드가 두 개라서 코딩량이 적고 에러 발생 가능성이 적지만, 필드가 더 많아지게 되면 코딩량도 많아지고 `StrucType`에 필드 순서를 잘못 지정하는 경우 주소 필드에 전화번호가 입력되는 등의 실수가 발생할 확율이 커진다.

아래는 case class만 사용한 예이다.

```scala
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}
import org.apache.spark.sql.Encoders

case class Person(name: String, age: Int)

val ds = spark.
  read.
  schema(Encoders.product[Person].schema).
  csv("file:///tmp/test.csv").as[Person]
```

처음 코드보다 확실히 깔끔해진 것을 볼 수 있다.

그렇다면 반대로 `StructType`을 `case class`로 변환할 수 있을까?

`StructType`을 traverse하면서 `case class`용 string을 생성하는 코드, 예를 들면 [Schema2CaseClass.scala](https://gist.github.com/yoyama/ce83f688717719fc8ca145c3b3ff43fd)가 있긴 한데, nested type을 지원하지 않는 듯 하다. 그리고 `case class`의 string만 생성되었을 뿐 이걸 가지고 다시 복붙해서 사용하라는 것 같다.

REPL 같은 걸 이용하면 동적으로 `case class`를 생성할 수 있을 것 같은데 이 부분은 잘 모르겠다.

그 이외에 `StrucType`을 `case class`로 변환하는 더 좋은 방법은 아직 찾지 못하였다.
