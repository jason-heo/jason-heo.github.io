---
layout: post
title: "Spark 3.0에서 elasticsearch hadoop 사용하기"
categories: "programming"
---

## 1. 개요

Spark 3.0을 적용하면서 놀란 점이 하나 있는데, elasticsearch hadoop (이하 es-hadoop)이 Spark 3.0을 지원하지 않는다는 점이다.

또한 Scala 2.12를 지원하지 않으므로 Spark 2.4를 Scala 2.12로 사용하는 경우 es-hadoop을 사용할 수 없다. Spark 2.4에서는 거의 대부분 환경에서 Scala 2.11을 사용할 것이라서 본 글에서는 Spark 3.0과 es-hadoop을 사용하는데 초점을 두고 있다.

Spark 3.0이 정식으로 release된지 벌써 6개월이 지났고 preview까지 포함하면 1년이 넘었는데 es-hadoop에서 2021년 1월 현재까지도 Spark 3.0을 지원하지 않는다는데 놀랄 노자일 수밖에 없다. 내부적인 사정이 있을 것 같은데 어떤 이유인지 궁금하다.

관련된 PR을 찾아보면 es-hadoop 개발자가 만든 [PR](https://github.com/elastic/elasticsearch-hadoop/pull/1551/files)이 나오고 merge는 되어 있지만 Spark 2.4 & Scala 2.12 기준이다. (이것도 아직 maven까지는 올라가지 않았다)

## 2. Spark 3.0에서 es-hadoop 사용하기

### 2-1) 연동에 성공한 버전

검색을 좀 더 해보면 외부 개발자가 만든 PR이 보인다. upstream에 merge되진 않았지만 다행히 직접 build를 해서 사용할 수 있다.

- PR: https://github.com/elastic/elasticsearch-hadoop/pull/1495
- Repository: https://github.com/avnerl/elasticsearch-hadoop/tree/6.5

아래 환경에서 사용해봤는데 잘 돌아간다.

- Spark 3.0.1
- Hadoop 3.2
- Elasticsearch 6.7

한 가지 아쉬운 점은 Elasticsearch 7.x를 지원하지 않는다는 점이다. ES 7.x를 `es.nodes`에 설정하고 수행해보면 "지원하지 않는 버전"이라는 에러가 나온다. 어차피 Rest API로 통신을 할텐데 버전 검사 정도는 loose하게 풀어줬으면 좋았을 듯 하다.

### 2-2) build 방법

build 방법은 간단하다. JAVA 1.8만 설치되어 있으면 되고 별도로 설정해야할 것도 없다. (아마 `JAVA_HOME` 정도는 설정되어 있어야할 듯)

```console
$ ./gradlew -Pdistro=hadoopYarn3 -x test
```

이후 `spark/sql-20/build/libs/` 디렉터리를 보면 `elasticsearch-spark-30_2.12-6.5.5-SNAPSHOT.jar` 이런 파일이 생성된 것을 볼 수 있다.

### 2-3) 사용법

`spark-shell`이나 `spark-submit`의 `--jars` 옵션을 이용하여 build된 jar 파일을 지정하면 된다.

주의할 점이 있는데 `spark-submit` 사용 시에 fat jar에 포함되면 안된다는 점이다. sbt 사용시 `unmanagedBase` 디렉터리에 jar를 넣으면 fat jar에 포함이 되고 compile time 의존성에도 포함되지만 이렇게 하면 Spark이 오작동을 한다. (본인 환경에서만 오류가 발생했을 수도 있다)

## 3. 연동 실패했지만 ES 7.x를 지원하는 버전

같은 개발자가 ES 7.x를 위해 만든 버전도 있다.

- PR: https://github.com/elastic/elasticsearch-hadoop/pull/1498
- Repository: https://github.com/avnerl/elasticsearch-hadoop

그런데 아쉽게도 build가 잘 안된다. 위 PR의 댓글을 읽어보면 build에 성공하려면 소스 코드를 좀 수정해야할 듯 하다. 다른 개발자가 build 성공한 버전을 만들어서 링크 건 게 있지만 이것도 제대로 build가 안되었다.
