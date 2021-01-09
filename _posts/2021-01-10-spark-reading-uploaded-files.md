---
layout: post
title: "spark-submit의 --files로 upload한 파일 읽기"
categories: "programming"
---

`spark-shell`의 `--files` 옵션을 사용하면 local에 존재하는 파일을 Spark Driver와 Executor가 수행 중인 서버에 upload할 수 있다.

`--files`로 upload된 파일은 cluster mode인 경우 Spark on Yarn에서는 기본적으로 classpath에 등록되므로 파일을 어렵지 않게 읽을 수 있다.

하지만, 아래와 같은 경우를 고려하면 upload된 파일을 읽는 게 쉽지 않다는 것을 알 수 있다.

1. deploy-mode를 cluster와 client 둘다 지원할 수 있어야 한다
    - client mode인 경우 `--files`로 upload한 파일은 classpth에 걸리지 않는다
    - 이 부분의 우회 방안은 `spark-submit`의 `--driver-class-path` 설정을 이용하여 local directory를 class path로 등록하는 것이다. 이렇게 되면 Spark code 자체는 cluster, client mode 구분없이 일관된 code를 작성할 수 있다
1. Spark on Kubernetes의 경우 `--files`로 upload된 파일이 classpath에 걸리지 않는다

이런 상황들을 모두 고려하여 완벽히 작동디되는 코드를 작성하긴 번거로운 일이다. Spark에서는 upload된 파일을 읽는 우아한 방법을 제공하는데 [`SparkFiles`](http://spark.apache.org/docs/latest/api/scala/org/apache/spark/SparkFiles$.html)를 이용하면 된다.

- `def get(filename: String): String`
  - Get the absolute path of a file added through `SparkContext.addFile()`
- `def getRootDirectory(): String`
  - Get the root directory that contains files added through `SparkContext.addFile()`

`SparkFiles`를 이용하면 cluster/client를 고려할 필요도 없고, Spark on Yarn인지 Kubernetes인지 고려할 필요도 없다.

참고로 `SparkFiles`는 object라서 그냥 아래처럼만 호출해도 된다.

`org.apache.spark.SparkFiles.getRootDirectory()`

`SparkFiles.getRootDirectory()`의 호출 결과는 다음과 같다.

- cluster mode & Spark on Kubernetes
    - `/var/data/spark-357eb33e-1c17-4ad4-b1e8-6f878b1d8253/spark-e07d7e84-0fa7-410e-b0da-7219c412afa3/userFiles-59084588-f7f6-4ba2-a3a3-9997a780af24`
    - 위 디렉터리는 driver pod에 존재한다
    - 참고로 `/tmp/spark-xxx/`에도 동일 파일이 존재한다
- client mode
    - `/tmp/spark-9f83a8ab-cb8b-42c4-ae42-7eec381dbb70/userFiles-e3a010e3-1ffe-4d6d-8dcc-04e2700417c3`
    - 위 디렉터리는 `spark-submit`을 수행하는 local에 존재한다

참고 자료

- https://stackoverflow.com/q/65353164/2930152
- https://stackoverflow.com/a/41678839/2930152
