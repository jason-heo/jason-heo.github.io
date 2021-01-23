---
layout: post
title: "[Scala] Jackson에서 Timestamp 타입의 custom format 지정하기"
categories: "programming"
---

[Scala에서 Jackson 라이브러리 사용법](/programming/2020/11/27/scala-jackson-example.html)라는 글을 적은 적이 있는데 이때는 날짜 관련 타입을 `LocalDate`만 다뤘었다.

이번엔 `Timestamp` 필드에 대한 예를 보자. 어차피 `JsonFormat` annotation을 사용하는 것이라서 기존 포스트 내용과 거의 유사하다.

## 문제 정의

아래의 예는 json을 `Person` class instance로 생성한 뒤에 다시 json으로 serialize하는 예이다.

원본 json에는 `"birthDate": "2031-01-23T12:34:56+09:00"` 처럼 입력이 되었지만, 최종 json을 보면 `"birthDate" : 1926905696000` 처럼 Long형으로 바뀌어 있다.

```scala
import java.sql.Timestamp

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.{DefaultScalaModule, ScalaObjectMapper}
import org.scalatest.FlatSpec
import org.scalatest.Matchers._

case class Person(
                   name: String,
                   birthDate: Timestamp
                 )

class Kafka2EsSpec extends FlatSpec {
  behavior of "Jackson JsonFormat"

  "Timestamp" should "ser/de in human readable format" in {
    val json = """
    {
      "name": "Kim",
      "birthDate": "2031-01-23T12:34:56+09:00"
    }
    """

    val objectMapper: ObjectMapper = new ObjectMapper() with ScalaObjectMapper
    objectMapper.registerModule(DefaultScalaModule)

    val kim: Person = objectMapper.readValue(json, classOf[Person])
    println(kim)
    // 출력 결과: Person(Kim,2031-01-23 12:34:56.0)

    println(
      objectMapper
        .writerWithDefaultPrettyPrinter()
        .writeValueAsString(kim)
    )
    // 출력 결과
    // {
    //   "name" : "Kim",
    //   "birthDate" : 1926905696000
    // }
  }
}
```

`"birthDate" : 1926905696000`를 다시 deserialize하하더라도 어차피 Timestamp로 잘 인식하므로 내부적으로 사용하는데는 큰 문제가 없다. 그런데 만약 자료를 전달받는 측에서 Long 형 대신 `2031-01-23T12:34:56+09:00` 처럼 사람이 읽기 쉬운 포맷으로 전달해달라고 하면 어떻게 해야할까?

이때는 `JsonFormat` annotation을 사용하면 된다.

### `JsonFormat` Annotation 사용법

```scala
case class Person(
                   name: String,
                   @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone="Asia/Seoul")
                   birthDate: Timestamp
                 )
println(
  objectMapper
    .writerWithDefaultPrettyPrinter()
    .writeValueAsString(Person("Kim", Timestamp.valueOf("2031-01-23 12:34:56")))
)
```

출력 결과

```json
{
  "name" : "Kim",
  "birthDate" : "2031-01-23T12:34:56+09:00"
}
```

(출처: https://stackoverflow.com/a/27686789/2930152)
