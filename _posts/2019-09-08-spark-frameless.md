---
layout: post
title: "Spark Dataset의 문제점과 frameless를 이용한 해결"
categories: "bigdata"
---

(아래 글은 작성 당시 최신 버전인 Spark 2.4.4 기준으로 작성된 것임을 알린다)

### 0. 들어가며

Spark의 Dataset typed API를 사용하면 에러를 빠르게 잡을 수 있고, IDE를 통한 코딩이 편해진다.

아직 Dataset typed API를 사용하지 않는 분은 아래 두 개 문서를 참고바란다.

- [Spark Dataset에서 typed API, untyped API 사용하기](/bigdata/2019/08/10/dataset-typed-api.html)
- [joinWith()를 이용한 Dataset JOIN](https://jason-heo.github.io/bigdata/2019/08/17/dataset-joinwith.html)

Dataset typed API도 만능은 아닌데, Frameless를 사용하면 Dataset typed API의 문제들을 쉽게 해결할 수 있다. 본인은 Frameless를 조사할 때 아래 문서를 참고하였다.

- [Spark Schema For Free](https://databricks.com/session/spark-schema-for-free]
- [Comparing TypedDatasets with Spark's Dataset](https://typelevel.org/frameless/TypedDatasetVsSparkDataset.html)
- [Frameless: A More Well-typed Interface for Spark](https://slides.com/longcao/intro-to-frameless#/)

### 1. Dataset

Spark을 사용한지 만 3년이 넘는 동안 Dataframe 기반의 untyped API만 사용하고 있었다. 사실 typed API를 제공한다는 사실도 모르고 있었다. Spark이 Dataframe 이후에 Dataset을 제공하는 건 알았지만 도입을 필요성을 모르고 관성에 의해 계속 Dataframe만 사용 중이었다.

그러던 중 진행되던 프로젝트가 취소되는 바람에 잠시 시간이 남아서 부족한 Spark 공부를 하면서 Dataset의 typed API를 제공한다는 것 알게 되어 관련된 블로깅오 해봤다. 신규 프로젝트에서는 Dataset과 typed API를 적극 사용 중인데 개인적으로는 만족 중이다.

블로그 유입 검색어를 보면 아직 typed API를 찾는 사람이 많이 없는 것 같다. 아마 예전의 나처럼 Dataframe을 사용할 것 같은데 (Spark의 예제 코드들도 Dataframe으로 작성된 것들이 많다) 아직 사용하지 않는다면 아래 본인이 작성한 아래 두 개 글을 참고바란다.

- [Spark Dataset에서 typed API, untyped API 사용하기](/bigdata/2019/08/10/dataset-typed-api.html)
- [joinWith()를 이용한 Dataset JOIN](https://jason-heo.github.io/bigdata/2019/08/17/dataset-joinwith.html)

하지만 모든 것이 그렇듯 Dataset도 만능은 아니다. 잠시 후 나오겠지만, Dataset에도 문제가 있다.

### 2. Spark AI Summit

Spark 관련하여 제일 큰 컨퍼런스는 [Spark AI Summit](https://databricks.com/sparkaisummit)이다. 미국과 유럽에서 한 차례씩 개최되고 아쉽게도 아시아에서는 열리지 않는다. 이틀 동안 Spark 관련된 수십 개 세션을 들을 수 있으나 한국에서 참가를 하려면 수백만원이 든다 (항공권, 호텔, 참가비) 그런데 컨퍼런스 이후 약 1개월 내로 거의 모든 세션의 발표 영상이 Youtube에 올라오며 모두 공짜로 볼 수 있다. 과거 영상은 [여기](https://www.youtube.com/channel/UC3q8O3Bh2Le8Rj1-Q-_UUbA/playlists)에서 볼 수 있다.

나도 관심있는 세션들을 정리 후 시간날 때 시청하려고 노력 중이다. 2018 유럽 컨퍼런스 중에서 내가 관심있는 것은 [여기](https://jason-heo.github.io/programming/2019/06/06/spark-ai-summit-2018-eu.html)에 정리해두었다. 그런데 보면 알겠지만, 진도가 잘 안나간다. 공짜라 그런가? 퇴근 후, 혹은 주말에 집에서 30분짜리 동영상을 시청하는데 한 두 시간은 걸리는 것 같다.

요즘 좀 바빠서 동영상 시청을 못하고 있다가, 한달 반만에 시청을 했는데 내가 고민 중이던 내용을 해결해주는 세션이었다. (작년 가을 세션이었으니 이 정도면 돈 주고 다녀와도 빨리 적용해부는 게 가성비가 높았을 것 같다)

해당 세션은 [Spark Schema For Free](https://databricks.com/session/spark-schema-for-free)로서 위에서 언급했던 Dataset의 문제를 해결하는 방법을 소개한다.

## 3. Dataset의 문제

Dataset의 문제점은 아래와 같다. 여기서 '문제'는 내가 바라볼 때의 '문제'이며 보는 관점에 따라 '문제'가 아닐 수 있다.

- 문제1: 특정 필드만 지정해도 모든 필드를 read함
- 문제2: Filter Pushdown 불가
- 문제3: GroupBy 시 코딩이 번거로움
- 문제4: Join 시 컬럼 이름을 참조

문제 하나하나에 대해서 코드로 살펴보자.

우선 Data를 준비하자. [이 글](https://jason-heo.github.io/bigdata/2019/08/10/dataset-typed-api.html)과 동일한 예제를 사용하지만, Filter Pushdown을 테스트하기 위해 Parquet로부터 Data를 읽어들일 것이다.

```scala
import spark.implicits._

case class Person(name: String, age: Int, gender: String)

val rawDs = Seq(
  Person("Kim", 20, "F"),
  Person("Lee", 30, "M"),
  Person("Park", 40, "F")
).toDS

rawDs.write.mode("overwrite").parquet("/tmp/example01")

val ds = spark.read.parquet("/tmp/example01").as[Person]
```

그럼 "문제1: 특정 필드만 지정해도 모든 필드를 read함" 부터 보자

`name` 필드만 SELECT하기 위해 다음과 같은 코드를 작성했다.

```scala
scala> ds.map(_.name).show
+-----+
|value|
+-----+
| Park|
|  Kim|
|  Lee|
+-----+
```

EXPLAIN을 해서 실행 계획을 보자. 눈에 확 들어오지 않아서 포맷은 임의로 변경했다.

```
scala> ds.map(_.name).explain
== Physical Plan ==
*(1) SerializeFromObject
+- *(1) MapElements
   +- *(1) DeserializeToObject newInstance(class $line28.$read$$iw$$iw$Person)
      +- *(1) FileScan parquet
             [name#40,age#41,gender#42] Batched: true,
             Format: Parquet,
             Location: InMemoryFileIndex[file:/tmp/example01],
             PartitionFilters: [],
             PushedFilters: [],
             ReadSchema: struct<name:string,age:int,gender:string>
```

제일 마지막 줄에 `ReadSchema: struct<name:string,age:int,gender:string>`를 보면 3개의 필드를 읽고 있다는 걸 알 수 있다. 즉, 내가 요청한 것은 `name` 필드 1개인데, Parquet에 존재하는 모든 필드를 읽게 된다.

"문제2: Filter Pushdown 불가"를 알아보자. SELECT 결과는 생략하고 EXPLAIN 결과만 본다.

```scala
scala> ds.filter(_.age == 20).map(_.name).explain
== Physical Plan ==
*(1) SerializeFromObject
+- *(1) MapElements
   +- *(1) Filter
      +- *(1) DeserializeToObject newInstance(class $line28.$read$$iw$$iw$Person),
         +- *(1) FileScan parquet
             [name#40,age#41,gender#42] Batched: true,
             Format: Parquet, Location:
             InMemoryFileIndex[file:/tmp/example01],
             PartitionFilters: [],
             PushedFilters: [],
             ReadSchema: struct<name:string,age:int,gender:string>
```

이번엔 `PushedFilters: []`를 주목해야 한다. `age=20` 필드만 가져오도록 지정하였으나, Push된 Filter가 하나도 없다.

첫 번째, 두 번째 문제를 해결하기 위해 `ds.select("name").show`를 하면 해결되는 문제 아니냐고 묻는다면 아직 untyped API vs typed API의 차이를 잘 모르는 것이다. 

무슨 소리인지 아예 모른다면, `ds.filter("age = 20").select("name").explain`과의 결과를 눈으로 꼭 확인하기 바란다. 본 글은 Frameless에 대한 설명이기 때문에 부차적인 것은 생략했다.

문제1,2는 성능과 직결되는 문제인 반면 "문제3: GroupBy 시 코딩이 번거로움"은 말 그대로 코딩이 번거롭다는 점이다. 자세한 코드는 [여기](https://jason-heo.github.io/bigdata/2019/08/10/dataset-typed-api.html)의 "5"번 절을 보기 바란다. 말로 대략 설명하자면,`GroupByKey()`의 key에 Tuple을 사용하면 그나마 번거로움이 덜한데 필드 이름 대신 Tuple의 필드 번호를 이용하거나 match를 사용해야 하는데 가독성이 떨어지고 error-prone한 방법이다. 매번 `case class`를 만드는 것도 번거롭다. 원래 input의  `case class`를 그대로 사용하고 key에 사용되지 않는 필드는 null 등의 default value를 줄 수도 있으나 이것도 불편하다.


"문제4: Join 시 컬럼 이름을 참조"는 `joinWith()`의 어정쩡함에 대한 문제이다.

아래 예제처럼 JOIN condition을 지정할 때는 특이하게도 필드명을 String으로 지정하므로 Runtime에 에러가 발생한다.

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
(`joinWith()`에 대한 사용 방법은 [여길](https://jason-heo.github.io/bigdata/2019/08/17/dataset-joinwith.html) 참고)

<BR>
<BR>
<BR>
{% include adsense-content.md %}
<BR>
<BR>
<BR>

## 4. Frameless 사용법

"문제가 무엇인가"를 정의하기 위해 서론이 길어졌는데, 이제 본격적으로 Frameless의 사용법을 알아보자.

본인은 아래 3개의 글을 참고했다.

- [Spark Schema For Free](https://databricks.com/session/spark-schema-for-free]
- [Comparing TypedDatasets with Spark's Dataset](https://typelevel.org/frameless/TypedDatasetVsSparkDataset.html)
- [Frameless: A More Well-typed Interface for Spark](https://slides.com/longcao/intro-to-frameless#/)

Spark 버전에 따라 사용되는 Frameless의 버전이 다르므로 아래 문서를 참고한다.

https://github.com/typelevel/frameless#versions-and-dependencies

`spark-shell`을 사용하는 경우 아래처럼 packages 옵션을 지정하면 된다.

```
$ spark-shell --packages=org.typelevel:frameless-dataset_2.11:0.8.0
```


`build.sbt`를 사용하는 경우는 [여길](https://github.com/typelevel/frameless#quick-start) 참고하면 된다.

우선 frameless에서 제공하는 `TypedDataset`을 생성하자.

```scala
import frameless.TypedDataset
import frameless.syntax._

case class Person(name: String, age: Int, gender: String)

val ds = spark.read.parquet("/tmp/example01").as[Person]

val fds = TypedDataset.create(ds)
```

이제 `TypedDataset`의 EXPLAIN 결과를 봄으로서 "문제1", "문제2"가 해결되는 것을 보자.

```scala
scala> fds.filter(fds('age) === 10).select(fds('age)).explain()
== Physical Plan ==
*(1) Project [age#1 AS _1#320]
+- *(1) Filter (isnotnull(age#1) && (age#1 = 10))
   +- *(1) FileScan parquet
    [age#1] Batched: true,
    Format:
    Parquet,
    Location: InMemoryFileIndex[file:/tmp/example01],
    PartitionFilters: [],
    PushedFilters: [IsNotNull(age), EqualTo(age,10)],
    ReadSchema: struct<age:int>
```

위의 결과에서 `PushedFilters`와 `ReadSchema`에서 볼 수 있듯이 Filter Pushdown도 잘 되었고, 원하는 필드만 읽고 있다.

만약 잘못된 필드명을 입력한 경우 다음과 같이 컴파일 타임에 에러가 발생한다.

```scala
fds.filter(fds('age2) === 10).select(fds('age)).explain()
```

아래는 위 코드를 컴파일할 때 에러 메시지이다.

```
[error] testscala:10:19: No column Symbol with shapeless.tag.Tagged[String("age2")] of type A in Person
[error]     fds.filter(fds('age2) === 10).select(fds('age)).explain()
[error]                   ^
[error] one error found
[error] (Compile / compileIncremental) Compilation failed
```

그런데 아쉽게도 IntelliJ에서는 error가 highlight되지 않는다.

이 부분이 좀 많이 아쉽다.

원래는 "문제3", "문제4" 즉, frameless에서 GroupBy와 Join에 대한 사용법도 적으려했으나, 주말 낮에 업무도 보고 저녁 늦게 블로깅 하느라 이쯤에서 마무리한다. (글 을 작성하면서 IntelliJ와 연동이 안 좋다는 걸 알고나서 피곤이 더 몰려왔다)

인터넷 상에 있는 다른 문서를 좀 더 찾아봤는데 [Typesafe data analytics](https://georgheiler.com/2018/02/26/typesafe-data-analytics/)도 좋아보인다. (내가 작성하려던 의도와 많이 비슷하다. 단 GroupBy 예제는 없다)

{% include spark-reco.md %}
