---
layout: post
title: "Scala에서 val과 def로 만든 함수의 차이"
categories: "programming"
---

## 개요

Spark에서 다음과 같이 UDF를 생성할 수 있다.

```
// 함수 생성
val toUpper: String => String = _.toUpperCase

// 함수 등록
val upper = org.apache.spark.sql.functions.udf(toUpper)

// test용 class
case class Person(name: String, age: Int)

// test용 DataFrame 생성
val df = Seq(Person("kim", 10), Person("lee", 20)).toDF

// UDF 결과 확인
df.withColumn("upper_name", upper('name)).show

+----+---+----------+
|name|age|upper_name|
+----+---+----------+
| kim| 10|       KIM|
| lee| 20|       LEE|
+----+---+----------+
```

([참고 자료](https://jaceklaskowski.gitbooks.io/mastering-spark-sql/spark-sql-udfs.html))

종종 UDF를 사용하면서 드는 생각이 왜 `val`로 함수를 만들었을까? `def`로 함수를 만들며 되지 않을까였다.

그런데, `def`로 함수를 만들면 다음과 같은 에러가 발생한다.

```
// 함수 생성
def toUpper(value: String) = value.toUpperCase

// 함수 등록
val upper = org.apache.spark.sql.functions.udf(toUpper)

<console>:25: error: missing argument list for method toUpper
Unapplied methods are only converted to functions when a function type is expected.
You can make this conversion explicit by writing `toUpper _` or `toUpper(_)` instead of `toUpper`.
       val upper = org.apache.spark.sql.functions.udf(toUpper)
                                                      ^
```

(참고: 에러를 우회하는 방법으로 위의 에러 메시지에서 볼 수 있듯이 아래와 같이 underscore를 붙여주는 방법이 있다)

```
// 함수 생성
def toUpper(value: String) = value.toUpperCase

// 함수 등록
val upper = org.apache.spark.sql.functions.udf(toUpper _)
```

## `val` 함수와 `def` 함수의 차이

이후 내용은 [Scala: The Differences Between `val` and `def` When Creating Functions](https://alvinalexander.com/scala/fp-book-diffs-val-def-scala-functions)라는 글을 초간단 요약한 내용이다.

우선 `def` 함수는 엄밀하게는 "함수"가 아니라 method이지만 편의를 위해 계속 "함수"라고 부른다.

`val` 함수는 `Function` class의 instance 변수이다.

scala REPL에서 val 함수를 만든 후, 결과를 보면 val 함수는 `Function1`의 instance 임을 알 수 있다.

```
scala> val toUpperVal: String => String = _.toUpperCase
toUpperVal: String => String = <function1>
```

`Function1`은 함수의 인자가 1개인 것을 나타낸다.

인자가 2개인 함수를 만들어보자.

```
scala> val sumVal = (x: Int, y: Int) => x + y
sumVal: (Int, Int) => Int = <function2>
```

Scala에서는 (최소한 version 2.11까지는) 이런 식으로 `Function0`부터 `Function22`까지 즉, 0개의 인자부터 22개의 인자까지 받을 수 있는 Class를 제공한다.

Function class에는 `apply` method가 존재한다. 우리가 `sumDef(1, 2)` 처럼 호출을 할 때 호출하는 생김새가 함수 같아서 `sumDef`가 함수처럼 보이지만, 사실 이것은 Scala의 syntactic sugar이다. 혹자는 이런 것 때문에 Scala의 사용이 편하다고 할지 모르겠지만 내부 작동 구조를 알기 어렵기 때문에 개인적으로 이런 synstactic sugar는 가급적 없는 게 좋을 것 같다. 이건 위 블로그 의견이 아니라 개인 의견인데 뭐 본인은 흔한 개발자 중의 하나이니 내가 잘못 생각하는 것이 있겠지)

그래서, `sumVal(10, 20)`을 사용하는 것은 내부적으로 `sumVal.apply(10, 20)`을 사용하는 것과 동일하다.

```
scala> sumVal(10, 20)
res5: Int = 30

scala> sumVal.apply(10, 20)
res6: Int = 30
```

val 함수는 `Function` class의 instance 변수이므로, 다음과 같이 anonymous class를 사용하여 val 함수를 생성할 수도 있다.

```
scala> val strLen = new Function1[String, Int] {
     |     def apply(a: String): Int = a.length
     | }
strLen: String => Int = <function1>

scala> strLen("kim")
res8: Int = 3
```

반면 def 함수를 만들어보면 그 결과가 val 함수와 사뭇 다르다.

```
scala> def sumDef(x: Int, y: Int) = x + y
sumDef: (x: Int, y: Int)Int
```

def 함수는 그냥 method일 뿐이다.

Spark의 udf를 생성하는 함수를 보면 다음과 같이 `Function` type의 인자를 받도록 되어 있다.

```
  /**
   * Defines a user-defined function of 1 arguments as user-defined function (UDF).
   * The data types are automatically inferred based on the function's signature.
   *
   * @group udf_funcs
   * @since 1.3.0
   */
  def udf[RT: TypeTag, A1: TypeTag](f: Function1[A1, RT]): UserDefinedFunction = {
    val inputTypes = Try(ScalaReflection.schemaFor(typeTag[A1]).dataType :: Nil).toOption
    UserDefinedFunction(f, ScalaReflection.schemaFor(typeTag[RT]).dataType, inputTypes)
  }
```

따라서 udf 등록 시, def 함수를 지정하면 에러가 발생했던 것이다.

개요에서 언급한 우회 방식은 def 함수를 val 함수로 변환해주는 방식을 사용했기 때문에 udf 등록이 가능했던 것이다.

```
scala> sumDef _
res7: (Int, Int) => Int = <function2>
```
