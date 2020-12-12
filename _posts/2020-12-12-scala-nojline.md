---
layout: post
title: "scala REPL에서 TAB 문자 입력하기, spark-shell에서 TAB 문자 입력하기"
categories: "programming"
---

scala에서는 REPL을 지원하기 때문에 간단한 테스트를 하기 쉽다. REPL에서는 <kbd>TAB</kbd> key를 이용하여 자동 완성도 할 수 있다. 그런데 가끔 `\t` 문자를 입력해야하는데 <kbd>TAB</kbd> key을 입력하면 자동완성 기능이 작동하기 때문에 `\t` 문자를 입력할 수 없다.

이런 경우 `-Xnojline` 옵션을 주고 `scala`나 `spark-shell`을 실행하면 된다.

```console
$ scala -Xnojline
```

```console
$ spark-shell -Xnojline <그외 spark 옵션들>
```

`-Xnojline` 옵션은 JLine이라는 library를 사용하지 않는 것을 지정하는 옵션이다. [JLine](https://github.com/jline/jline3)은 "JLine is a Java library for handling console input. It is similar in functionality to BSD editline and GNU readline"이라고 한다.
