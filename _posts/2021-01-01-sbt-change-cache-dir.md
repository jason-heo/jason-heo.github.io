---
layout: post
title: "sbt로 빌드 시 cache 디렉터리 변경하기 (ivy, coursier)"
categories: "programming"
---

## 요약

`sbt`를 이용해서 빌드를 하게 되면 `build.sbt`에 걸린 의존성 jar들을 다운로드하게 된다. 이 경로를 cache 디렉터리라고 하자. 경우에 따라서 이 경로를 변경해야할 필요성이 있는데 아래처럼 변경할 수 있다.

```console
$ sbt -Dsbt.ivy.home=/path/to/ivy2 -Dsbt.coursier.home=/path/to/cache
```

혹은 `SBT_OPTS`라는 환경 변수를 만들어두면 `sbt` 명령만 입력해도 된다.

```console
$ export SBT_OPTS="-Dsbt.ivy.home=/path/to/ivy2 -Dsbt.coursier.home=/path/to/cache"
```

## Coursier

`$HOME/.cache/coursier/`라는 디렉터리의 용도가 뭔지 궁금했었는데 알고보니 Coursier라는 툴이 사용하는 cache 디렉터리였다. [Coursier](https://get-coursier.io/docs/overview)는 ivy 같은 의존성 관리 툴이라고 한다. sbt 빌드할 때 ivy가 사용되고 있는데 Coursier가 사용되는 정확한 원인은 모르겠다. 추측하기로는 Java의 ivy로 의존성이 설정된 것은 ivy를 사용하고 Scala에서 Coursier로 의존성이 걸린 프로젝트는 Coursier를 사용하는 것 아닐까 싶다.

참고로 Mac에서 Coursier의 cache 디렉터리는 `$HOME/Library/Caches/Coursier/v1`이다.

Coursier의 cache 경로는 변경하는 방법이 `-Dsbt.coursier.home=/path/to/cache`이다.

[github의 댓글](https://github.com/coursier/coursier/issues/783#issuecomment-537452742)을 보면 `COURSIER_CACHE`라는 환경 변수를 설정해도 된다고 하는데, 나의 개발 황경에서는 이 걸로는 cache 경로가 변경되지 않았다.
