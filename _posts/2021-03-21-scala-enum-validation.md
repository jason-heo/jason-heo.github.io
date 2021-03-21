---
layout: post
title: "Scala enum value validation"
categories: "programming"
---

literal 대신 Enum을 사용하면 여러 장점이 있다. [What are enums and why are they useful?](https://stackoverflow.com/a/4709224/2930152) 참고

Scala의 Enum 사용법은 다음과 같다.

```scala
object Color extends Enumeration {
  val Blue = Value("Blue")
  val Red = Value("Red")
  val Black = Value("Black")
}

Color.withName("Blue")
// res3: Color.Value = Blue
```

하지만 지정되지 않은 Color가 입력되면 Exception이 발생하므로 프로그램이 중지되지 않기 위해선 input 검사를 잘 해야한다.

```scala
Color.withName("Green")

java.util.NoSuchElementException: No value found for 'Green'
  at scala.Enumeration.$anonfun$withName$2(Enumeration.scala:148)
  at scala.Enumeration.withName(Enumeration.scala:148)
  ... 28 elided
```

이런 경우 아래의 `isValidColor()` 부분을 참고하면 허용된 값인지 검사를 할 수 있다.

```scala
object Color extends Enumeration {
  val Blue = Value("Blue")
  val Red = Value("Red")
  val Black = Value("Black")

  def isValidColor(str: String): Boolean = {
    values.exists(_.toString == str)
  }
}

Color.isValidColor("Red")
// res5: Boolean = true

Color.isValidColor("Green")
// res6: Boolean = false
```

코드를 살짝 수정하여 `UNKNOWN`을 추가해두면 사용하기 편한다.

```scala
object Color extends Enumeration {
  val Blue = Value("Blue")
  val Red = Value("Red")
  val Black = Value("Black")

  val UNKNOWN = Value("Unknown")

  def isValidColor(str: String): Boolean = {
    values.exists(_.toString == str)
  }

  def getColor(colorStr: String): Color.Value = {
    if (Color.isValidColor(colorStr)) {
      Color.withName(colorStr)
    }
    else {
      Color.UNKNOWN
    }
  }
}

Color.getColor("Red")
// res7: Color.Value = Red

Color.getColor("Green")
// res8: Color.Value = Unknown
```

출처: https://stackoverflow.com/a/22589600/2930152

혹은 위 출처의 다른 답변처럼 아예 return type을 `Option`으로 변경할 수도 있다.
