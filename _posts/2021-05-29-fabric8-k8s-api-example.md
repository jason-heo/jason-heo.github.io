---
layout: post
title: "fabric8 kubernetes-client API 사용법 예제"
categories: "programming"
---

Fabric8 Kubernetes Client API를 사용하면 k8s app을 쉽게 작성할 수 있다. 본 문서에서는 Fabric8 문서에 나와있지 않은 기능 위주로 사용법을 알아볼 예정이다.

사용된 언어는 scala이며 Fabric8 4.x version을 사용했다. 5.x에서는 일부 함수의 return type이 변경되었다.

여기저기 흩어진 예제를 모은 것이다보니 Java와 Scala code가 섞여 있다.

## 1. 들어가며

Java용 Kubernetes Client API는 크게 두가지가 있다.

- [Official Java Client](https://github.com/kubernetes-client/java)
- [Fabric8 kubernetes-client](https://github.com/fabric8io/kubernetes-client)

위 두개 API에 대한 비교는 [Difference between Fabric8 and Official Kubernetes Java Client](https://itnext.io/difference-between-fabric8-and-official-kubernetes-java-client-3e0a994fd4af)에 잘 정리되어 있으니 관심있는 분은 직접 읽어보기 바란다.

참고로 본인은 위의 글 읽어보지 않고 Fabric8을 사용했다. 이유는 Apache Spark에서 Fabric8을 사용 중이었기 때문이다. 어떤 걸 선택할지 고민하는 사이 시간만 흐를 것 같아서 Spark이 선택한 걸 나도 선택했고 결과는 대만족이다.

Fabric8를 사용하다는 중에 비교 테스트를 위해 Official Java Client를 잠시 사용해봤었는데, Official Java Client는 함수에 argument 개수가 많고, argument가 `null`인 경우도 많아서 코드 가독성이 많이 떨어졌다.

반면 Fabric8는 Builder Pattern을 사용하기 때문에 DSL 작성하듯이 함수를 사용할 수 있어서 사용이 매우 편하다.

참고로 fabric8의 발음은 fabricate라고 한다. 유튜브에서 발음을 들어보면 '페브릭에이트'라기보단 '페브릭케이트'가 맞는 듯 하다.

## 2. `kubectl` 명령에 대응되는 API 예제

Fabric8 웹 사이트에서 [`kubectl`에 대응되는 API 예제](https://github.com/fabric8io/kubernetes-client#kubectl-java-equivalents)들를 제공하기 때문에 API를 참고할 때 편하다.

대부분의 경우는 위 API를 참고 사용법을 알 수 있다.

예를 들어 `kubectl get pods`에 대응되는 코드는 다음과 같은데, 이런 예제들을 위 리크에서 볼 수 있다.

```java
public static void main(String[] args) {
    try (KubernetesClient k8s = new DefaultKubernetesClient()) {
        // Print names of all pods in specified namespace
        k8s.pods().inNamespace("default").list()
          .getItems()
          .stream()
          .map(Pod::getMetadata)
          .map(ObjectMeta::getName)
          .forEach(logger::info);
    }
}
```

## 3. 기타 예제들

### 3-1) `kind: Job` 생성하기

```java
JobBuilder()
  .withApiVersion("batch/v1")
  .withNewMetadata()
  .withName("job1")
  .withUid("3Dc4c8746c-94fd-47a7-ac01-11047c0323b4")
  .withLabels(Collections.singletonMap("label1", "maximum-length-of-63-characters"))
  .withAnnotations(Collections.singletonMap("annotation1", "some-very-long-annotation"))
  .endMetadata()
  .withNewSpec()
  .withNewTemplate()
  .withNewSpec()
  .addNewContainer()
  .withName("pi")
  .withImage("perl")
  .withArgs("perl", "-Mbignum=bpi", "-wle", "print bpi(2000)")
  .endContainer()
  .withRestartPolicy("Never")
  .endSpec()
  .endTemplate()
  .endSpec();
```

(출처: [`JobTest.java`](https://github.com/fabric8io/kubernetes-client/blob/6e439a876f3b7973c744aa35bece70aca4700f39/kubernetes-tests/src/test/java/io/fabric8/kubernetes/client/mock/JobTest.java#L339-L358))

위의 Test 디렉터리를 보면 각종 API 사용법을 볼 수 있어서 많은 도움이 된다.

### 3-2) running 중인 pod에 label 추가하기

(참고: Scala code임)

```scala
  val k8sClient: DefaultKubernetesClient = new DefaultKubernetesClient

  // pod 찾기
  val podResource: PodResource[Pod, DoneablePod] = k8sClient
  .pods
  .inNamespace(k8sClient.getNamespace)
  .withName(podName)

  // pod에 label 추가하기
  podResource
     .edit()
     .editMetadata()
       .addToLabels("key", "value")
     .endMetadata()
     .done
```

### 3-3) `kind: Service` 생성하기

(참고: Scala code임)

```scala
val driverService = new ServiceBuilder()
  .withNewMetadata()
    .withName(resolvedServiceName)
    .addToAnnotations(kubernetesConf.serviceAnnotations.asJava)
    .addToLabels(SPARK_APP_ID_LABEL, kubernetesConf.appId)
    .endMetadata()
  .withNewSpec()
    .withClusterIP("None")
    .withSelector(kubernetesConf.labels.asJava)
    .addNewPort()
      .withName(DRIVER_PORT_NAME)
      .withPort(driverPort)
      .withNewTargetPort(driverPort)
      .endPort()
    .addNewPort()
      .withName(BLOCK_MANAGER_PORT_NAME)
      .withPort(driverBlockManagerPort)
      .withNewTargetPort(driverBlockManagerPort)
      .endPort()
    .addNewPort()
      .withName(UI_PORT_NAME)
      .withPort(driverUIPort)
      .withNewTargetPort(driverUIPort)
      .endPort()
    .endSpec()
  .build()
```

(출처: [Apache Spark의 driver service 생성 코드](https://github.com/apache/spark/blob/00f06dd267c37db09951b40315f8ea3afbb3aaae/resource-managers/kubernetes/core/src/main/scala/org/apache/spark/deploy/k8s/features/DriverServiceFeatureStep.scala#L68-L97))

### 3-4) `OwnerReferenceBuilder` 사용법

(참고: Scala code)

```scala
  private def addDriverOwnerReference(driverPod: Pod, resources: Seq[HasMetadata]): Unit = {
    val driverPodOwnerReference = new OwnerReferenceBuilder()
      .withName(driverPod.getMetadata.getName)
      .withApiVersion(driverPod.getApiVersion)
      .withUid(driverPod.getMetadata.getUid)
      .withKind(driverPod.getKind)
      .withController(true)
      .build()
    resources.foreach { resource =>
      val originalMetadata = resource.getMetadata
      originalMetadata.setOwnerReferences(Collections.singletonList(driverPodOwnerReference))
    }
  }
```

(출처: [Apache Spark에서 driver pod 생성 시 ownerReference 설정하기](https://github.com/apache/spark/blob/d99ff20cb00e204a9781812cb3fe3465b4f3a20e/resource-managers/kubernetes/core/src/main/scala/org/apache/spark/deploy/k8s/submit/KubernetesClientApplication.scala#L163-L175))

### 3.5 실행 중인 pod에 ownerReference 연동하기

```scala
  val k8sClient: DefaultKubernetesClient = new DefaultKubernetesClient

  // pod 찾기
  val podResource: PodResource[Pod, DoneablePod] = k8sClient
  .pods
  .inNamespace(k8sClient.getNamespace)
  .withName(podName)

  // pod에 ownerReference 설정하기
  PodResource
    .edit()
      .editMetadata()
        .addNewOwnerReference()
          .withName(owner.getMetadata.getName)
          .withApiVersion(owner.getApiVersion)
          .withUid(owner.getMetadata.getUid)
          .withKind(owner.getKind)
          .withBlockOwnerDeletion(true)
          .withController(true)
        .endOwnerReference()
      .endMetadata()
    .done
```
