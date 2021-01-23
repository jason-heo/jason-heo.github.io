---
layout: post
title: "Scala test에서 whitespace 무시하고 비교하기"
categories: "programming"
---

scala test에서 json을 비교할 때 string이 너무 길면 diff를 확인하기가 쉽지 않다. 그렇다고 해서 json을 pretty하게 multi line으로 만들게되면 공백과 new line까지 신경을 써야 정확하게 일치하게 된다.

그런데 아래 방법을 이용하면 whitespace를 제거하고 정합성을 검사할 수 있다.

만약 diff가 발생하는 경우 IntelliJ에서는 어떤 부분에서 차이가 있는지 보여줄 때는 다시 whitespace를 복원하고 보여주기 때문에 여러모로 편하다.

아래 예처럼  new line이라던가 indentation이 다르더라도 test가 통과한다.

```scala
package example

import org.scalactic.{AbstractStringUniformity, Uniformity}
import org.scalatest._

class WhitespaceTest extends FlatSpec with Matchers {
  it should "generate valid json" in {
    val str = """
    {
      "name": "Kim",
      "age": 10
    }
    """

    str should equal("""{
    "name": "Kim",
    "age": 10
    }
    """)(after being whiteSpaceNormalized)
  }

  val whiteSpaceNormalized: Uniformity[String] = new AbstractStringUniformity {
    /** Returns the string with all consecutive white spaces reduced to a single space. */
    def normalized(s: String): String = s.replaceAll("(?s)\\s+", " ").trim

    override def toString: String = "whiteSpaceNormalised"
  }
}
```

(참고자료: https://stackoverflow.com/a/30729202/2930152)
