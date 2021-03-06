---
layout: post
title: "Spark 3.1 on Kubernetes - local storage용 pvc를 onDemand로 mount하기"
categories: "bigdata"
---

## 1. Local Storage를 onDemand로 mount하기

Spark 3.1부터는 Kubernetes를 사용할 때 local storage용 pvc를 onDemand로 mount할 수 있다.

이를 위해서는 Spark 실행 시 다음과 같은 옵션을 지정하면 된다.

```
spark.kubernetes.executor.volumes.persistentVolumeClaim.spark-local-dir-1.options.claimName=OnDemand
spark.kubernetes.executor.volumes.persistentVolumeClaim.spark-local-dir-1.options.storageClass=gp
spark.kubernetes.executor.volumes.persistentVolumeClaim.spark-local-dir-1.options.sizeLimit=500Gi
spark.kubernetes.executor.volumes.persistentVolumeClaim.spark-local-dir-1.mount.path=/data
spark.kubernetes.executor.volumes.persistentVolumeClaim.spark-local-dir-1.mount.readOnly=false
```

`spark-local-dir-1` 부분이 Volume Name인데 `spark-local-dir`로 시작하는 Volume Name의 경우 Spark에서 spill 등 내부적인 용도로 활용하게 된다.

`claimName=OnDemand`의 의미는 pvc를 onDemand로 생성하겠다는 것이다. 즉,  executor가 생성될 때 pvc를 생성하고, executor가 종료될 때 pvc를 반환하게 된다.

사실 위의 옵션은 Spark on Kubernetes에서 pvc를 mount할 수 있는 일반적인 용도이다. 따라서 위 옵션을 잘 조절하면 pvc를 mount한 뒤에 business logic에서도 pvc에 마음대로 접근할 수 있다.

Volume Name에는 이미 존재하는 pvc를 지정할 수도 있다. 이 경우 onDemand로 생성/삭제하는 것이 아니라 말그대로 영구적인 volume을 사용할 수 있다.

참고 문서: [Running on Kubernetes - Local Storage](https://spark.apache.org/docs/latest/running-on-kubernetes.html#local-storage)

Apache Spark 한국인 커미터 현동중님이 만든 기능으로서 관련된 PR은 [여기](https://github.com/apache/spark/pull/29846)에서 볼 수 있다.

## 2. 위 기능이 필요한 이유

Spark 문서 중 [Local Storage](https://spark.apache.org/docs/latest/running-on-kubernetes.html#local-storage)를 보면 다음과 같은 내용이 있다.

> Spark supports using volumes to spill data during shuffles and other operations.

즉, memory에 저장된 data가 disk로 spill될 때 저장되는 공간으로 pvc를 사용할 수 있다.

일반적으로 kubernetes를 사용할 때 할당받는 ephemeral storage는 size가 크지 않다. 이런 적은 size에 대량의 data가 spill되는 경우 공간이 금방 차게 되고 spark task가 실패된다.

ephemeral storage를 크게 할당받을 수 있다면 아무 문제가 없지만 managed kubernetes service를 사용하는 경우 제한이 있기 때문에 대량의 shuffle이 문제가 된다.

이를 해결하는 것이 onDemand로 pvc를 mount하는 것이다.

Spark 3.0에도 비슷한 기능을 지원하긴 했지만 pvc 1개를 n개의 executor에서 mount하는 방식을 지원했다.

## 3. spill이란? shuffle이란?

위에서 설명한 내용을 정확히 이해하기 위해선 spill과 shuffle에 대해서 알고 있어야 한다.

우선 shuffle부터 알아보자.

Spark에는 wide transformation이라는 것이 있는데, 예를 들어 groupBy 연산을 할 때는 여러 곳에 분산되어 있는 동일한 값의 key들을 특정 executor로 모으는 작업이 필요하다. 그래야만 group별로 COUNT나 SUM을 할 수 있다. 이 과정이 shuffle이다. shuffle은 Spark이 아니더라도 Big Data에서 매우 매우 중요한 개념이다.

shuffle 시에는 대량의 데이터를 executor 간에 서로 주고 받게 되는데 이런 데이터는 최초에는 memory에 저장된다. 이후 memory가 꽉 차는 경우 disk에 저장하는데 이를 spill이라고 한다.

spill의 사전적 의미는 '유출', '흘리다', '엎질러지다'라는 의미가 있다.

spill은 memory와 disk를 '그릇'으로 비교하면 이해하기 쉽다. 아래 그림을 보자.

executor에는 1GB만 저장할 수 있는데 shuffle로 전달받은 데이터가 1GB 이상이라면 Disk에 저장해야한다. 이걸 spill이라고 하는 것이다.

<img src="https://i.imgur.com/P4tFdvh.png" title="source: imgur.com" />

(그림 출처: 본인)
