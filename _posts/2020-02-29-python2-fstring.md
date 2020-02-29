---
layout: post
title: "Python2에서 f-strings 사용하는 방법"
categories: "python"
---

{% include python.md %}

현업에서 Spark을 주로 사용하기 때문에 개발 언어도 자연스레 Scala을 많이 쓰고 있다. Scala 언어의 장점이 여러 개가 있는데, string interpolation도 큰 장점이라 생각한다.

Python에서는 3.6부터 f-strings이라는 걸 지원하고 있다. 그 이하 버전에서는 아래와 같이하면 f-strings을 사용할 수 있다.

f-strings 설치 방법

```bash
$ pip install future-fstrings
```

f-strings 사용 방법

```bash
$ cat fstring.py
#-*- coding: future_fstrings -*-

name = 'Jason'

print f"my name is '{name}'"

$ python2 fstring.py
my name is 'Jason'
```

f-strings을 사용한 코드를 기존 형식의 코드로 변환할 수도 있다.

```bash
$ future-fstrings-show fstring.py
#-*- coding: future_fstrings -*-

name = 'Jason'

print "my name is '{}'".format((name))
```

이 기능을 활용하면 개발 서버에서만 f-strings을 설치하고, 배포 전에 파일을 변환해서 배포하면 된다고 한다. pip 패키지를 맘대로 설치할 수 없는 시스템에서 사용하면 좋을 듯.
