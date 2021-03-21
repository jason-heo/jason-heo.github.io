---
layout: post
title: "sbt assembly 속도 향상하기"
categories: "programming"
---

kubernetes pod 위에서 scala project를 빌드하는데 assembly 속도가 들쑥날쑥이다.

`[info] SHA-1: 3b2963cc8415665f20443ccff64d1f9589e08fa4`라고 나오는 단계에서 시간이 많이 걸린다.

예를 들어 일반 물리 장비에서 2분 걸리는 작업이 pod에서 띄우면 10분 이상 걸리고 간혹 20분이 걸릴 때도 있다. 그리고 아주 간혹 pod이 아니더라도 물리 장비에서도 assembly 속도가 엄청 느려지는 경우가 있다.

구글링 후 아래 내용을 추가했더니 느려지는 현상이 사라졌다.

```
assemblyOption in assembly := (assemblyOption in assembly).value.copy(cacheOutput = false)
```

관련 내용

- https://stackoverflow.com/a/19604430
- https://github.com/sbt/sbt-assembly#caching

`cacheOutput` 기능을 disable하면 모든 소스 코드를 새로 컴파일 한다. enable된 상태에서는 변경된 소스 코드만 컴파일한다. assembly는 컴파일과 관련이 없어보이는데 이 옵션을 끈 것이 어떻게 assembly 속도를 향상시켰는지는 아직 잘 모르겠다.
