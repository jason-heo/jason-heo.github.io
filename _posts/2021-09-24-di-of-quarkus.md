---
layout: post
title: "Quarkus에서의 Dependency Injection"
categories: "programming"
---

테스트에 사용된 코드는 github에 올려두었습니다. [github 바로가기](https://github.com/jason-heo/quarkus-dependency-injection)

## 목차

- [Disclaimer](#disclaimer)
- [1. Quarkus 기본 - Rest Application 생성 및 실행하기](#1-quarkus-기본---rest-application-생성-및-실행하기)
  - [1-1) Project 생성](#1-1-project-생성)
  - [1-2) `pom.xml` 및 생성된 소스코드 확인](#1-2-pomxml-및-생성된-소스코드-확인)
  - [1-3) Rest Server 실행 및 rest 요청하기](#1-3-rest-server-실행-및-rest-요청하기)
- [2. CDI (Context and Dependency Injection)](#2-cdi-context-and-dependency-injection)
  - [2-1) `@Inject`](#2-1-inject)
  - [2-2) scope](#2-2-scope)
  - [2-3) Lifecycle Callbacks](#2-3-lifecycle-callbacks)
  - [2-4) Interceptors](#2-4-interceptors)
  - [2-5) `@Produces` 1](#2-5-produces-1)
  - [2-6) `@Produces` 2](#2-6-produces-2)
  - [2-7) property에 의존적인 injection](#2-7-property에-의존적인-injection)
  - [2-8) maven build profile에 의존적인 injection](#2-8-maven-build-profile에-의존적인-injection)
  - [2-9) private variable을 사용하지 않는게 좋다](#2-9-private-variable을-사용하지-않는게-좋다)
  - [2-10) Quarkus CDI의 limitations](#2-10-quarkus-cdi의-limitations)
  - [2-11) `@Alternative` and `@Priority`](#2-11-alternative-and-priority)
- [3. Unit Test, Mocking](#3-unit-test-mocking)
  - [3-1) 예제 코드 - Email 전송하기](#3-1-예제-코드---email-전송하기)
  - [3-2) 참고: REST-assured를 이용한 Unit Test](#3-2-참고-rest-assured를-이용한-unit-test)
  - [3-3) 문제 정의 - Unit Test 시에 email 전송을 생략하려면?](#3-3-문제-정의---unit-test-시에-email-전송을-생략하려면)
  - [3-4) 좀 더 간결한 코드 - `@InjectMock`](#3-4-좀-더-간결한-코드---injectmock)

## Disclaimer

본 글은 Quarkus 기능을 DI 위주로 소개하는 글입니다.

본인도 처음 접하는 내용이 많아서 인터넷에서 찾은 각종 문서들을 읽으면서 직접 테스트해보고 취합 정리한 글입니다.

따라서 부정확한 내용이 포함되었을 수 있습니다. 단순 기능 소개 용도일 뿐 아래 내용이 Best Practice를 담고 있지 않습니다.

사용된 버전

- Java 11
- Quarkus 2.2.3 Final

참고한 문서들, 같이보면 좋을 문서들

- Quarkus 기초
    - [Getting Started](https://quarkus.io/guides/getting-started)
- CDI (Contents & Dependency Injection)
    - [Quarkus CDI Reference](https://quarkus.io/guides/cdi-reference)
    - [Introduction to contexts and dependency injection](https://quarkus.io/guides/cdi)
    - [Introduction to CDI](https://www.baeldung.com/java-ee-cdi), 2021.05.15
    - [Quarkus Dependency Injection](https://quarkus.io/blog/quarkus-dependency-injection/), 2021.07.24
    - [JBoss EAP 6과 친해지기 3탄 - Java 기반 웹 시스템의 이해](https://chanchan-father.tistory.com/668?category=843153)
- Unit Test, Mocking
    - [Quarkus - Testing Your App#Mock Support](https://quarkus.io/guides/getting-started-testing#mock-support)
    - [Quarkus - Testing your App#Further simplcation with `@InjectMock`](https://quarkus.io/guides/getting-started-testing#further-simplification-with-injectmock)
    - [Mocking CDI beans in Quarkus](https://quarkus.io/blog/mocking/), 2020.04.28
    - [Testing Quarkus Applications](https://www.baeldung.com/java-quarkus-testing), 2020.08.29
    - [Using Alternatives in CDI Applications](https://docs.oracle.com/javaee/7/tutorial/cdi-adv002.htm)

## 1. Quarkus 기본 - Rest Application 생성 및 실행하기

### 1-1) Project 생성

아래의 명령을 실행하면 간단한 Rest Application을 실행할 수 있다.

```console
$ mvn io.quarkus.platform:quarkus-maven-plugin:2.2.3.Final:create \
    -DprojectGroupId=io.github.jasonheo \
    -DprojectArtifactId=quarkus-di \
    -DclassName="io.github.jasonheo.GreetingResource" \
    -Dpath="/hello"

$ cd quarkus-di

$ ls
README.md    mvnw*        mvnw.cmd*    pom.xml        src/
```

혹은 https://code.quarkus.io/ 에 방문하셔 원하는 의존성 패키지를 선택 후 zip file을 다운로드 할 수도 있다.

### 1-2) `pom.xml` 및 생성된 소스코드 확인

자동으로 생성된 `pom.xml` 내용은 [여기](https://github.com/jason-heo/quarkus-dependency-injection/blob/c6847428989d0c5f7ab6f322b783ba0191881da9/pom.xml)에서 볼 수 있다.

`src/main/java/io/github/jasonheo/GreetingResource.java` 파일을 열어보면 다음과 같은 내용이 추가된 걸 볼 수 있다.

```java
package io.github.jasonheo;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/hello")
public class MyResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "Hello RESTEasy";
    }
}
```

`GreetingResource`는 `/hello`라는 URL에 접근하는 경우 `Hello RESTEasy`라는 내용을 출력한다.

### 1-3) Rest Server 실행 및 rest 요청하기

아래의 명령을 실행하면  Rest Server가 실행된다.

```console
$ ./mvnw compile quarkus:dev -DskipTests
```

수행 결과는 다음과 같다.

```
__  ____  __  _____   ___  __ ____  ______
 --/ __ \/ / / / _ | / _ \/ //_/ / / / __/
 -/ /_/ / /_/ / __ |/ , _/ ,< / /_/ /\ \
--\___\_\____/_/ |_/_/|_/_/|_|\____/___/
2021-10-01 19:46:56,331 INFO  [io.quarkus] (Quarkus Main Thread) quarkus-di 1.0.0-SNAPSHOT on JVM (powered by Quarkus 2.2.3.Final) started in 2.024s. Listening on: http://localhost:8080
2021-10-01 19:46:56,336 INFO  [io.quarkus] (Quarkus Main Thread) Profile dev activated. Live Coding activated.
2021-10-01 19:46:56,338 INFO  [io.quarkus] (Quarkus Main Thread) Installed features: [cdi, resteasy, smallrye-context-propagation]

--
Tests paused
Press [r] to resume testing, [o] Toggle test output, [h] for more options>
```

Quarkus는 live coding mode를 지원한다. 소스 코드를 수정하였더라도 mvn 명령을 restart할 필요없이 자동으로 수정된 코드가 수행된다. (엄청 편하다)

이제 브라우저에서 http://localhost:8080/hello/ 에 접속을 해보면 `Hello RESTEasy`가 출력되는 것을 볼 수 있다.

## 2. CDI (Context and Dependency Injection)

### 2-1) `@Inject`

Quarkus에서는 `@Inject` annotation을 이용하면 DI를 쉽게 사용할 수 있다.

코드를 다음과 같이 수정해보자.

```java
package io.github.jasonheo;

import org.jboss.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/hello")
public class MyResource {

    @Inject
    Logger logger;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        logger.info("hello() called");

        return "Hello RESTEasy";
    }
}
```

`new Logger()`도 없는데 `logger` instance가 생성되었다.

### 2-2) scope

이번엔 scope 개념에 대해서 알아보자.

우선 다음과 같은 `DbService`를 만들어보자.

하는 일은 "DB로부터 name을 조회"하는 class라고 가정한다.

```java
package io.github.jasonheo;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DbService {
    public String getName(String id) {
        return "name of id '" + id + "'";
    }
}
```

`MyResource`의 내용은 다음과 같이 변경했다.

```java
package io.github.jasonheo;

import org.jboss.logging.Logger;
import org.jboss.resteasy.annotations.jaxrs.PathParam;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/hello")
public class MyResource {

    @Inject
    Logger logger;

    @Inject
    DbService dbService;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/id/{id}")
    public String hello(@PathParam String id) {
        logger.info("hello(" + id + ") called");
        return "Hello " + dbService.getName(id);
    }
}
```

이후 http://localhost:8080/hello/id/1 에 접속하면 `Hello name of id '1'`가 출력된 것을 볼 수 있다.

위의 예에서는 `@ApplicationScoped`를 사용하였다. scope의 의미는 "injection을 통해 생성된 instance의 scope"를 의미하는데 `@ApplicationScoped`는 application이 살아있는 동안 instance가 계속 유지된다는 것을 의미한다. 반대의 개념으로 `@RequestScoped`도 존재한다. 이것은 매 http request마다 instance를 생성하는 것을 의미한다.

`@ApplicationScoped`만 봤을 때는 명확한 의미를 알기 어렵지만 `@RequestScoped`를 이해한다면 `@ApplicationScoped`의 의미도 쉽게 이해할 수 있다.

그 외에도 다양한 scope가 존재하는데 이에 대해선 [10. What scopes can I actually use in my Quarkus application?](https://quarkus.io/guides/cdi#what-scopes-can-i-actually-use-in-my-quarkus-application)를 참고하기 바란다.

Quarkus 관련 문서를 읽다보면 `normal scope`니 `pseudo scope`니 하는 용어가 나오는데, normal scope는 `@ApplicationScoped`와 `@RequestScoped`인 듯 하다.

`@ApplicationScoped`과 `@Singleton`은 비슷한 annotation이다. 두 개의 차이는 client proxy 생성 여부에 따라 다르다. client proxy가 어떻게 생겼는지는 [CDI and Proxies](https://blog.triona.de/development/java/cdi-and-proxies.html)에 나와있다. 

`@Inject`를 이용해서 injection이 발생할 때는 DI container가 개입되는데, DI container는 directly 생성할지? 혹은 client proxy를 이용할지 결정하게 된다.

이런 결정은 'bean의 lifecycle'에 의해 결정되고, 'bean의 lifecycle'은 scope에 의해 결정된다.

normal scope에서는 client proxy가 사용된다고 한다. client proxy가 중요한 이유는 서로 다른 scope bean 간에 DI를 사용할 수 있기 때문이라고 한다.

원래 CDI에서는 default constructor를 생성해야한다. 하지만, Quarkus에서는 default constructor를 생성할 필요가 없다. 또한 constructor가 1개인 경우 `@Inject` 조차 필요가 없다. ([관련 내용](https://quarkus.io/guides/cdi-reference#simplified-constructor-injection))

### 2-3) Lifecycle Callbacks

[관련 글](https://quarkus.io/guides/cdi#lifecycle-callbacks)

CDI에서는 Lifecycle callback을 제공한다. 말로 설명하는 것보다 아래의 예를 보는 것이 더 편할 것이다.

```java
package io.github.jasonheo;

import org.jboss.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class DbService {
    @Inject
    Logger logger;

    public String getName(String id) {
        return "name of id '" + id + "'";
    }

    // instance가 생성된 후 호출되는 함수
    @PostConstruct
    void init() {
        logger.info("DbService created");
    }

    // instance가 삭제될 때 호출되는 함수
    @PreDestroy
    void destroy() {
        logger.info("now, destroying DbService");
    }
}
```

자, 이제 다시 http://localhost:8080/hello/id/1 에 접속하면 `"DbService created"`가 출력된 것을 볼 수 있다. `@ApplicationScoped`이기 떄문에 몇 번을 요청하든 `"DbService created"`는 최초 1회만 출력된다. `@RequestScoped`로 변경을 하면 매 요청시마다 로그가 출력되는 것을 볼 수 있다.

### 2-4) Interceptors

다음과 같은 intercept들이 있다.

- `javax.interceptor.AroundConstruct`
- `javax.interceptor.AroundInvoke`
- `javax.interceptor.AroundTimeout`

예를 들어 `AroundInvoke`를 이용하면 특정 함수가 실행되기 전/후에 원하는 로직을 추가할 수 있다. (일종의 hooking 같아 보인다)

Quarkus의 [14.2. Interceptors](https://quarkus.io/guides/cdi#interceptors) 문서를 보면 다음과 같은 예제가 있다.

```java
import javax.interceptor.Interceptor;
import javax.annotation.Priority;

@Logged 
@Priority(2020) 
@Interceptor 
public class LoggingInterceptor {

   @Inject 
   Logger logger;

   @AroundInvoke 
   Object logInvocation(InvocationContext context) {
      // ...log before

      Object ret = context.proceed(); 

      // ...log after

      return ret;
   }

}
```

위 코드에서는 `AroundInvoke`를 이용하여 함수가 실행되지 전/후에 로그를 출력하는 예이다.

그런데 무슨 문제인지 잘 테스트가 안 되서 실제 작동되는 예제를 만들지는 못하였다.

([이 글](https://javaee.github.io/tutorial/interceptors001.html#GKIGQ)에 따르면 `@PostConstruct`도 일종의 interceptor인 것 같다)

Apache TomEE에 [Simple CDI Interceptor](https://tomee.apache.org/examples-trunk/simple-cdi-interceptor/)라는 문서가 있어서 이대로 따라해봤는데도 작동을 안한다.

### 2-5) `@Produces` 1

`@Produces` annotation을 이용하여 inject할 instance를 생성할 수도 있다.
3rd party library 사용 시에 편하다.

`@Produces`에 관한 내용은 [13. 절](https://quarkus.io/guides/cdi#ok-you-said-that-there-are-several-kinds-of-beans)에 설명되어 있다.

`@Produces`가 유용한 경우는 3rd party library를 사용하는 경우이다.

예를 들어, Quarkus에서 fabric8을 이용하여 `KubernetesClient`를 생성한다고 가정하자. fabric8은 3rd party library이기 떄문에 내 마음대로 생성자를 만들 수 없다. 이때는 다음과 같이 `@Produces`를 사용하면 된다.

```java
@Singleton
public class KubernetesClientProducer {

    @Produces
    @ApplicationScoped
    // default는 @ApplicationScoped 임
    public KubernetesClient kubernetesClient() {
        Config config = new Config();
        
        config.setHttp2Disable(true);

        KubernetesClient client = new DefaultKubernetesClient(config);

        return client;
    }
}
```
([출처](https://quarkus.io/guides/kubernetes-client#overriding))

`KubernetesClient`를 생성할 때 다양한 configuration을 지정할 수 있어야하는데, 이런 경우 `@Produces`가 유용하게 사용된다.

(참고: 실제 사용 시에는 `return new DefaultKubernetesClient();`만 있어도 작동하는데 충분하다. 위의 예는 "custom한 설정을 쉽게 할 수 있다"는 측면을 설명하기 위해 작성된 코드이다)

www.baeldung.com의 [7. The @Produces Annotation](https://www.baeldung.com/java-ee-cdi#the-produces-annotation)에 따르면 `@Produces`는 Factory class를 사용할 수 있게 한다고 한다.

> `@Produces` allows us to implement factory classes, whose responsibility is the creation of fully-initialized services.

### 2-6) `@Produces` 2

이번에는 `DbService`에 `@Produces`를 적용해보자. (본인도 정확한 이유는 모르지만, scope의 위치를 bean에서 `@Produces` method로 옮겨야만 제대로 작동을 하였다. 내가 실수한 것이라면 다행이지만 혹시라도 다른 분들도 동일한 문제를 겪을 수도 있어서 해당 내용을 정리해봤다)

이번 예에서는 `DbService`에 `connectionString`이라는 argument를 추가하였다. 그리고 `@Produces`를 이용하여 bean instance를 생성해보자.

우선 `DbService`는 다음과 같이 수정하였다. 코드를 보면 알겠지만 `DbService`에는 `@ApplicationScoped`가 없다. 이걸 추가하면 에러가 발생한다.

```java
public class DbService {
    @Inject
    Logger logger;

    public DbService(String connectionString) {
        // 현 시점에서는 `logger`가 아직 inject되기 전이다.
        // 따라서 logger.info()를 호출하면 NPE가 발생한다.
        System.out.println("connectionString='" + connectionString + "'");

        // DB 연결 코드
        // ...
    }

    public String getName(String id) {
        return "name of id '" + id + "'";
    }
}
```

그리고 다음과 같은 Producer class를 만들자.

```java
package io.github.jasonheo;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.Produces;

public class Producers {
    @Produces
    @ApplicationScoped
    public DbService getDbService() {
        DbService dbService = new DbService("jdbc:mysql://hostname.com:3306/sakila");

        return dbService;
    }
}
```

`@ApplicationScoped`가 `getDbService()` method에 annotate된 걸 볼 수 있다.

`MyResource`는 코드 변경없이 동일한 코드를 계속 사용한다.

이제 http://localhost:8080/hello/id/1 에 접속하면 `DbService` instance가 잘 생성된 것을 볼 수 있다.

그런데, 만약 `class DbService`에 `@ApplicationScoped` annotation을 사용한 경우에는 다음과 같은 에러가 발생하게 된다.

본인도 아직은 공부를 하는 중이라서 정확한 이유는 모르겠다.

```
javax.enterprise.inject.spi.DeploymentException: Found 2 deployment problems: 
[1] Ambiguous dependencies for type io.github.jasonheo.DbService and qualifiers [@Default]
    - java member: io.github.jasonheo.MyResource#dbService
    - declared on CLASS bean [types=[io.github.jasonheo.MyResource, java.lang.Object], qualifiers=[@Default, @Any], target=io.github.jasonheo.MyResource]
    - available beans:
        - CLASS bean [types=[io.github.jasonheo.DbService, java.lang.Object], qualifiers=[@Default, @Any], target=io.github.jasonheo.DbService]
        - PRODUCER METHOD bean [types=[io.github.jasonheo.DbService, java.lang.Object], qualifiers=[@Default, @Any], target=io.github.jasonheo.DbService getDbService(), declaringBean=io.github.jasonheo.Producers]
[2] Unsatisfied dependency for type java.lang.String and qualifiers [@Default]
    - java member: io.github.jasonheo.DbService#<init>()
    - declared on CLASS bean [types=[io.github.jasonheo.DbService, java.lang.Object], qualifiers=[@Default, @Any], target=io.github.jasonheo.DbService]
    The following beans match by type, but none have matching qualifiers:
        - Bean [class=java.lang.String, qualifiers=[@ConfigProperty, @Any]]
    at io.quarkus.arc.processor.BeanDeployment.processErrors(BeanDeployment.java:1108)
    at io.quarkus.arc.processor.BeanDeployment.init(BeanDeployment.java:265)
    ...
    ...
```

### 2-7) property에 의존적인 injection

환경에 따라 의존적으로 injection을 할 수 있다. 우선 property에 의존적인 것부터 확인해보자.

우선 `Producer`를 다음과 같이 수정한다.

```java
package io.github.jasonheo;

import io.quarkus.arc.properties.IfBuildProperty;
import io.quarkus.arc.properties.UnlessBuildProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.Produces;

public class Producers {
    @Produces
    @ApplicationScoped
    @IfBuildProperty(name = "env", stringValue = "local")
    public DbService getLocalDbService() {
        DbService dbService = new DbService("jdbc:mysql://localhost:3306/sakila");

        return dbService;
    }

    @Produces
    @ApplicationScoped
    @UnlessBuildProperty(name = "env", stringValue = "local")
    public DbService getRealDbService() {
        DbService dbService = new DbService("jdbc:mysql://real-db.com:3306/sakila");

        return dbService;
    }
}
```

예상했겠지만, `env`가 `local`인 경우와 아닌 경우에 따라 `connectionString`을 다르게 지정하는 예이다.

이후 `src/main/resources/application.properties` 파일에 아래의 내용을 추가한다.

```
env=local
```

이제 http://localhost:8080/hello/id/1 에 접속한 뒤 로그를 보면 `localhost:3306`에 접속하는 것을 볼 수 있다.

`env=real`처럼 `local` 이외의 값을 지정하면 `real-db.com:3306`에 접속을 한다.

### 2-8) maven build profile에 의존적인 injection

property file이 아닌 build profile에 의존적인 injection도 가능하다.

maven build profile을 모르는 경우 [maven profile 을 이용하여 운영 환경에 맞게 패키징 하기](https://www.lesstif.com/java/maven-profile-14090588.html)를 참고한다.

이후 `@IfBuildProfile`, `@UnlessBuildProfile`를 사용하면 된다. 예제는 Quarkus 문서의 [5.7. Enabling Beans for Quarkus Build Profile](https://quarkus.io/guides/cdi-reference#enabling-beans-for-quarkus-build-profile)에서 볼 수 있다.

### 2-9) private variable을 사용하지 않는게 좋다

bean class의 member variable은 `private`으로 선언하지 않는 것이 좋다고 한다.

관련된 내용은 다음의 글에서 볼 수 있다.

- [@Inject With Package-Private Fields Only, Or Harmful Accessors](https://adambien.blog/roller/abien/entry/inject_with_package_private_fields)
    - private member를 사용하지 않아야 Unit Test 시에 mocking하기가 쉽다
- Quarkus guide 문서 [2. Native Executables and Private Members](https://quarkus.io/guides/cdi-reference#native-executables-and-private-members)
    - private member를 사용하지 말아야 native image의 size가 줄어들게 된다

private member를 사용하지 않는 방법에는 다음과 같은 것들이 있다.

- package-private modifier 사용
    - java에서 `private`, `public` 같은 modifier를 사용하지 않으면 기본적으로 package-private이다
    - 즉, 동일 package에서는 access가 가능하다
- constructor injection
- public setter 사용
    - adambien.blog 에 나온 방법
    - private member를 사용하는 방법이므로, native image의 size는 줄어들지 않을 것 같다

### 2-10) Quarkus CDI의 limitations

Quarkus의 CDI는 J2EE 표준 CDI의 구현과 다른 점이 있다.

정확한 것은 [4. Limitations](https://quarkus.io/guides/cdi-reference#limitations)에 나열되어 있다.

이 중에서 "`beans.xml` 내용이 무시됨"은 큰 차이점이라 생각된다.

### 2-11) `@Alternative` and `@Priority`

마지막으로 `Alternative`와 `@Priority`에 대해서 알아보자.

(아래의 예는 baeldung.com의 [3.5. The @Default and @Alternative Annotations](https://www.baeldung.com/java-ee-cdi#5-the-default-and-alternative-annotations)에서 가져온 예이다)

DI를 사용하다보면 ambiguity 문제가 발생할 수 있다. 예를 들어 다음과 같이 `ImageFileEditor`의 구현이 여러 개인 경우 `ImageFileEditor editor`에 대해 어떤 bean을 inject해야할지 ambiguous하므로 에러가 발생한다.

```java
public class GifFileEditor implements ImageFileEditor { ... }

public class JpgFileEditor implements ImageFileEditor { ... }

public class PngFileEditor implements ImageFileEditor { ... }
```

이때 `@Alternative`를 사용할 수 있다.

```java
@Alternative
public class GifFileEditor implements ImageFileEditor { ... }

@Alternative
public class JpgFileEditor implements ImageFileEditor { ... }

public class PngFileEditor implements ImageFileEditor { ... }
```

위의 코드에서는 `PngFileEditor`가 inject된다. 

baeldung.com 원문에는 아래와 같은 내용이 있는데 사실 이해는 잘 안 된다.

> how we can very easily swap the run-time injection of implementations by simply switching the @Alternative annotations in the service classes.

이해가 안 되는 점은 'run-time에 easily swap' 가능하다는 부분이다. `JpgFileEditor`를 사용하고 싶은 경우에는 `@Alternative` annotation을 수정해야할 것 같은데 이렇게 되면 run-time에 swap하기 어려운 것 같은데, 아마 내가 뭔가 모르는 것이 또 있는 것 같다.

`@Priority`는 `@Alternative` 사이의 우선 순위를 지정하는 annotation이다. 값이 작을 수록 우선 순위가 높다.

암튼 `@Alternative`와 `@Priority`는 다음 장에서 설명할 Unit Test를 위해서 알고 있어야 한다.

## 3. Unit Test, Mocking

이제 마지막 내용이다.

### 3-1) 예제 코드 - Email 전송하기

https://quarkus.io/guides/getting-started-testing#mock-support

http://localhost:8080/hello/id/1 에 접속 시, 사용자에게 email을 전송하다고 가정하자.

우선 다음과 같은 `EmailService`를 작성한다.

```java
package io.github.jasonheo;

import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class EmailService {
    @Inject
    Logger logger;

    public void sendEmail(String emailAddr) {
        // email 전송 코드

        logger.info("email sent");
    }
}
```

`DbService`에는 다음과 같이 `getEmailAddr()`을 추가했다.

```java
public class DbService {
    ...

    public String getEmailAddr(String id) {
        return "email of id '" + id + "'";
    }
```

마지막으로 `MyResource`는 다음과 같이 수정하였다.

```java
@Path("/hello")
public class MyResource {

    @Inject
    Logger logger;

    @Inject
    DbService dbService;

    @Inject
    EmailService emailService;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/id/{id}")
    public String hello(@PathParam String id) {
        logger.info("hello(" + id + ") called");

        emailService.sendEmail(dbService.getEmailAddr(id));

        return "Hello " + dbService.getName(id);
    }
}
```

이제 http://localhost:8080/hello/id/1 에 접속하면 `1`번 사용자에게 email이 발송된다.

### 3-2) 참고: REST-assured를 이용한 Unit Test

(Mocking과 관련없는 이야기지만) Unit Test용 code를 설명하기 위해 간단히 설명한다.

Quarkus에서는 [REST-assured](https://rest-assured.io/)를 이용하여 Rest endpoint를 쉽게 Test할 수 있다. (그냥 `쉽게`도 아니고 매우 쉽고 가독성 좋게 테스트 가능하다)

`src/test/java/io/github/jasonheo/MyResourceTest.java`에 기본으로 생성된 TC가 있는데 이 내용을 다음과 같이 수정해보자.

```java
package io.github.jasonheo;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class MyResourceTest {

    @Test
    public void testHelloEndpoint() {
        given()
          .when().get("/hello/id/{id}", 1)
          .then()
             .statusCode(200)
             .body(is("Hello name of id '1'"));
    }

}
```

이제 console에서 `mvn test`를 실행해보면 1개의 Test가 성공적으로 실행된 것을 볼 수 있다.

```console
$ mvn test
...
...
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 3.513 s - in io.github.jasonheo.MyResourceTest
2021-10-02 10:25:05,019 INFO  [io.quarkus] (main) Quarkus stopped in 0.044s
[INFO]
[INFO] Results:
[INFO]
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  7.731 s
[INFO] Finished at: 2021-10-02T10:25:05+09:00
[INFO] ------------------------------------------------------------------------
```

### 3-3) 문제 정의 - Unit Test 시에 email 전송을 생략하려면?

위의 코드에는 문제가 있는데 Test를 실행할 때마다 email이 전송된다는 점이다.

이를 해결하는 방법이 `EmailService`를 mocking 하는 것이고, CDI에서는 `@Alternative`와 `@Priority`를 이용하여 mocking할 수 있다.

`src/test/java/io/github/jasonheo/NoOpEmailService.java`에 다음과 같은 파일을 만들어보자.

```java
package io.github.jasonheo;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;

@ApplicationScoped
@Alternative
@Priority(1)
public class NoOpEmailService extends EmailService {
    @Override
    public void sendEmail(String emailAddr) {
        logger.info("email not sent");
    }
}
```

다음과 같은 annotation을 볼 수 있다.

```
@Alternative
@Priority(1)
```

즉, 우선 순위가 높은 alternative bean을 inject하여 email을 전송하지 않도록 한 것이다.

test를 다시 실행하면 `"email not sent"`가 출력된 것을 볼 수 있다. 즉, mock class가 inject된 건 것이다.

### 3-4) 좀 더 간결한 코드 - `@InjectMock`

3-3)에서 소개한 방법은 단점이 있는데 매번 mock class를 생성해야한다는 점이다.


[Mocking CDI beans in Quarkus](https://quarkus.io/blog/mocking/)를 보면 위 방법을 old approach라고 부르고 있으며 Quarkus 1.4부터는 new approach를 사용할 수 있다고 한다.

new approach에는 다음과 같은 두 가지 방법이 있다.

- `@QuarkusMock`
- `@InjectMock`

위 블로그를 보면 `@InjectMock`이 최종적으로 제일 간단한 방법 같아서 본 글에서도 `@InjectMock`만 테스트해보았다.

`@InjectMock`은 CDI의 기능이라기보다는 [Mockito](https://site.mockito.org/)를 사용한 방법으로 생각된다. 따라서 아래의 내용은 Quarkus DI 본연의 기능보다는 Mockito에 대한 설명이라 볼 수 있다. (앞서 이야기한 것처럼 본인은 본 포스팅에 나오는 개념에 대해 익숙하지 않다. 아직 Mockito도 잘 모르기 때문에 아래 내용은 잘못된 내용을 포함할 수 있다)

`@InjectMock`을 사용하기 위해선 `pom.xml`에 아래의 내용을 추가해야한다.

```xml
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-junit5-mockito</artifactId>
    <scope>test</scope>
</dependency>
```

이제 `src/test/java/io/github/jasonheo/InjectMockTest.java`에 다음과 같은 내용을 적어보자.

```java
package io.github.jasonheo;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class InjectMockTest {

    @InjectMock
    EmailService emailService;

    @Test
    public void testHelloEndpoint() {
        given()
                .when().get("/hello/id/{id}", 1)
                .then()
                .statusCode(200)
                .body(is("Hello name of id '1'"));
    }
}
```

`@InjectMock`을 선언한 것만으로도 `"email not sent"`가 출력되지 않은 것을 볼 수 있다. 이유는 "`@InjectMock`으로 mock instance를 생성 시 default가 아무 것도 하지 않는 것"이기 때문이다.

아래와 같이 명시적으로 `doNothing()`으로 선언해도 된다.

```java
@QuarkusTest
public class InjectMockTest {

    @InjectMock
    EmailService emailService;

    @BeforeEach
    public void setup() {
        doNothing().when(emailService).sendEmail(Mockito.anyString());
    }

    ...
}
```

위의 코드는 `void` 함수이고 아무 것도 출력하지 않다보니 mocking이 제대로 적용된 것인지 잘 파악이 안 된다.

이번엔 Mockito를 이용하여 `DbService.getName()`의 return value를 변경해보자.

```java
@QuarkusTest
public class InjectMockTest {

    @InjectMock
    EmailService emailService;

    @InjectMock
    DbService dbService;

    @BeforeEach
    public void setup() {
        doNothing().when(emailService).sendEmail(Mockito.anyString());

        // DbService.getName()이 호출되면 "my id"를 return한다.
        Mockito.when(dbService.getName(Mockito.anyString())).thenReturn("my id");
    }

    @Test
    public void testHelloEndpoint() {
        given()
                .when().get("/hello/id/{id}", 1)
                .then()
                .statusCode(200)
                .body(is("Hello name of id '1'"));
    }
}
```

`setup()`을 보면 `DbService.getName()`이 실행되면 무조건 `"my id"`를 return하도록 정의하였다. 그렇다. Mockito를 이용하여 method 행동을 새로 정의한 것이다.

항상 `"my id"`를 return하므로 위의 테스트는 실패하게 된다. Mockito를 이용하여 mock class를 정의하지 않고 behavior를 재정의할 수 있음을 보여주는 예제일 뿐이다.

Mockito에는 수많은 기능이 존재한다. 예를 들어, 특정 함수가 실행되었는지 실행되었다면 몇 번 실행되었는지도 확인할 수 있다. 자세한 것은 아무래도 Mockito에 관련된 다른 tutorial을 찾아보는 것이 좋겠다.

위의 예제에서는 `DbService.getName()`이 항상 `"my id"`를 return하였다. 이번에는 마지막으로 `DbService.getName()` 호출 시 argument에 접근하는 방법에 대해 알아보자.

```java
package io.github.jasonheo;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.doNothing;

@QuarkusTest
public class InjectMockTest {

    @InjectMock
    EmailService emailService;

    @InjectMock
    DbService dbService;

    @BeforeEach
    public void setup() {
        doNothing().when(emailService).sendEmail(Mockito.anyString());

        // case 1: argument와 상관없이 항상 동일한 값을 return
        // Mockito.when(dbService.getName(Mockito.anyString())).thenReturn("my id");

        // case 2: argument에 의존적인 값을 return
        Mockito.when(dbService.getName(Mockito.anyString())).thenAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();

                return "name of id '" + args[0] + "'";
            }
        });
    }

    @Test
    public void testHelloEndpoint() {
        given()
                .when().get("/hello/id/{id}", 1)
                .then()
                .statusCode(200)
                .body(is("Hello name of id '1'"));
    }
}
```

Mockito의 `thenAnswer()`를 이용하여, `getName()`의 argument value를 return하도록 하였다. (당연하겠지만, 원래 함수와 동일한 일을 하므로 Test도 통과를 한다)

## 4. 마무리

올해 6월경인가, Spark Operator를 알아보다가 Quarkus를 알게 되면서 그동안 잘 알지 못했던 기술 및 라이브러리들을 알게 되었다. 이후 9월부터 본격적으로 Quarkus에 대해 공부를 하게 되었다. Quarkus 관련된 내용이 워낙 방대한데 그 중에서 Quarkus의 DI를 중심으로 글을 적어봤다. Quarkus에 처음 접하는 분에게 도움이 되면 좋겠다. 그리고 업무를 마치고 저녁 늦게, 주말에 틈틈히 시간내서 공부한 내용을 정리했는데, 도움이 안 되더라도 내 스스로에게는 뿌듯함이 남는 작업이었다.
