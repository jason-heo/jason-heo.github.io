---
layout: post
title: "Scala에서 Jackson 라이브러리 사용법"
categories: "programming"
---

본 포스팅은 두 가지 내용을 담고 있다.

1. scala에서 Jackson을 사용하는 방법
2. 그리고 jackson에 대한 기본적인 사용법들

검색해보면 다 나오는 내용이지만 나중에 쉽게 찾기 위해 정리해봤다

그리고 실제로 작동하는 repository도 하나 만들어봤다.

https://github.com/jason-heo/scala-jackson-example

검색해서 나오는 내용은 의존성이나 import가 누락되어 있어서 돌아가는 코드를 어떻게 작성해야하는지 헷갈릴 때가 있는데 이런 문제는 겪는 분들에게 도움이 될 것이라 생각한다.

### 목차

- [build.sbt에 의존성 추가하기](#buildsbt에-의존성-추가하기)
- [Serialize 기본](#serialize-기본)
- [pretty print](#pretty-print)
- [LocalDate Format 0 - Default Format](#localdate-format-0---default-format)
- [LocalDate Format 1 - JSR310 사용하기](#localdate-format-1---jsr310-사용하기)
- [LocalDate Format 2 - Array 대신 String으로 출력하기](#localdate-format-2---array-대신-string으로-출력하기)
- [LocalDate Format 3 - custom format 지정하기](#localdate-format-3---custom-format-지정하기)
- [Deserialize 1](#deserialize-1)
- [Deserialize 2](#deserialize-2)
- [Deserialize 3 - Map으로 변환하기](#deserialize-3---map으로-변환하기)
- [Deserialize 4 - LocalDate](#deserialize-4---localdate)

### build.sbt에 의존성 추가하기

아래 내용만 추가하면 된다.

jsr310은 Date 관련 포맷 때문에 필요하다.

```
libraryDependencies ++= Seq(
  "com.fasterxml.jackson.core" % "jackson-core" % "2.11.2",

  // JSR310 의 필요성: https://perfectacle.github.io/2018/01/16/jackson-local-date-time-serialize/
  "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310" % "2.11.2",

  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.11.2"
)
```

### Serialize 기본

`writeValueAsString()` 함수 하나면 class instance 변수를 json string으로 변환할 수 있다.

```scala
    case class Person(
                       name: String,
                       hasCat: Boolean
                     )

    val you: Person = Person(
      name = "Kim",
      hasCat = true
    )

    val objectMapper = new ObjectMapper() with ScalaObjectMapper

    objectMapper.registerModule(DefaultScalaModule)

    println(objectMapper.writeValueAsString(you))
```

출력 결과

```json
{"name":"Kim","hasCat":true}
```

### pretty print

`writerWithDefaultPrettyPrinter()`를 이용하면 이쁘게 출력할 수 있다.

```scala
    case class Person(
                       name: String,
                       hasCat: Boolean
                     )

    val you: Person = Person(
      name = "Kim",
      hasCat = true
    )

    val objectMapper = new ObjectMapper() with ScalaObjectMapper

    objectMapper.registerModule(DefaultScalaModule)

    println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(you))
```

출력 결과

```json
{
  "name" : "Kim",
  "hasCat" : true
}
```

### LocalDate Format 0 - Default Format

여기서부터 좀 어려워진다. LocalDate를 예로 들었지만, LocalDateTime에도 적용된다.

`birthDate`가 아주 이상하게 출력되었다.

```scala
  case class LocalDateFormat(
                              name: String,
                              hasCat: Boolean,

                              birthDate: LocalDate
                            )
  def localDateDefaultFormat(): Unit = {.
    val you = LocalDateFormat(
      name = "Kim",
      hasCat = true,
      birthDate = LocalDate.of(2000, 11, 23)
    )

    val objectMapper = new ObjectMapper() with ScalaObjectMapper

    objectMapper.registerModule(DefaultScalaModule)

    println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(you))
  }
```

출력 결과

```json
{
  "name" : "Kim",
  "hasCat" : true,
  "birthDate" : {
    "year" : 2000,
    "month" : "NOVEMBER",
    "chronology" : {
      "calendarType" : "iso8601",
      "id" : "ISO"
    },
    "monthValue" : 11,
    "dayOfMonth" : 23,
    "era" : "CE",
    "dayOfYear" : 328,
    "dayOfWeek" : "THURSDAY",
    "leapYear" : true
  }
}
```

### LocalDate Format 1 - JSR310 사용하기

JSR310을 사용하면 위의 결과보다는 포맷이 간단해진다.

```scala
  case class LocalDateTimeCustomFormat1(
                                         name: String,
                                         hasCat: Boolean,

                                         birthDate: LocalDate
                                       )

  // 출처1: https://www.baeldung.com/jackson-serialize-dates#iso-8601
  // 출처2: https://perfectacle.github.io/2018/01/16/jackson-local-date-time-serialize/
  def localDateCustomFormat1(): Unit = {
    val you = LocalDateTimeCustomFormat1(
      name = "Kim",
      hasCat = true,
      birthDate = LocalDate.of(2000, 11, 23)
    )

    val objectMapper = new ObjectMapper() with ScalaObjectMapper

    objectMapper.registerModule(DefaultScalaModule)
    objectMapper.registerModule(new JavaTimeModule)

    println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(you))
  }
```

출력 결과

```json
{
  "name" : "Kim",
  "hasCat" : true,
  "birthDate" : [ 2000, 11, 23 ]
}
```

하지만, 여전히 Array 형식이라서 별로 안 좋다

### LocalDate Format 2 - Array 대신 String으로 출력하기

```scala
  case class LocalDateCustomFormat2(
                                     name: String,
                                     hasCat: Boolean,

                                     birthDate: LocalDate
                                   )

  // 출처: 상동
  def localDateCustomFormat2(): Unit = {

    val you = LocalDateCustomFormat2(
      name = "Kim",
      hasCat = true,
      birthDate = LocalDate.of(2000, 11, 23)
    )

    val objectMapper = new ObjectMapper() with ScalaObjectMapper

    objectMapper.registerModule(DefaultScalaModule)
    objectMapper.registerModule(new JavaTimeModule)

    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS) <=== 요게 추가되었다

    println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(you))
  }
```

출력 결과

```json
{
  "name" : "Kim",
  "hasCat" : true,
  "birthDate" : "2000-11-23"
}
```

### LocalDate Format 3 - custom format 지정하기

`@JsonFormat` annotation을 사용하면 format을 지정할 수 있다

```scala
  // @JsonFormat test
  case class LocalDateCustomFormat3(
                                     name: String,
                                     hasCat: Boolean,

                                     @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd")
                                     birthDate: LocalDate
                                   )

  def localDateCustomFormat3(): Unit = {

    val you = LocalDateCustomFormat3(
      name = "Kim",
      hasCat = true,
      birthDate = LocalDate.of(2000, 11, 23)
    )

    val objectMapper = new ObjectMapper() with ScalaObjectMapper

    objectMapper.registerModule(DefaultScalaModule)
    objectMapper.registerModule(new JavaTimeModule())

    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

    println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(you))
  }
```

{% include adsense-content.md %}

### Deserialize 1

`readValue()`를 이용하여 deserialize할 수 있다.

```scala
  case class PersonDeserialize1(
                                 name: String,
                                 hasCat: Boolean
                               )


  // 출처: https://stackoverflow.com/a/31867613/2930152
  def basicDeserialize1(): Unit = {

    val jsonStr = """
    {
      "name": "Kim",
      "hasCat": false
    }
    """

    val objectMapper = new ObjectMapper() with ScalaObjectMapper

    objectMapper.registerModule(DefaultScalaModule)

    println(objectMapper.readValue[PersonDeserialize1](jsonStr))
  }
```

출력 결과

```
PersonDeserialize1(Kim,false)
```

### Deserialize 2

`configure()`를 이용하면 deserialize 관련된 많은 옵션을 지정할 수 있다.

두 가지 테스트해봤는데 내가 아직 잘 몰라서 그런지 생각했던 대로 돌아가지는 않았다.

필요할 때 다시 한번 테스트해봐야겠다.

```scala
  // configure test
  //   - json string에 필드가 누락된 경우 에러 테스트
  //   - json의 int를 Long Type으로 변환
  def basicDeserialize2(): Unit = {
    val jsonStr = """
    {
      "name": "Kim",
      "hasCat": false,
      "age": 1
    }
    """

    val objectMapper = new ObjectMapper()

      // 필드가 없는 경우 에러 테스트 -> 에러가 발생하지 않네;;
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)

      // true든 false든 deserialize가 잘 된다;;
      .configure(DeserializationFeature.USE_LONG_FOR_INTS, true)
      .registerModule(DefaultScalaModule)

    println(objectMapper.readValue(jsonStr, classOf[PersonDeserialize2]))
  }
```

출력 결과

```
PersonDeserialize2(Kim,false,null,1)
```

### Deserialize 3 - Map으로 변환하기

class instance가 아닌 Map 타입으로도 변환할 수 있다.

```scala
  // Map으로 변환
  def basicDeserialize3(): Unit = {
    val jsonStr = """
    {
      "name": "Kim",
      "hasCat": false,
      "age": 1
    }
    """

    val objectMapper = new ObjectMapper()

    objectMapper.registerModule(DefaultScalaModule)

    println(objectMapper.readValue(jsonStr, classOf[Map[String, Any]]))
  }
```

출력 결과

```
Map(name -> Kim, hasCat -> false, age -> 1)
```

### Deserialize 4 - LocalDate

필요해질 때 https://perfectacle.github.io/2018/01/15/jackson-local-date-time-deserialize/ 읽어보자.
