---
layout: post
title: "Spark 3.0부터 spark-shell에서 paste하기 편해졌다"
categories: "programming"
---

### 배경

우선 Spark 3.0 기반의 설명을 하기 전에 Spark 2.4 이하 버전에서 multi line 입력하는 방법에 대해서 알아보자.

아래와 같은 scala가 코드가 있다고 하자.

```scala
val df = Seq(("heo", 20), ("kim", 10)).toDF("name", "age")

df
  .filter("name = 'heo'")
  .count
```

`.`으로 연결된 multi line이 있을 때 line의 시작을 `.`로 하는 것이 convention이다.

그런데 가끔씩 source code를 copy해서 `spark-shell`에 paste할 때 위 포맷은 에러가 발생한다.

아래는 Spark 2.4의 `spark-shell`에 paste한 결과이다.

```scala
scala> val df = Seq(("heo", 20), ("kim", 10)).toDF("name", "age")
df: org.apache.spark.sql.DataFrame = [name: string, age: int]

scala>

scala> df
res0: org.apache.spark.sql.DataFrame = [name: string, age: int]

scala>   .filter("name = 'heo'")
<console>:1: error: illegal start of definition
  .filter("name = 'heo'")
  ^

scala>   .count
<console>:1: error: illegal start of definition
  .count
  ^
```

아무래도 REPL 방식으로 실행되다보니 줄이 바뀌었을 때 `.filter()`를 새로운 expression으로 인식할 수 밖에 없어서 발생하는 문제이다.

이를 해결하는 방법은 약 3가지가 있다.

1. `.`를 end of line으로 이동시키기
2. 괄호로 감싸기
3. `:paste` mode 사용

먼저 `.`를 end of line으로 이동한 코드는 다음과 같다.

```scala
val df = Seq(("heo", 20), ("kim", 10)).toDF("name", "age")

df.
  filter("name = 'heo'").
  count
```

그런데 IDE에서 개발한 코드를 복붙할 때는 `.`의 위치를 변경해야하므로 역시나 번거롭다.

두 번째로 괄호로 감싸는 방법은 다음과 같다.

```scala
scala> val df = Seq(("heo", 20), ("kim", 10)).toDF("name", "age")
df: org.apache.spark.sql.DataFrame = [name: string, age: int]

scala>

scala> (
     |   df
     |     .filter("name = 'heo'")
     |     .count
     | )
res5: Long = 1
```

`.`의 위치를 변경할 필요없지만 이것도 약간 불편하긴 한다.

마지막으로 `:paste`가 있는데, 기존 코드를 변경하지 않아도 되기 때문에 가장 편한 방법이다 (그런데 가끔 `:paste` 입력하는 걸 까먹을 때가 있다)

사용 방법은 간단하다  `spark-shell`에서 `:paste`를 입력한 뒤에 multi line을 입력하고 마지막에 <kbd>Crtl</kbd>+<kbd>D</kbd>를 입력하면 된다.

```scala
scala> :paste
// Entering paste mode (ctrl-D to finish)

val df = Seq(("heo", 20), ("kim", 10)).toDF("name", "age")

df
  .filter("name = 'heo'")
  .count

// Exiting paste mode, now interpreting.

df: org.apache.spark.sql.DataFrame = [name: string, age: int]
res0: Long = 1
```

### Spark 3.0부터 새로운 모드가 생겼다

올해부터 Spark on k8s를 사용 중인데 CDH 대비 장점으로 "Spark 버전을 내 맘대로 올리기 쉽다"는 것이 있다. (CDH가 별로라는 이야기는 아니다)

Spark 3.1을 사용하다가 깜빡하고 `:paste` 입력을 잊었는데 아래와 같이 작동을 하였다.

```scala
scala> val df = Seq(("heo", 20), ("kim", 10)).toDF("name", "age")
df: org.apache.spark.sql.DataFrame = [name: string, age: int]

scala>

scala> df
res0: org.apache.spark.sql.DataFrame = [name: string, age: int]

scala>   .filter("name = 'heo'")
res1: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [name: string, age: int]

scala>   .count
res2: Long = 1
```

결과에서 볼 수 있듯이 `.`로 시작하는 multi line도 정상적으로 수행되었다.

수행 결과가 `:paste` mode와 약간 다르긴하다. 작동 방법을 예상컨데 아마 `.filter(...)`가 입력되면 `spark-shell`에서는 `res0.filter(...)` 처럼 가장 마지막에 생성된 변수 기반으로 실행하는 것 같다.

따라서 다음과 같은 code가 있을 때 `cnt`의 data type은 `Long`이 아닌 `DataFrame`이 된다.

입력 코드

```scala
val cnt = df
            .filter("name = 'heo'")
            .count
```

`spark-shell`에서 실행한 결과

```scala
scala> val cnt = df
cnt: org.apache.spark.sql.DataFrame = [name: string, age: int]

scala>             .filter("name = 'heo'")
res3: org.apache.spark.sql.Dataset[org.apache.spark.sql.Row] = [name: string, age: int]

scala>             .count
res4: Long = 1

scala> cnt
res5: org.apache.spark.sql.DataFrame = [name: string, age: int]
```

이 경우는 어쩔 수 없이 `:paste` mode를 사용해야겠다.
