---
layout: post
title: "fabric8 example"
categories: "programming"
---

Java용 Kubernetes Client API는 크게 두가지가 있다.

- [Official Java Client](https://github.com/kubernetes-client/java)
- [Fabric8](https://github.com/fabric8io/kubernetes-client)

두 개에 대한 비교는 [Difference between Fabric8 and Official Kubernetes Java Client](https://itnext.io/difference-between-fabric8-and-official-kubernetes-java-client-3e0a994fd4af)에 잘 정리되어 있으니 관심있는 분은 직접 읽어보기 바란다.

참고로 본인은 위의 글 읽어보지 않고 Fabric8을 사용했다. Apache Spark에서 Fabric8을 사용 중이길래 고민없이 선택을 하였다.

[`kubectl`에 대응되는 API 예제](https://github.com/fabric8io/kubernetes-client#kubectl-java-equivalents)를 제공하기 때문에 API를 참고할 때 편하다.

대부분의 경우는 위 API를 참고 사용법을 알 수 있다.

Custom Resource 같은 경우는 API로 제공이 안 될텐데 이런 경우는 yaml 파일을 생성해서 실행하면 된다.

```java
try (KubernetesClient k8s = new DefaultKubernetesClient()) {
  k8s.load(CreateOrReplaceResourceList.class.getResourceAsStream("/test-resource-list.yaml"))
	.inNamespace("default")
	.createOrReplace();
}
```

([출처](https://github.com/fabric8io/kubernetes-client/blob/6e439a876f3b7973c744aa35bece70aca4700f39/kubernetes-examples/src/main/java/io/fabric8/kubernetes/examples/kubectl/equivalents/CreateOrReplaceResourceList.java#L27-L32))

Custom Resource를 실행할 때마다 매번 yaml 파일 만들기 귀찮으므로 이때는 String을 `InputStream`으로 변경 후 argument로 전달하면 된다.

Pod이나 Service 같은 기본적인 kind는 모두 Builder를 제공한다. 아래의 예는 `JobBuilder`를 이용하여 `Job`을 생성하는 예제이다.

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
