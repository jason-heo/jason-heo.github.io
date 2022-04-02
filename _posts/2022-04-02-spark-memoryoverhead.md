---
layout: post
title: "spark memoryOverhead 설정에 대한 이해 (2)"
categories: "bigdata"
---

2020.10.24에 작성한 [spark memoryOverhead 설정에 대한 이해](/bigdata/2020/10/24/understanding-spark-memoryoverhead-conf.html)에 이은 두 번째 글입니다.

memoryOverhead 설정에 대해 잘 모른다면 위 글을 먼저 읽길 추천합니다.

### 개요

Spark 프로그램이 있는데, 하는 일은 parquet file을 읽어서 집계를 한 뒤 다시 parquet file에 저장하는 프로그램이다.

Spark 2.2에서는 별 문제없이 잘 작동하는 코드인데 Spark 3.1으로 upgrade한 뒤에 memory 부족 현상이 발생했다.

이를 어떻게 해결했는지 간단히 정리한다

### 현상: executor용 container가 OOM으로 죽다

본인은 Spark on k8s를 사용 중이므로 executor가 pod에서 수행 중인데 k8s OOM killer에 의해 executor pod이 계속 kill되었다.

Spark on yarn에서 발생하는 `"Container killed by YARN for exceeding memory limits"`와 동일한 현상이다.

`jstat`으로 heap usage 및 gc 상태를 보면 heap이 부족해보이지 않았다. 따라서 memoryOverhead 설정을 늘리면 container가 kill되는 현상은 없앨 수 있다.

([이전 글]((/bigdata/2020/10/24/understanding-spark-memoryoverhead-conf.html))에도 적었지만 메모리가 부족할 때 executor memory를 늘려야할지 혹은 memoryOverhead를 늘려야할지를 잘 결정해야한다)

그런데 memoryOverhead를 Spark 2.2보다 더 많이 줘야한다는 점이었다. 왜 이런 현상이 발생했을까?

### 문제: 3rd party library도 off-heap을 사용한다

메모리를 많이 사용하는 이슈에 대해 처음에는 Spark 3.1의 버그라 생각했었다. 그런데 아무리 검색해도 관련된 내용이 나오지 않았다. 그러던 중 이용환님이 작성한 [Spark Internal Part 2. Spark의 메모리 관리(1)](https://medium.com/@leeyh0216/spark-internal-part-2-spark%EC%9D%98-%EB%A9%94%EB%AA%A8%EB%A6%AC-%EA%B4%80%EB%A6%AC-1-c18e39af942e)를 봤는데 다음과 같은 내용이 있었다.

> 여기까지는 좋은데, Third Party Library에서 사용하는 Off-Heap 공간이 문제다. Netty, Parquet 등의 Third Party Library에서도 성능 향상을 위해 Unsafe를 사용하고 있는데, 이러한 Library들에서 사용하는 메모리 공간에 대해서는 Spark에서 관여하지 않는 듯 하다.

즉, Spark 의 버그가 아니더라도 parquet 같은 3rd party library의 버그에 의해 memory를 많이 사용할 수 있다.

### parquet library에 memory 관련 오류가 있다

정확히는 java parquet-mr library인데 편의상 parquet library라고 하자.

spark 버전별 사용하는 parquet의 버전은 다음과 같다.

- Spark 2.2.x: parquet 1.5.0
- Spark 3.1.2: parquet 1.10
- Spark 3.2.0: parquet 1.12

parquet의 change log를 읽는 중에 [PARQUET-1485 Snappy Decompressor/Compressor may cause direct memory leak](https://issues.apache.org/jira/browse/PARQUET-1485)

PARQUET-1485는 parquet 1.8.1에서 발생을 했고 parquet 1.11.0에서 수정되었다.

즉, Spark 2.2에서는 memory 문제없다가 Spark 3.1에서는 memory 오류가 발생했다가 Spark 3.2에서 수정된 것을 볼 수 있다

parquet library의 [change log](https://github.com/apache/parquet-mr/blob/master/CHANGES.md)에서 `memory`나 `leak`으로 검색해보면 의외로 관련된 버그가 꽤 있다는데 놀랄 것이다.

### 마무리

memory를 많이 사용하던 문제는 Spark 3.2로 upgrade 후 해결되었다. (Spark on k8s이 성공적으로 안착되기 까지 어려움도 많았지만, 적용 이후로 많은 장점이 있는데 Spark version을 마음대로 올리기 쉽다는 점이다)

Spark memory management 방식을 이해하는 것도 어려운 일인데 memory 문제 발생 시 Spark 외에 3rd party library까지 확인을 한다. (이런 challenging한 문제를 해결하는 것은 언제나 즐겁다)

혹시 비슷한 현상을 겪는 분들을 위해서 참고로 남기자면 parquet를 read할 때가 아닌 write할 때 memory 문제가 발생했다.

{% include spark-reco.md %}
