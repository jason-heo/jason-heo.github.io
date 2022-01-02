---
layout: post
title: "Scala에서 외부 프로그램 exec 결과를 capture하기"
categories: "programming"
---

Scala에서 `!`를 이용하면 외부 프로그램을 쉽게 실행시킬 수 있다. 그런데 프로그램 실행 결과를 저장하려면 약간의 추가 작업이 필요한데 오늘은 이에 대해 알아보자.

### 프로그램 실행 기본

Scala에서 `!`를 이용하여 외부 프로그램을 실행시킬 수 있다. exit code는 `!`의 결과로 반환되고 프로그램의 output은 Scala의 output에 그대로 출력된다. 따라서 output을 저장할 수는 없다.

예를 보자.

```scala
scala> import sys.process._
import sys.process._

scala> val exitCode = "ls /usr/" !
X11
X11R6
bin
...

scala> println(exitCode)
0
```

`exitCode`에 `0`이 저장되었지만, `ls`의 결과는 그냥 화면에 출력되었다.

`!!`를 이용하면 프로그램의 output을 변수에 저장할 수 있다.

```scala
scala> val output = "ls /usr/" !!
output: String =
"X11
X11R6
bin
...
"
```

그런데 `!!`에서는 exit code를 알 수 없다. 또한 stderr의 내용은 `output`에 저장되지 않는다.

```scala
scala> val output2 = "ls /usr /foo" !!
ls: cannot access '/foo': No such file or directory
java.lang.RuntimeException: Nonzero exit value: 2
  at scala.sys.package$.error(package.scala:27)
  at scala.sys.process.ProcessBuilderImpl$AbstractBuilder.slurp(ProcessBuilderImpl.scala:134)
  at scala.sys.process.ProcessBuilderImpl$AbstractBuilder.$bang$bang(ProcessBuilderImpl.scala:104)
  ... 28 elided
```

### exit code, stdout, stderr를 구분해서 저장하는 방법

`ProcessLogger`를 이용하면 exit code, stdout, stderr를 모두 저장할 수 있다.

```scala
import sys.process._

val stdout = new StringBuilder
val stderr = new StringBuilder

val logger = ProcessLogger(
    (o: String) => stdout.append(o + "\n"),
    (e: String) => stderr.append(e + "\n"))

val exitCode = "ls /usr /foo" ! logger

scala> println(exitCode)
2

scala> println(stdout)
/usr:
X11
X11R6
bin
...

scala> println(stderr)
ls: cannot access '/foo': No such file or directory
```

### 참고 자료

- https://alvinalexander.com/scala/scala-execute-exec-external-system-commands-in-scala/
- https://stackoverflow.com/a/5564667/2930152
