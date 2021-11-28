---
layout: post
title: "Quarkus native image app 만들 때 tip"
categories: "programming"
---

Quarkus를 이용하여 native image app을 만들던 에러가 발생했다.

구글링해보면 금방 찾을 수 있는 내용이지만 정리 겸 블로그에 남겨본다.

좀더 자세한 내용은 Quarkus 공식 문서인 [TIPS FOR WRITING NATIVE APPLICATIONS](https://quarkus.io/guides/writing-native-applications-tips)에서 볼 수 있다.

### resource file을 native image에 포함시키기.

`src/main/resources/a.txt`라는 파일은 다음과 같은 Java code로 읽을 수 있다.

```
InputStream inputStream = getClass().getClassLoader().getResourceAsStream("a.txt");
```

그런데 native image로 빌드 후 실행해보면 '파일을 못찾는다'는 에러가 발생한다. 이유는 GraalVM은 기본적으로 resources file들을 native image에 포함하지 않기 때문이다.

해결 방법은 `src/main/resources/application.properties` 파일에 아래의 내용을 추가하면 된다.

```
quarkus.native.resources.includes=**
```

위 설정은 Quarkus의 설정이고, 내부적으로는 GraalVM 문서의 [Accessing Resources in Native Images](https://www.graalvm.org/reference-manual/native-image/Resources/)에 나온 것을 대신해줄 것 같다.

### Registering for reflection

이건 한글 제목이 마땅히 생각나지 않아서 [Quarkus의 제목](https://quarkus.io/guides/writing-native-applications-tips#registering-for-reflection)을 그대로 가져왔다.

GraalVM은 native image를 생성할 때 사용되지 않는 code는 제거한다.

따라서 Jackson ObjectMapper를 사용하는 경우 다음과 같은 에러 메시지를 보게 된다.

> com.fasterxml.jackson.databind.exc.InvalidDefinitionException: No serializer found for class org.acme.jsonb.Person and no properties discovered to create BeanSerializer (to avoid exception, disable SerializationFeature.FAIL_ON_EMPTY_BEANS)

이때는 class에 `@RegisterForReflection` annotation을 추가해주면 된다.

이것도 Quarkus에서 추가된 annotation이다.

GraalVM 문서의 [Reflection Use in Native Images](https://www.graalvm.org/reference-manual/native-image/Reflection/#manual-configuration)를 자동으로 해주는 Quarkus의 annotation이라 생각된다.
