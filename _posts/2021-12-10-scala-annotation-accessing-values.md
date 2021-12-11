---
layout: post
title: "Scala에서 Annotation 접근 방법"
categories: "programming"
---

### 들어가며

Scala에서 Annotation을 사용할 일이 생겼다. 인터넷을 보면 Annotation을 정의하는 글은 찾을 수 있었지만, Annotation의 값을 접근하는 예는 찾기가 어려웠다. 관련된 글을 찾더라도 `import` 구문이 없어서 작동하는 코드를 찾기가 어려웠다. 게다가 IntelliJ에서 Reflection 관련 import가 제대로 자동 완성되지 않아서 더 어려웠다.

본 글에서는 Annotation을 정의하고, 해당 Annotation이 정의된 class의 필드와 Annotation 값에 접근하는 code를 보여준다.

Scala 2.12.10에서 테스트된 코드이고 잘 작동된 것을 확인하였다.

### 참고한 자료들

- https://stackoverflow.com/a/17793050
- https://stackoverflow.com/a/37556999
- https://stackoverflow.com/a/48755442
- https://stackoverflow.com/a/13223299

### 주의 사항

- Annotation 필드 타입이 `String`, `Array[String]` 인 경우에만 작동한다
    - 그 외 타입은 직접 구현해야한다
    - 예를 들어, `Array[String]`을 `Seq[String]`으로 변환하면 작동하지 않는다
    - `Array`와 `Seq`의 parsing 결과가 다른 듯 하다
- `Array[String]`의 경우 좋은 구현 방법이 아닌데 더 좋은 방법을 찾지 못했다

### 코드

```scala
package io.github.jasonheo

import org.scalatest.FlatSpec
import org.scalatest.Matchers._

import scala.annotation.StaticAnnotation
import scala.reflect.runtime.universe._

class JsonAlias(aliases: Array[String]) extends StaticAnnotation

class JsonFormat(pattern: String) extends StaticAnnotation

class Person(
                   @JsonAlias(Array("name", "fullName"))
                   name: String,

                   @JsonAlias(Array("registeredDate", "regDate"))
                   @JsonFormat("yyyy-MM-dd")
                   registeredDate: String,

                   gender: String
                 )

class AnnotationTest extends FlatSpec {
  it should "return valid map for String annotation" in {
    val jsonFormatMap: Map[String, String] = getFieldAnnotationMap(typeOf[Person], typeOf[JsonFormat])
      .map {
        case (fieldName, annotation) => (fieldName, getStringValue(annotation))
      }

    jsonFormatMap.contains("name") should be(false)
    jsonFormatMap.getOrElse("registeredDate", "") should be("yyyy-MM-dd")
    jsonFormatMap.contains("gender") should be(false)
  }

  it should "return valid map of Seq[String]" in {
    val jsonAliasMap: Map[String, Seq[String]] = getFieldAnnotationMap(typeOf[Person], typeOf[JsonAlias])
      .map {
        case (fieldName, annotation) => {
          (fieldName, getArrayOfStringValue(annotation))
        }
      }

    jsonAliasMap.get("name") should be(Some(Seq("name", "fullName")))
    jsonAliasMap.get("registeredDate") should be(Some(Seq("registeredDate", "regDate")))
    jsonAliasMap.contains("gender") should be(false)
  }

  def getFieldAnnotationMap(targetClass: Type, targetAnnotation: Type): Map[String, Annotation] = {
    val fields: Iterable[TermSymbol] = targetClass
      .members // targetClass에 존재하는 모든 멤버 조회 (variable 이외에 method도 포함된다)
      .collect { case s: TermSymbol => s }
      .filter(s => s.isVal || s.isVar) // variable만 조회

    val map: Map[String, Annotation] = fields.flatMap((field: TermSymbol) => {
      field.
        annotations. // field의 annotation 조회
        find(annotation => annotation.tpe =:= targetAnnotation). // annotation의 type이 targetAnnotation인 것만 filtering
        map((annotation: Annotation) => {
          (
            field.fullName.split('.').last, // fullName="io.github.jasonheo.Person.name", split('.').last="name"
            annotation
          )
        }
        )
    }).toMap

    map
  }

  def getStringValue(annotation: Annotation): String = {
    val annotationValue: String = annotation
      .tree
      .children
      .tail
      .collect({
        case Literal(Constant(value: String)) => value // '@JsonFormat("yyyy-MM-dd")' 에서 "yyyy-MM-dd"만 발라내는 코드
      }).head

    annotationValue
  }

  def getArrayOfStringValue(annotation: Annotation) = {
    // println(annotation.tree)의 결과
    //
    // new io.github.jasonheo.JsonAlias(
    //   scala.Array.apply[String]("name", "fullName")
    //   (
    //     (ClassTag.apply[String](classOf[java.lang.String]): scala.reflect.ClassTag[String])
    //   )
    // )

    val annotationValue: Seq[String] = annotation
      .tree
      .children
      .tail
      .collect({ case tree => {
        // '@JsonAlias(Array("name", "fullName"))'에서 'Array("name", "fullName")'만 발라내는 코드
        // 이렇게 하는 게 맞는지, 혹은 이게 좋은 방법인지 확실치 않음
        tree
          .children
          .head
          .children
          .drop(1)
          .map {
            case Literal(Constant(value: String)) => value
          }
      }
      }).head

    annotationValue
  }
}
```

### `case class`에 적용 방법

위의 예는 `case class`에서는 작동하지 않는다.

이에 대한 해결책은 https://stackoverflow.com/a/13223299 에서 볼 수 있다.

제일 쉬운 방법은 다음과 같이 `@field`를 붙여주는 방법이다.

```scala
import scala.annotation.meta.field

case class Person(
                   @(JsonAlias @field)(Array("name", "fullName"))
                   name: String,

                   @(JsonAlias @field)(Array("registeredDate", "regDate"))
                   @(JsonFormat @field)("yyyy-MM-dd")
                   registeredDate: String,

                   gender: String
                 )

```

한눈에 봐도 복잡하고 사용하기 불편해보인다.

https://stackoverflow.com/a/28279648를 보면 `@field` 없이 사용하는 방법도 있는 듯 한데 이건 테스트해보지 못했다. (토요일 밤, 눈도 아프고 이제 쉬어야겠다)
