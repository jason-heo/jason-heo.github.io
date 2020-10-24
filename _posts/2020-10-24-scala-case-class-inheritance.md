---
layout: post
title: "Scala case class의 상속"
categories: "programming"
---

제목을 뭐라해야할지 고민다가 적당한 제목이 안 떠올라서 간단히 적었다.

Spark을 사용할 때는 case class를 빈번히 사용하는데 이게 상속이 안 되다보니 generic한 code를 작성하기가 어렵다.

그런데 아래처럼 하면 case class가 다른 class를 상속받도록 할 수 있다.

```scala
abstract class Animal {
    def name: String
}

case class Human(name: String, address: String) extends Animal

case class Cat(name: String, color: String) extends Animal

def printName(animal: Animal): Unit = {
    println(animal.name)
}

val me = Human("Heo", "Korea")
val myCat = Cat("Guru", "gray")

printName(me)
printName(myCat)
```

최소한 Scala 2.11, 2.12에서는 잘 돌아가는 것 확인 완료.

참고 자료: https://stackoverflow.com/a/53987270/2930152
