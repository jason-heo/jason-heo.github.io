---
layout: post
title: "fabric8 kubernetes test 사용법 (mocking Kubernetes API Server)"
categories: "programming"
---

{% include test-for-data-engineer.md %}

## 개요

최근 Quarkus라는 것을 공부 중인데, quarkus에서 제공하는 기능 중에 kubernetes-client 기능이 있다.

[Kubernetes Client](https://quarkus.io/guides/kubernetes-client)에서 관련된 내용을 볼 수 있는데 이 문서를 보면 Kubernetes API 서버를 [mocking하는 내용](https://quarkus.io/guides/kubernetes-client#testing)이 있다.

꽤나 흥미있는 주제였라서 어떻게 구현되었는지 궁금했는데 이게 알고보니깐 fabric8 kubernetes에서 기본으로 제공 중인 기능이었다.

quarkus 가이드 문서를 따라서하는 건 쉬웠지만, 아무래도 quarkus 없이 fabric8만 이용해서 mocking하는 방법이 궁금했다. 이에 대한 내용은 [fabric8의 Mocking Kubernetes](https://github.com/fabric8io/kubernetes-client#mocking-kubernetes)에서 볼 수 있는데 문서만 봐서는 뭘 어떻게 설정해야 사용할 수 있는지 알기가 어려웠다.

그래서 테스트해본 내용을 블로그에 정리해본다.

아래에 사용된 소스 코드는 본인의 github에도 올려두었다. ([바로가기](https://github.com/jason-heo/fabric8-mock-k8s-server/))

아무래도 프로젝트를 최초 설정할 때 maven 의존성과 import 대상 등을 설정하는 것이 어려운데 본 포스팅이 도움되었으면 한다.

fabric8의 소스코드에서도 Unit Test를 위해서 아래의 코드를 사용 중이라고 한다. 각자 본인의 kubernetes 관련 프로젝트에서도 Mocking을 이용하여 Unit Test를 작성하는데 도움이 되면 좋겠다.

## Mocking mode

두 가지 mode가 존재한다

- CRUD mode
    - `CRUD`라는 많이 들어봤을 텐데 Create, Read, Update, Delete의 약자이다
    - 즉, k8s api server에 요청을 전송하여 k8s resource를 직접 생성/조회/수정/삭제를 할 수 있다
- Expectations mode (non-CRUD mode)
    - k8s resource를 직접 CRUD하지 않는다
    - 우리가 해야할 것은 "특정 API가 요청되었을 때 response를 정의"하는 것이다
    - 즉, request별로 expect를 정의하는 것이다

글로 설명하다보니 설명도 잘 안 되고 이해하는 분도 이해가 어려운 듯 한데 아래 코드로 보는 것이 훨씬 쉬울 것 같다.

## 코드

### `pom.xml` 설정

문서에 이 부분에 대한 설명이 없어서 제일 어려웠다.

다음과 같은 내용을 `pom.xml`에 추가하면 Mocking을 사용할 수 있다.

```xml
    <dependencies>
        <!-- k8s server mocking을 위한 필수 내용 -->
        <dependency>
            <groupId>io.fabric8</groupId>
            <artifactId>kubernetes-client</artifactId>
            <version>${fabric8.version}</version>
        </dependency>
        <dependency>
            <groupId>io.fabric8</groupId>
            <artifactId>kubernetes-test</artifactId>
            <version>${fabric8.version}</version>
        </dependency>
        <dependency>
            <groupId>io.fabric8</groupId>
            <artifactId>kubernetes-server-mock</artifactId>
            <version>${fabric8.version}</version>
        </dependency>
        <dependency>
            <groupId>io.rest-assured</groupId>
            <artifactId>rest-assured</artifactId>
            <version>${restAssured.version}</version>
            <scope>test</scope>
        </dependency>

        <!-- junit5를 위해 추가한 내용 -->
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter-api</artifactId>
            <version>${junit.version}</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter-params</artifactId>
            <version>${junit.version}</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter-engine</artifactId>
            <version>${junit.version}</version>
            <scope>test</scope>
        </dependency>
    </dependencies>
```

### CRUD mode

CRUD mode에서는 Mock Server에 GET/POST/DELETE 같은 요청을 전송하면 해당 요청을 Mock Server가 실제로 수행하게 된다.

장황한 글보다는 실제 코드를 보도록 하자.

코드 자체는 쉬우므로 상세한 설명은 생략한다.

```java
package io.github.jasonheo;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;

public class CrudMode {
    static String namespace = "ns1";

    public static void main(String[] args) throws Throwable {
        KubernetesServer server = new KubernetesServer(false, true);

        // mock server 실행
        server.before();

        CrudMode crudMode = new CrudMode();

        // pod 생성: pod 2개를 생성한다
        crudMode.setUp(server);

        // pod 목록 출력: pod 2개가 출력된다
        crudMode.listPods(server);

        // pod 삭제: pod 1개를 삭제한다
        server.getClient().pods().inNamespace(namespace).withName("pod1").delete();

        // pod 목록 출력: pod 1개가 출력된다
        crudMode.listPods(server);

        // mock server 종료
        server.after();
    }

    public void setUp(KubernetesServer server) {
        Pod pod1 = new PodBuilder()
                .withNewMetadata()
                .withName("pod1")
                .withNamespace(namespace)
                .and()
                .build();

        Pod pod2 = new PodBuilder()
                .withNewMetadata()
                .withName("pod2")
                .withNamespace(namespace)
                .and()
                .build();

        server
                .getClient()
                .pods()
                .inNamespace(namespace)
                .create(pod1);
        server
                .getClient()
                .pods()
                .inNamespace(namespace)
                .create(pod2);
    }

    public void listPods(KubernetesServer server) {
        // 아래 코드는 `kubectl get pods`에 대응되는 코드이다
        KubernetesClient client = server.getClient();
        client
                .pods()
                .inNamespace(namespace)
                .list()
                .getItems()
                .stream()
                .forEach(pod -> {
                    System.out.println(pod.getMetadata().getName());
                });
    }
}
```
### Expectations mode

이번에도 바로 코드를 보여주겠다.

```java
package io.github.jasonheo;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.PodListBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;

public class NonCrudMode {
    static String namespace = "ns1";

    public static void main(String[] args) {
        KubernetesServer server = new KubernetesServer(false, false);

        // mock 서버 실행
        server.before();

        NonCrudMode nonCrudMode = new NonCrudMode();

        // API 요청에 대한 응답 정의
        nonCrudMode.setUp(server);

        // pod 목록 출력
        nonCrudMode.listPods(server);

        // mock 서버 종료
        server.after();
    }

    public void setUp(KubernetesServer server) {
        Pod pod1 = new PodBuilder()
                .withNewMetadata()
                .withName("pod1")
                .and()
                .build();

        Pod pod2 = new PodBuilder()
                .withNewMetadata()
                .withName("pod2")
                .and()
                .build();

        // input: "/api/v1/namespaces/ns1/pods" 요청이 인입된 경우
        // response: pod 2개를 return하도록 한다
        server
                .expect()
                .get()
                .withPath(String.format("/api/v1/namespaces/%s/pods", namespace))
                .andReturn(200, new
                        PodListBuilder()
                        .withNewMetadata()
                        .withResourceVersion("1")
                        .endMetadata()
                        .withItems(pod1, pod2)
                        .build())
                .always();

    }

    public void listPods(KubernetesServer server) {
        KubernetesClient client = server.getClient();
        client
                .pods()
                .inNamespace(namespace)
                .list()
                .getItems()
                .stream()
                .forEach(pod -> {
                    System.out.println(pod.getMetadata().getName());
                });

    }
}
```

non-CRUD mode의 경우 중요한 것은 "request별로 response를 정의"하는 것이다. 이에 대해선 fabric8의 mock 관련 Test Code를 참고하는 것이 좋겠다. ([바로가기](https://github.com/fabric8io/kubernetes-client/tree/master/kubernetes-tests/src/test/java/io/fabric8/kubernetes/client/mock)) 사실 나도 잘 모르기 때문에 필요한 내용이 생실 때 직접 찾아보려고 생각 중이다.

### test with JUnit5

이번에는 JUnit5에서 사용하는 방법을 보자. 내용 자체는 위의 코드랑 별다를 것이 없다.

Test용 class에 `@EnableKubernetesMockClient(crud = true)` annotation을 추가하면 된다.

그리고 mock server와 client 생성 방법은 다음과 같이 설정하면 된다.

```java
@EnableKubernetesMockClient(crud = true)
public class CrudModeTest {
    KubernetesMockServer server;
    KubernetesClient client;
    ...
}
```

전체 코드는 다음과 같다. non-CRUD에 대한 TC는 git repository에서 참고하면 된다 (별다른 내용이 없기 때문에 글 본문에는 넣지 않았다)

```java
package io.github.jasonheo;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.server.mock.EnableKubernetesMockClient;
import io.fabric8.kubernetes.client.server.mock.KubernetesMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@EnableKubernetesMockClient(crud = true)
public class CrudModeTest {
    String namespace = "ns1";

    /**
     * 헷갈리는 점
     *   - 아래처럼 생성해도 되는데, @EnableKubernetesMockClient(crud = true) annotation을 사용하라고 한 이유
     *   - KubernetesServer server = new KubernetesServer(false, true);
     */
    KubernetesMockServer server;
    KubernetesClient client;

    @BeforeEach
    public void beforeEach() {
        Pod pod1 = new PodBuilder()
                .withNewMetadata()
                .withName("pod1")
                .withNamespace(namespace)
                .and()
                .build();

        Pod pod2 = new PodBuilder()
                .withNewMetadata()
                .withName("pod2")
                .withNamespace(namespace)
                .and()
                .build();

        client
                .pods()
                .inNamespace(namespace)
                .create(pod1);

        client
                .pods()
                .inNamespace(namespace)
                .create(pod2);

    }

    @AfterEach
    public void afterEach() {
        server.destroy();
    }

    @Test
    public void testListPods() {
        int numPods = client
                .pods()
                .inNamespace(namespace)
                .list()
                .getItems()
                .size();

        assertEquals(2, numPods);
    }
}
```

### Mock server의 port 조회하기

(필요한 상황이있을지 모르겠지만) Mock server의 port는 `KubernetesMockServer.getPort()`를 이용하면 조회할 수 있다.

k8s mock server에 직접 요청을 전송해야할 필요가 있을 때 사용하면 된다.

참고로 mock server의 content-type이 json이 아니다. 이것에 rest-assured와는 연동이 좀 번거롭다 (혹시 해결 방법 아는 분 계시면 알려주시면 감사하겠다)

```java
String hostname = server.getHostName();
int port = server.getPort();

String url = "http://" + hostname + ":" + port + "/api/v1/namespaces/" + namespace + "/pods";

// mock server의 response content type이 json이 아니다
// 따라서 response 전체를 string 변수에 저장했다
String response = RestAssured
        .given()
            .log()
            .all()
        .when()
            .get(url)
            .asString();

List<String> items = from(response).getList("items");

assertEquals(2, items.size());
```

{% include test-for-data-engineer.md %}
