---
layout: post
title: "SparkSession의 implicit에 대한 이해"
categories: "bigdata"
---

설명에 사용된 Spark Version: 2.4.0

## 들어가며

Spark에서 Seq를 Dataframe으로 변환하는 `toDF()`라던가 String을 Column으로 변환하는 `$`을 사용하기 위해선 `import spark.implicits._`을 해야한다.

```scala
val spark: SparkSession = SparkSession
  .builder()
  .master("local[2]")
  .getOrCreate()

import spark.implicits._

val df: DataFrame = Seq(("Heo", 20), ("Park", 19)).toDF("name", "age")
```

희한한 것은 `object SparkSession`이 아닌 `class SparkSession`의 instance를 import한다는 점이다.

그리고 `var spark`인 경우 `import spark.implicits._`를 할 수 없다.

Unit Test를 만들다보면 `var spark`을 사용해야할 경우가 있는데 이때는 아래의 트릭을 사용하면 우회할 수 있다.

```
class SomeSpec extends FlatSpec with BeforeAndAfter { self =>

  var spark: SparkSession = _

  private object testImplicits extends SQLImplicits {
    protected override def _sqlContext: SQLContext = self.spark.sqlContext
  }
  import testImplicits._

  before {
    spark = SparkSession.builder().master("local").getOrCreate()
  }

  "a test" should "run" in {
    // implicits are working
    val df = spark.sparkContext.parallelize(List(1,2,3)).toDF()
  }
}
```

