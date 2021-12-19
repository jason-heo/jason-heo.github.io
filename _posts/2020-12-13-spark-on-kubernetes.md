---
layout: post
title: "Spark on Kubernetes 사용법 및 secure HDFS에 접근하기"
categories: "bigdata"
---

테스트에 사용된 Spark 버전: 3.0.1

아래 설명된 내용은 본인의 환경에서만 수행되는 내용일 수 있습니다.

또한 부정확한 내용을 포함할 수 있습니다.

## 목차

- [1. 개요](#1-개요)
- [2. Spark docker image 만들기](#2-spark-docker-image-만들기)
  - [2-1) Spark binary로부터 image 만들기](#2-1-spark-binary로부터-image-만들기)
  - [2-2) Spark source code로부터 image 만들기](#2-2-spark-source-code로부터-image-만들기)
- [3. Spark on Kubernetes에서 spark-shell 실행하기](#3-spark-on-kubernetes에서-spark-shell-실행하기)
  - [3-1) 물리 장비에서 실행하기](#3-1-물리-장비에서-실행하기)
  - [3-2) pod 내부에서 실행하기](#3-2-pod-내부에서-실행하기)
- [4. Spark on Kubernetes에서 secured HDFS에 접근하기](#4-spark-on-kubernetes에서-secured-hdfs에-접근하기)

## 1. 개요

본 내용은 두가지 내용을 담고 있다.

- Spark on Kubernetes 사용하기
    - Kubernetes 환경이 갖추어졌다는 가정하에서, `spark-shell`를 Kubernetes에 수행하는 옵션을 설명한다
    - `spark-submit`도 사용법은 동일하다
- Spark on Kubernetes에서 secure HDFS에 접근하는 방법
    - kerberos가 설정된 HDFS에 접근하는 옵션을 설명한다
    - secure HDFS를 설정하는 방법에 대해서는 설명하지 않는다

약 2년 전부터 Spark on Kubernetes가 화두이다. CDH 같은 걸 사용하면 Spark on Yarn은 쉽게 사용할 수 있지만 Spark on Kubernetes에 대한 자료는 찾기가 어려웠다.

올해 여름에 Spark on Yarn에서 secure HDFS의 파일을 읽는 환경을 만드느라 잠도 잘 못자고 고생을 했는데, Spark on Kubernetes에서도 고생을 좀 했다.

고생했던 내용을 정리해본다.

## 2. Spark docker image 만들기

### 2-1) Spark binary로부터 image 만들기

Spark docker image를 만드는 가장 쉬운 방법은 spark binary를 이용하는 방법이다.

Spark이 이미 build되어 있으므로 jar만 복사하면 되므로 image 만드는 속도가 빠르다.

아래 예는 2020.12.13 현재 가장 최신 버전인 Spark 3.0.1을 docker image로 만드는 예이다.

base image를 변경하는 등 기능을 추가하려면 `kubernetes/dockerfiles/spark/Dockerfile` 파일을 수정하면 된다.

```console
$ wget https://downloads.apache.org/spark/spark-3.0.1/spark-3.0.1-bin-hadoop3.2.tgz

$ tar xvfz spark-3.0.1-bin-hadoop3.2.tgz

$ cd spark-3.0.1-bin-hadoop3.2/

$ docker build --tag spark:3.0.1 -f kubernetes/dockerfiles/spark/Dockerfile .
```

이제 각자의 docker registry로 push 후에 Spark on Kubernetes를 사용하면 된다.

### 2-2) Spark source code로부터 image 만들기

아직 release되지 않은 버전을 사용하려면 소스 코드로부터 build 후 docker image를 만들면 된다.

아래 2020.12.13 현재 release되지 않은 Spark 3.1을 직접 build하는 예이다.

```console
$ git clone https://github.com/apache/spark.git

$ cd spark/

$ git checkout branch-3.1

$ export MAVEN_OPTS="-Xmx4g -XX:ReservedCodeCacheSize=1g"

$ ./build/mvn -T 8 -DskipTests -Pkubernetes clean package

$ ./dev/make-distribution.sh --name custom-spark --pip --tgz -Phadoop-3.2 -Phive -Phive-thriftserver -Pmesos -Pyarn -Pkubernetes
```

## 3. Spark on Kubernetes에서 spark-shell 실행하기

참고 문서: https://spark.apache.org/docs/3.0.1/running-on-kubernetes.html

### 3-1) 물리 장비에서 실행하기

Kubernetes에 spark job을 실행하는 건 크게 어렵지 않았다.

```console
$ spark-shell \
    --master k8s://https://<k8s-api-server>:6443 \
    --deploy-mode=client \
    --name=<app-name> \
    --conf spark.executor.instances=5 \
    --conf spark.kubernetes.container.image=<registry server>/<path>/spark:3.0.1 \
    --conf spark.kubernetes.namespace=<namespace>
```
### 3-2) pod 내부에서 실행하기

어려웠던 점

- default API 서버가 뭔지 몰랐다 -> 회사 분이 알려주셔서 해결
- 처음엔 executor에서 driver로 연결이 안 되었다 -> 아래 참고:w

pod 내부에서 수행할 때 변경되는 옵션

- `--master`에 지정하는 API 서버가 default로 변경되었다
- `spark.driver.host`를 ip 주소로 지정한다
    - 사유는 executor에서 driver에 접속할 때 pod name으로 접속이 불가능하기 때문이다
    - service나 headless가 설정된 pod이라면 굳이 ip로 지정할 필요는 없다

실제 옵션은 아래와 같다.

```console
$ spark-shell \
    --master=k8s://https://kubernetes.default \
    --deploy-mode=client \
    --name=<app-name> \
    --conf spark.driver.host=$(hostname -i) \
    --conf spark.executor.instances=5 \
    --conf spark.kubernetes.container.image=<registry server>/<path>/spark:3.0.1 \
    --conf spark.kubernetes.namespace=<namespace>
```

{% include adsense-content.md %}

## 4. Spark on Kubernetes에서 secured HDFS에 접근하기

참고 문서: https://spark.apache.org/docs/3.0.1/security.html#secure-interaction-with-kubernetes

Spark on Yarn에서도 secure HDFS를 읽느라 고생을 많이했는데 Spark on Kubernetes에서도 고생을 했다.

우선 Spark docker image에 아래 파일과 설정을 추가하는 것이 중요했다. 앞서 말했듯이 본인에게만 중요한 것인지 다른 사람들의 환경에서도 중요한 것인지는 확실치 한다.

- `/etc/krb5.conf` 파일 추가
    - Spark on Yarn에서는 `krb5.conf` 파일이 `/etc/krb5.conf`에 있을 필요가 없었다.
        - /`etc/krb5.conf`에 없더라도 `KRB5_CONFIG` 환경 변수를 이용하여 `kinit` 명령을 수행하는데 문제가 없었고
        - spark submit 시에는 `SPARK_SUBMIT_OPTS="-Djava.security.krb5.conf=<path-to-krb5.conf>"`로 지정이 가능했다
    - 그런데 Spark on Kubernetes에서는 이 설정이 잘 안 되었다
        - 내가 설정을 제대로 못한 것인지 원래 Spark on Kubernetes가 이렇게 작동하는 것인지 알기가 어려웠다
        - 어차피 Spark docker image를 우리가 맘대로 설정할 수 있으므로 `/etc/krb5.conf` 파일을 만들었다
        - 이렇게 되면 executor pod에도 내가 입맛대로 수정한 `/etc/krb5.conf`를 만들 수 있다
- Hadoop 설정을 Spark on Yarn에서 사용하던 config file들을 등록
    - Spark on Yarn에서 secure HDFS에 접근이 가능하던 Hadoop 설정들을 그대로 복사해봤다
    - Spark on yarn에는 spark job을 제출하는 서버에서만 `$HADOOP_CONF_DIR`의 설정 내용이 secure HDFS에 접근할 수 있으면 되었는데, Spark on Kubernetes에는 잘 안 되었다

Spark on Yarn과는 작동 방법이 다른 것 같은데 내가 설정을 제대로 못한 것인지 원래 Spark의 작동 방법이 이런 것인지 모르겠다.

고생을 많이했는데 결론은 executor용 pod에도 `/etc/krb5.conf` 파일과 Hadoop 설정이 추가했더니 잘 돌아간다는 이야기이다. 아마도 일부 설정은 spark job을 체출하는 pod에만 존재하면 될 것 같다. 정확한 방식을 이해하기 위해 어려 조합으로 테스트를 해봤지만 알 수가 없어서 모든 pod에 설정들을 추가하는 선에서 마무리하였다.

이제 다음과 같은 옵션으로 `spark-shell`을 실행하면 secure HDFS에 접근하여 파일을 읽을 수 있다.

```console
$ spark-shell \
    --master=k8s://https://kubernetes.default \
    --deploy-mode=client \
    --name=<app-name> \
    --conf spark.driver.host=$(hostname -i) \
    --conf spark.executor.instances=5 \
    --conf spark.kubernetes.container.image=<registry server>/<path>/spark:3.0.1 \
    --conf spark.kubernetes.namespace=<namespace> \
    --conf spark.kerberos.access.hadoopFileSystems=hdfs://<nameservice> \
    --conf spark.kerberos.principal=<princial> \
    --conf spark.kerberos.keytab=<path/to/keytab>
```

미리 `kinit`을 한 경우에는 다음의 두 개 설정을 생략할 수 있다. 이미 인증된 token을 그대로 사용하기 때문이다.

- `--conf spark.kerberos.principal=<princial>`
- `--conf spark.kerberos.keytab=<path/to/keytab>`

제일 어려웠던 설정은 `spark.kerberos.access.hadoopFileSystems`이다. 위의 참고 문서에서 `spark-submit` 옵션을 설명할 때 등장하지 않았기 때문인데 같은 문서의 약간 위쪽에 https://spark.apache.org/docs/3.0.1/security.html#kerberos 에서 설명이 되어있다.

> If an application needs to interact with other secure Hadoop filesystems, their URIs need to be explicitly provided to Spark at launch time. This is done by listing them in the `spark.kerberos.access.hadoopFileSystems` property

이 글을 읽는 분들은 본인의 설정과 다르기 때문에 이 설정을 필요없을 수도 있다.

{% include spark-reco.md %}
