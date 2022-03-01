---
layout: post
title: "Scala에서 Mockito 사용하기 with mockito-scala"
categories: "programming"
---

{% include test-for-data-engineer.md %}

## 안내

테스트에 사용된 버전

- Scala: 2.12.12
- Java: 11
- Mockito scala: 1.16.42
- scala test: 3.0.5

## 1. 개요

scala에서 mocking하는 방법을 찾아보니 [Testing with mock objects](https://www.scalatest.org/user_guide/testing_with_mock_objects)라는 글에 다음과 같은 것들이 소개되었다.

- [ScalaMock](https://www.scalatest.org/user_guide/testing_with_mock_objects#scalamock)
- [EasyMock](https://www.scalatest.org/plus/easymock)
- [JMock](https://www.scalatest.org/plus/jmock)
- [Mockito](https://www.scalatest.org/plus/mockito)

아무래도 Mockito를 사용하는 것이 나중에 Java에서 개발할 떄를 대비하면 좋을 것 같다.

Scala에서 Mockito를 사용하는 방법은 최소 3가지가 있는 것 같다.

- Java용 Mockito를 그대로 사용 (이하 Vanilla 방식)
- [scalatest plus의 Mockito](https://github.com/scalatest/scalatestplus-mockito)를 사용
- [Mockito scala](https://github.com/mockito/mockito-scala)를 사용

인생은 선택의 연속이다. 선택할 것이 1개만 있다면 고민이 없는데 선택지가 3개다보니깐 "Mockito 자체를 적용하는 테스트"보다 "어떤 걸 사용할지 고민"하는 시간이 더 걸린다.

최종적으로는 Mockito scala를 선택했다. 이유는 다음과 같다.

- Vanilla 방식
    - Scala가 JVM 기반이지만  Scala와 Java의 언어적 특성 때문에 뭔가 오류가 있을 것만 같다
        - 예를 들어 다음과 같은 고민들 말이다
        - "Scala Object의 method를 잘 mocking할 수 있을까?", "Option 타입도 잘 작동할까?"
    - 오류가 없더라도 Java에서 Mockito 사용하는 문법이 Scala와 동일할지 걱정이었다
    - 실제 Stackoverflow의 [Mockito for Objects in Scala](https://stackoverflow.com/questions/16443801/mockito-for-objects-in-scala)라는 질문이 있다
- 이러한 이유로 Vanilla 방식보다는 Mockito를 선택하였다
- scalatest plus
    - github에서 Star가 10개 밖에 안 되었다
    - 또한 [Why is scalatest MockitoSugar deprecated?](https://stackoverflow.com/q/48552339/2930152)라는 질문을 보면 뭔가 버전 별로 사용법이 좀 헷갈려보였다
- Mockito scala의 경우 Star가 266개라서 고민없이 Mockito scala를 선택하였다

(참고: Star 개수는 2021.10.03 현재 기준)

## 2. 예제 코드

아래의 코드는 본인의 github에 올려두었다. [바로가기](https://github.com/jason-heo/mockito-scala-example)

### 2-1) `build.sbt` 설정

```scala
libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.5" % "test",
  "org.mockito" %% "mockito-scala-scalatest" % "1.16.42" % "test"
)
```

maven respoity는 [여기](https://search.maven.org/search?q=mockito-scala)에서 검색할 수 있다.

`mockito-scala`와 `mockito-scala-scalatest`가 따로 있는데 두 개의 차이는 잘 모르겠다. 본인의 경우 `mockito-scala-scalatest`만 의존성에 추가했는데 잘 작동되었다.

### 2-2) 간단한 예제

mocking이 잘 작동하는지 간단히 3가지 예제를 작성해봤다.

- `when().thenReturn()`
- `when().thenAnswer()`
- Object method에 대한 mocking

```scala
import org.mockito.ArgumentMatchers.{any, anyInt, anyString}
import org.mockito.IdiomaticMockito.StubbingOps
import org.mockito.MockitoSugar.{mock, when, withObjectMocked}
import org.mockito.invocation.InvocationOnMock
import org.scalatest.{BeforeAndAfterAll, FlatSpec}
import org.scalatest.Matchers.{be, convertToAnyShouldWrapper}

class DbService {
  // return: id에 해당하는 user의 name을 return
  def getName(id: String): String = {
    s"name of '${id}'"
  }

  // return: BMI(체질량 지수)를 반환
  // Answer test를 위해 height String으로 하였음
  def getBMI(heightInMeter: String, weight: Int): Double = {
    weight.toDouble / (heightInMeter.toDouble * heightInMeter.toDouble)
  }
}

object DbService {
  def getVersion(): String = "v1"
}

class MockitoTest extends FlatSpec with BeforeAndAfterAll{
  val mockDbService = mock[DbService]

  // mocking
  override def beforeAll: Unit = {
    when(mockDbService.getName(any())).thenReturn("My name")

    // Answer test
    // Answer 단축 표현: https://github.com/mockito/mockito-scala#function-answers
    when(mockDbService.getBMI(anyString(), anyInt())).thenAnswer((i: InvocationOnMock) => {
      val height: String = i.getArgument[String](0)
      val weight: Int = i.getArgument[Int](1)

      height.toDouble + weight
    })
  }

  "mockito-scala" should "mock basic features" in {
    mockDbService.getName("id") should be ("My name")

    // 원래는 20.761245674740486 이어야 정상이지만, mock을 통해서 값을 변경했으므로 61.7이 반환되어야 한다
    mockDbService.getBMI("1.7",60) should be (61.7)
  }

  "mockito-scala" should "mock Object's method" in {
    // mocking Object
    // https://github.com/mockito/mockito-scala#mocking-scala-object
    withObjectMocked[DbService.type] {
      DbService.getVersion() returns "v2"

      DbService.getVersion() should be ("v2")
    }
  }
}
```

### 2-3) `object` method mocking 시 주의사항

`src/test/resources/mockito-extensions/org.mockito.plugins.MockMaker`라는 파일에 아래 내용을 추가해야한다.

```
mock-maker-inline
```

`src/test/resources/mockito-extensions`까지는 디렉터리이고 `org.mockito.plugins.MockMaker`는 파일이다.

[참고 문서](https://github.com/mockito/mockito-scala#mocking-scala-object)

### 2-4) 더 많은 예제 확인하기

[Mockito scala의 테스트 코드](https://github.com/mockito/mockito-scala/tree/release/1.x/scalatest/src/test)를 참고하면 된다. scala 버전 별로 Test case가 작성되어 있으므로 도움이 된다.

## 3. 마무리

(부끄럽지만) 본인은 Mockito라는 걸 최근에 와서야 알게 되었다. 이제라도 Mockito를 Unit Test에 적용하기 위해 몇 가지 테스트한 내용을 정리해봤는데 Scala에서 mocking을 도입하려는 분에게 도움이 되면 좋겠다.

{% include test-for-data-engineer.md %}