([출처](https://stackoverflow.com/a/39173501/2930152))

본 글에서는 다음과 같은 3개의 implicit에 대해서 알아본다.

- `toDF()`: Seq를 DataFrame으로 변환
- `$`: String을 Column으로 변환
- `'`: String을 Column으로 변환


## Seq를 DataFrame으로 변환: `toDF()`

`toDF()`를 이용하면 Seq를 Dataframe으로 변환할 수 있다. 이와 관련된 첫 번째는 `class SQLImplcits`에 정의된 `def localSeqToDatasetHolder()`이다.

```scala
abstract class SQLImplicits extends LowPrioritySQLImplicits {

  protected def _sqlContext: SQLContext

  ...

  /**
   * Creates a [[Dataset]] from a local Seq.
   * @since 1.6.0
   */
  implicit def localSeqToDatasetHolder[T : Encoder](s: Seq[T]): DatasetHolder[T] = {
    DatasetHolder(_sqlContext.createDataset(s))
  }

  ...
}
```

([관련 코드](https://github.com/apache/spark/blob/081c12bb2f9d72c3776e626ec23e871a24a88a88/sql/core/src/main/scala/org/apache/spark/sql/SQLImplicits.scala#L227-L229))

`localSeqToDatasetHolder()`는 분산되지 않은 (즉, driver local에 존재하는) Seq를 `_sqlContext.createDataset()`을 이용하여 Dataset으로 변환한다.

`toDF()`는 `case class DatasetHolder`에 정의되어 있다.

```scala
case class DatasetHolder[T] private[sql](private val ds: Dataset[T]) {

  // This is declared with parentheses to prevent the Scala compiler from treating
  // `rdd.toDS("1")` as invoking this toDS and then apply on the returned Dataset.
  def toDS(): Dataset[T] = ds

  // This is declared with parentheses to prevent the Scala compiler from treating
  // `rdd.toDF("1")` as invoking this toDF and then apply on the returned DataFrame.
  def toDF(): DataFrame = ds.toDF()

  def toDF(colNames: String*): DataFrame = ds.toDF(colNames : _*)
}
```

([관련 코드](https://github.com/apache/spark/blob/081c12bb2f9d72c3776e626ec23e871a24a88a88/sql/core/src/main/scala/org/apache/spark/sql/DatasetHolder.scala#L34-L45))

즉, 아래의 코드는

```scala
val df: DataFrame = Seq(("Heo", 20), ("Park", 19)).toDF("name", "age")
```

실제론 다음과 같은 과정을 통해서 Seq를 DataFrame으로 변환하는 것이었다.

```scala
val datasetHolder: DatasetHolder[(String, Int)] = Seq(("Heo", 20), ("Park", 19))

val dataFrame: DataFrame = datasetHolder.toDF("name", "age")
```

즉, Seq를 `DatasetHolder` 객체로 암묵적 (implicit) 변환을 한 뒤에, `DatasetHolder.toDF()`을 이용하여 DataFrame으로 변환을 하였다.

`localSeqToDatasetHolder`에 `implicit def`가 붙은 것을 볼 수 있는데, `immplicit def`는 A 타입을 B 타입으로 암묵적으로 변환할 때 사용되는 편한 듯 하면서도 가독성을 떨어트리고 코드를 이해하기 어렵게 만드는 것 중 하나다.

## String을 Column으로 변환: `$`

DataFrame에서 특정 컬럼을 select할 때 아래와 같은 방법을 사용할 수 있다.

```scala
// a)
df.select("name").show

// b)
df.select($"name").show
```

두 결과가 동일하게 작동을 하지만, Spark의 암묵적 변환이 없었다면 결과가 완전히 다르게 된다. `a)`는 `"name"`이라는 string 문자열을 select하는 것이고 `b)`는 'name' 필드의 값을 select하겠다는 것이다.

SQL로 표현하면 다음과 같다.

```sql
-- a)
SELECT "name" FROM tab

-- b)
SELECT name FROM tab
```

`a)` 방식은 모든 레코드마다 `"name"`이라는 문자열만 출력되게 된다. `b)` 방식에서는 레코드의 `name` 필드의 값이 출력된다.

`a)` 방식의 `select()`는 다음과 같이 정의되어 있다.

```scala
  /**
   * Selects a set of column based expressions.
   * {{{
   *   ds.select($"colA", $"colB" + 1)
   * }}}
   *
   * @group untypedrel
   * @since 2.0.0
   */
  @scala.annotation.varargs
  def select(cols: Column*): DataFrame = withPlan {
    Project(cols.map(_.named), logicalPlan)
  }
```
([관련 코드](https://github.com/apache/spark/blob/081c12bb2f9d72c3776e626ec23e871a24a88a88/sql/core/src/main/scala/org/apache/spark/sql/Dataset.scala#L1330-L1342))

<BR><BR><BR>

`b)` 방식의 `select()`는 다음과 같이 정의되어 있다.

```scala
  /**
   * Selects a set of columns. This is a variant of `select` that can only select
   * existing columns using column names (i.e. cannot construct expressions).
   *
   * {{{
   *   // The following two are equivalent:
   *   ds.select("colA", "colB")
   *   ds.select($"colA", $"colB")
   * }}}
   *
   * @group untypedrel
   * @since 2.0.0
   */
  @scala.annotation.varargs
  def select(col: String, cols: String*): DataFrame = select((col +: cols).map(Column(_)) : _*)
```
([관련 코드](https://github.com/apache/spark/blob/081c12bb2f9d72c3776e626ec23e871a24a88a88/sql/core/src/main/scala/org/apache/spark/sql/Dataset.scala#L1344-L1358))

즉, `select("name")`은 실제로는 `select(Column("name"))`으로 수행된다.

잠시 이야기가 새었는데 계속해서 `select($"name")`에 대해서 알아보자.

`$"name"`에 관련된 코드는 `class SQLImplcits.StringToColumn()`이다.

```scala
  implicit class StringToColumn(val sc: StringContext) {
    def $(args: Any*): ColumnName = {
      new ColumnName(sc.s(args: _*))
    }
  }
```

([관련 코드](https://github.com/apache/spark/blob/081c12bb2f9d72c3776e626ec23e871a24a88a88/sql/core/src/main/scala/org/apache/spark/sql/SQLImplicits.scala#L43-L47))

여기서는 `implicit class`가 등장하는데, 이미 존재하는 class에 새로운 함수를 추가하기 위해 사용되는 것이라고 한다. ([출처](https://blog.seulgi.kim/2014/09/scala-implicit-keyword-2-implicit-class.html))

즉, 이미 존재하는 `StringContext`에 `$()`라는 함수를 추가한 것이다.

`StringContext`는 scala에서 제공되는 class인데, 알고봤더니 scala에서 무심결에 사용하던 string interpolation도 `StringContext`에서 제공되던 함수였다.

```scala
    case class StringContext {
      def s(args: Any*): String = standardInterpolator(treatEscapes, args)
    }
```

`s"Hello, $name"`를 입력하면 내부적으론 `StringContext("Hello, ", "").s(name)`처럼 변환된다고 한다. (scala의 implicit 때문에 내부 작동 방식을 알기가 너무 어렵다)

{% include adsense-content.md %}

## String을 Column으로 변환: `'`

select 시 필드는 다음과 같은 방법으로도 지정 가능하다.

```scala
df.select('name)
```

그럼 `'`는 또 무엇인가? 본 글을 잘 읽었다면 느낌 상으로는 `"name"`이라는 문자열을 `name` Column으로 변환하는 것이라는 걸 짐작할 수 있다. 그런데 어떻게 작동하는 것일까?

처음엔 `$`와 마찬가지로 `def '()` 같은 게 있을 줄 알았다. 그런데 IDE에서 text 검색을 해봐도 안나왔다.

알고보니 `'`에 관련된 함수는 `def symbolToColumn()`였다.

```scala
  /**
   * An implicit conversion that turns a Scala `Symbol` into a [[Column]].
   * @since 1.3.0
   */
  implicit def symbolToColumn(s: Symbol): ColumnName = new ColumnName(s.name)
```

`'`는 scala에서 Symbol이라고 불리는 것이었다.

scala REPL에서 확인해보면 `'`의 타입이 Symbol인 걸 쉽게 확인할 수 있다.

```scala
scala> 'name
res1: Symbol = 'name
```

## 참고: `'`는 앞으로 없어질 것 같다

[Scala 논의](https://contributors.scala-lang.org/t/proposal-to-deprecate-and-remove-symbol-literals/2953)를 보면 앞으로 `'`가 없애려는 것 같다. `'` 대신 명시적으로 `sym`을 사용하려는 것 같다.

이에 따라 Spark 측에서도 자연스로 `df.select('name)` 같은 문법을 없애려는 것 같다. ([관련 논의](http://apache-spark-developers-list.1001551.n3.nabble.com/Do-you-use-single-quote-syntax-for-the-DataFrame-API-td26914.html))

## 마무리

`import spark.implicits._`를 보면 `SparkSession` class의 instance를 import하고 있다. 이건 어떻게 가능한 것일까? 그리고 왜 `SparkSession`의 instance가 필요한 것일까?

`SparkSession.implicits`는 object로서 다음과 같이 정의되어 있다.

```
class SparkSession {
  ...
  object implicits extends SQLImplicits with Serializable {
    protected override def _sqlContext: SQLContext = SparkSession.this.sqlContext
  }
  ...
}
```

([관련 코드](https://github.com/apache/spark/blob/081c12bb2f9d72c3776e626ec23e871a24a88a88/sql/core/src/main/scala/org/apache/spark/sql/SparkSession.scala#L702-L704))

`spark`이 `SparkSession`의 instance이긴 하지만, `implicits`가 object이기 때문에 import가 가능했다.

그리고 자기 자신의 `SQLContext` instance를 `SQLImplicits`의 `_sqlContext` 멤버로 override하고 있다. 그렇다, `SparkSession`의 instance가 필요한 이유는 `DataFrame`을 만들기 위해서 `SparkSession`이 필요했기 때문이다. `object SparkSession` 같은 곳에서는 Spark framework이 활성화(?)된 것이 없으로 Dataframe을 생성할 수가 없다.
