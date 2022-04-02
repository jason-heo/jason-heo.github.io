---
layout: post
title: "spark memoryOverhead 설정에 대한 이해"
categories: "bigdata"
---

### 2022.04.02 추가

[spark memoryOverhead 설정에 대한 이해 (2)](/bigdata/2022/04/02/spark-memoryoverhead.html)가 등록되었습니다.

### 목차

- [Spark 버전에 따른 설정명](#spark-버전에-따른-설정명)
- [memoryOverhead 설정이란?](#memoryoverhead-설정이란)
- [Yarn으로부터 Mmeory를 할당받는 과정](#yarn으로부터-mmeory를-할당받는-과정)
- [executor 실행시 jvm 옵션](#executor-실행시-jvm-옵션)
- ["Consider boosting spark.yarn.executor.memoryOverhead" 메시지의 이해](#consider-boosting-sparkyarnexecutormemoryoverhead-메시지의-이해)
- [`--executor-memory=2g --conf spark.executor.memoryOverhead=1g` VS `--executor-memory=3g`](#--executor-memory2g---conf-sparkexecutormemoryoverhead1g-vs---executor-memory3g)
- [메모리가 부족한 경우에 `--executor-memory`를 늘려야할까? memoryOverhead를 늘려야할까?](#메모리가-부족한-경우에---executor-memory를-늘려야할까-memoryoverhead를-늘려야할까)

### Spark 버전에 따른 설정명

우선 Spark 버전에 따른 설명명부터 알아보자.

- Spark 2.2까지: `spark.yarn.executor.memoryOverhead`
- Spark 2.3부터: `spark.executor.memoryOverhead`

Spark 2.3부터 memoryOverhead 설정명이 변경되었다. (참고로 2.3, 2.4 메뉴얼에는 해당 설정이 누락된 듯 하다)

설정 이름이 변경된 이유에 대해서는 [Stack overflow의 답변](https://stackoverflow.com/q/58813117/2930152)을 보면 Spark을 운영하는 cluster가 yarn이 아닌 kubernetes도 있기 때문에 "yarn에 종속된 듯한 설정명"을 일반적인 설정명으로 변경한 것 같다.

다만 호환성을 위해 Spark 2.3 이상에서도 기존의 `spark.yarn.executor.memoryOverhead` 설정을 사용할 수 있다. ([관련 코드](https://github.com/apache/spark/blob/7766fd13c9e7cb72b97fdfee224d3958fbe882a0/core/src/main/scala/org/apache/spark/SparkConf.scala#L684-L685)]

### memoryOverhead 설정이란?

비교적 설명이 잘 되어 있는 [Spark 2.2 메뉴얼](https://spark.apache.org/docs/2.2.0/running-on-yarn.html#spark-properties)을 보면 아래와 같이 설명되어 있다.

> The amount of off-heap memory (in megabytes) to be allocated per executor. This is memory that accounts for things like VM overheads, interned strings, other native overheads, etc. This tends to grow with the executor size (typically 6-10%).

메뉴얼만 봐서는 무슨 말인지 대략은 이해가 되지만 명확한 이해가 되진 않는다.

java를 실행할 때는 `-Xmx` 설정으로 heap size를 지정한다. 즉, `-Xmx2g`라고 지정하는 경우 jvm에는 2GB의 heap이 할당된다. 하지만 `top`으로 해당 jvm의 메모리 사용량 (정확히는 resident memory, 또는 RSS)을 보면 2GB보다 더 많은 메모리를 사용하는 걸 볼 수 있다.

2GB 이외의 메모리는 off-heap이며 jvm에서 다양한 용도로 사용되는 영역이다.

Spark의 memoryOverhead 설정을 off-heap용 메모리 공간을 임의로 지정할 수 있다.

### Yarn으로부터 Mmeory를 할당받는 과정

우선 `--executor-memory=1g`를 지정했다고 가정하자.

Spark Driver는 yarn에게 executor용 memory를 요청하게 되는데 이때 요청되는 메모리는 (`--executor-memory`에 지정된 메몰 + overhead를 감안한 메모리)가 된다.

'overhead를 감안한 메모리'는 `MIN(executorMemory * 0.1, 384MB)`가 된다.

따라서 `--executor-memory=1g`로 지정한 경우 executor가 yarn으로부터 할당받는 메모리는 `1GB + 384MB`가 된다.

`--executor-memory=5g`가 된다면 이때는 `5GB + 500MB`를 할당받게 된다.

Spark 2.2 기준의 관련된 코드는 다음과 같다.

```scala
private[yarn] val resource = Resource.newInstance(executorMemory + memoryOverhead, executorCores)
```

(출처: [`YarnAllocator.scala`](https://github.com/apache/spark/blob/branch-2.2/resource-managers/yarn/src/main/scala/org/apache/spark/deploy/yarn/YarnAllocator.scala#L140))

`resource` 변수는 이후에 Yarn ResourceManager에게 자원을 요청할 때 사용되는 변수이다.

### executor 실행시 jvm 옵션

executor가 실행될 때 java의 옵션으로 `-Xmx`가 지정되는데 이때는 `--executor-memory`에 설정된 값만 지정된다.

즉, `--executor-memory=2g --conf spark.executor.memoryOverhead=1g`를 지정한다고 하더라도 executor가 실행될 때는 `-Xmx2g`만 사용된다.

이게 관련된 코드는 다음과 같다.

```
javaOpts += "-Xmx" + executorMemoryString
```

(출처: [`ExecutorRunnable`](https://github.com/apache/spark/blob/branch-2.2/resource-managers/yarn/src/main/scala/org/apache/spark/deploy/yarn/ExecutorRunnable.scala#L140))

### "Consider boosting spark.yarn.executor.memoryOverhead" 메시지의 이해

Sprak on yarn을 사용할 때 아래와 같은 메시지를 흔히 볼 수 있다.

```
Reason: Container killed by YARN for exceeding memory limits.
5.5 GB of 5.5 GB physical memory used.
Consider boosting spark.yarn.executor.memoryOverhead.
```

`--executor-memory=2g --conf spark.executor.memoryOverhead=1g` 옵션을 지정한 경우즉 Yarn에게는 "3GB를 사용하겠다고 예약"을 한 뒤에 `-Xmx2g`만 지정을 한다. off-heap 사용량이 1GB를 넘게되는 경우 Yarn ResourceManager는 해당 executor를 kill한다. 예약된 3GB를 넘게 메모리를 사용하기 때문이다.


### `--executor-memory=2g --conf spark.executor.memoryOverhead=1g` VS `--executor-memory=3g`

아래 두 가지 방법 모두 3GB를 사용하는 것처럼 보여지나 off-heap 사용량 관점에서는 다르다.

- 1) `--executor-memory=2g --conf spark.executor.memoryOverhead=1g`
    - on-heap: 2GB
    - off-heap: 1GB
    - yarn의 memory 할당량: 3GB
- 2) `--executor-memory=3g`
    - on-heap: 3GB
    - off-heap: 384MB
    - yarn의 memory 할당량: 3GB + 384MB

### 메모리가 부족한 경우에 `--executor-memory`를 늘려야할까? memoryOverhead를 늘려야할까?

- `--executor-memory`를 늘려야하는 경우
    - GC가 자주 발생하는 경우
    - 이때는 on-heap이 부족하다는 이야기이다
- memoryOverhead를 늘려야하는 경우
    - GC는 적지만, yarn에 의해 executor가 죽는 경우

물론 둘다 늘려야하는 경우도 있겠다.

{% include spark-reco.md %}
