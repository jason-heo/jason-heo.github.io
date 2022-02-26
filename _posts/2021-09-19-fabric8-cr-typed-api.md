---
layout: post
title: "fabric8 Custom Resource Typed API 사용법"
categories: "programming"
---

### 들어가며

fabric8 kubernetes api를 사용하면 Java 혹은 Scala에서 k8s 관련 로직은 쉽게 구현할 수 있다. k8s에서 제공하는 Java 공식 API도 존재하지만 이것보단 fabric8의 API가 훨씬 사용하기 편하다. Spark on k8s에서 사용하는 API도 fabric8이다.

문서화도 상당히 잘 되어 있어서 [Cheat Sheet](https://github.com/fabric8io/kubernetes-client/blob/master/doc/CHEATSHEET.md)이라던가 [Kubectl Java equivalents](https://github.com/fabric8io/kubernetes-client#kubectl-java-equivalents) 문서만 보면 웬만한 것들은 쉽게 구현 가능하다.

그런데 한 가지 아쉬운 점은 Custom Resource 관련 API 사용법 예제가 부족하다는 점이다.

정확하게는 Custom Resource 관련하여 다음과 같은 두 가지 API가 존재한다.

1. Typed API
    - strong type을 사용할 수 있다
    - 일반적인 fabric8 API와 동일하게 사용 가능하다
1. Typeless API
    - Custom Resource에 대한 결과를 Map 형태로 return한다
    - watcher를 이용하는 경우 json string으로 접근할 수 있다

Typeless API는 Cheat Sheet를 보면 비교적 쉽게 구현 가능하고 본 글에서는 Typed API 사용법에 대해 알아보자

### 예제용 Custom Resource Definition - Spark Operator

본 글에서는 `kind: SparkApplication`인 CRD를 이용할 것이다.

`kubectl get sparkapp <name> -o json`을 하면 대략 다음과 같은 결과가 출력된다.

```json
{
  "apiVersion": "sparkoperator.k8s.io/v1beta2",
  "kind": "SparkApplication",
  "metadata": {
    "creationTimestamp": "2021-09-19T09:42:27Z",
    "generation": 1,
    "labels": {
      ...
    },
    "name": ...,
    ...
  },
  "spec": {
    "arguments": [...],
    "driver": {
      ...
      "memory": "2g",
      "memoryOverhead": "2g",
      ...
    },
    "executor": {
      "cores": 1,
      "env": [
        {
          ...
        }
      ],
      "instances": 2,
      ...
    },
    ...
  },
  "status": {
    "applicationState": {
      "state": "RUNNING"
    },
    ...
    "executionAttempts": 1,
    ...
  }
}
```

본 글에서는 custom resource를 list한 뒤에 각 Typed API를 이용해서 위 결과를 class instance로 변환해볼 것이다.

### fabric8 Typed API 사용법

fabric8의 Cheat Sheet 중 [Resource Typed API](https://github.com/fabric8io/kubernetes-client/blob/master/doc/CHEATSHEET.md#resource-typed-api)를 보면 사용법이 다음과 같이 간단하게 설명되어 있다.

```java
package io.fabric8.kubernetes.client.mock.crd;

import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Version;

@Version("v1")
@Group("stable.example.com")
public class CronTab extends CustomResource<CronTabSpec, CronTabStatus> implements Namespaced {
}

MixedOperation<CronTab, KubernetesResourceList<CronTab>, Resource<CronTab>> cronTabClient = client.resources(CronTab.class);


CronTabList cronTabList = cronTabClient.inNamespace("default").list();
```

Cheat Sheet에서는 `kind: CronTab`에 대한 예제인데 우선 `CronTabSpec`과 `CronTabStatus`를 만들어야하는 것다.

그런데 이에 대한 예제가 없다보니 여기에서 막혀 버린다.

우선 위에서 봤던 json을 다시 살펴보자. 1 depth의 내용만 발췌하면 다음과 같이 간단한 구조인 것을 알 수 있다.

```json
{
  "apiVersion": "sparkoperator.k8s.io/v1beta2",
  "kind": "SparkApplication",
  "metadata": {...},
  "spec: {...},
  "status": {...}
}
```

그렇다! 우리는 `spec`과 `status`에 대한 class만 정의하면 된다!

`metadata` 부분은 k8s의 모든 resource에 대해 동일한 필드를 갖는 것 같다. 그리고 metadata 하위 필들은 fabric8의 `getMetadata()` 함수를 이용하여 접근할 수 있다. 본인이 typeless API 대신 typed API를 사용하게 된 것도 Map 대신 기존 fabric8 API 사용 경험을 그대로 살릴 수 있기 때문이었다. (물론 compile time 에러 검사 등 strong type의 장점 포함)

다들 알다시피 k8s API는 HTTP로 통신을 한다. 즉, String으로 전달된 정보를 class 변수로 생성해야하는데 fabric8에서는 jackson을 사용한다. 따라서 Spec과 Status class를 정의할 때 jackson annotation을 그대로 사용할 수 있다.

Spark Operator에서 제공하는 Spec가 Status에는 매우 많은 필드가 존재한다. 이 중에서 각자 원하는 필드들만 class에 정의하면 된다. json의 모든 필드를 class에 정의하지 않으므로 jackson의 `JsonIgnoreProperties` annotation을 사용하면 된다

### Spark Operator용 Spec, Status class 예

다음과 같이 정의하면 된다.

setter/setter를 이용해도 되도, 귀찮으면 필드를 public으로 생성해도 된다.

```java
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
class DriverSpec {
    private String memory;
    private String memoryOverhead;

    void setMemory(String memory) {
        this.memory = memory;
    }

    String getMemory() {
        return memory != null ? memory : "";
    }

    void setMemoryOverhead(String memoryOverhead) {
        this.memoryOverhead = memoryOverhead;
    }

    String getMemoryOverhead() {
        return memoryOverhead;
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
class ExecutorSpec {
    public int instances;
    public String memory;
    public String memoryOverhead;
}

@JsonIgnoreProperties(ignoreUnknown = true)
class SparkApplicationSpec {
    public List<String> arguments;
    public DriverSpec driver;
    public ExecutorSpec executor;
}

@JsonIgnoreProperties(ignoreUnknown = true)
class AppStateSpec {
    public String state;
}

@JsonIgnoreProperties(ignoreUnknown = true)
class SparkApplicationStatus {
    public int executionAttempts;
    public AppStateSpec applicationState;
}
```

### `kind: SparkApplication` 목록 조회 및 출력

드디어 마지막 단계이다.

다음과 같은 코드를 작성하면 된다.

```java
import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Version;

@Version("v1beta2")
@Group("sparkoperator.k8s.io")
class SparkApplication extends CustomResource<SparkApplicationSpec, SparkApplicationStatus> implements Namespaced {
    // 여기에는 필드를 정의할 필요가 없다
}

MixedOperation<SparkApplication, KubernetesResourceList<SparkApplication>, Resource<SparkApplication>> sparkAppClient =
        kubernetesClient.resources(SparkApplication.class);

KubernetesResourceList<SparkApplication> sparkAppList = sparkAppClient.inNamespace(kubernetesClient.getNamespace()).list();

sparkAppList.getItems().forEach(app -> {
    logger.info(String.format("%s,%s,%d,%d",
            app.getMetadata().getName(),
            app.getSpec().driver.getMemory(),
            app.getSpec().executor.instances,
            app.getStatus().executionAttempts));
});
```
