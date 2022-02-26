---
layout: post
title: "scala에서 resources 디렉터리 파일 사용하기"
categories: "programming"
---

## 목차

- [1. 개요](#1-개요)
- [2. 예제 코드](#2-예제-코드)
  - [2-1) json file 읽기](#2-1-json-file-읽기)
  - [2-2) hocon file 사용하기](#2-2-hocon-file-사용하기)
- [3. sbt에서 resource 디렉터리 변경하기](#3-sbt에서-resource-디렉터리-변경하기)
- [4. 참고 자료](#4-참고-자료)

## 1. 개요

sbt도 java maven의 디렉터리 구조를 사용한다. 따라서 `src/main/resources/` 디렉터리에 존재하는 파일은 자동으로 읽을 수 있다.

sbt를 이용하여 scala 언어에서 resources 디렉터리의 파일을 읽는 예제 코드를 작성해봤다.

실제 작동하는 코드는 [github repository](https://github.com/jason-heo/scala-working-with-resources-dir)에 등록해두었으니 참고바란다.

예제 코드에서는 단순히 파일을 읽는 것 이외에 hocon file의 `include()` 기능도 추가해두었다. hocon 파일에 대해서는 본인이 작성한 [HOCON 파일, pureconfig, pyhocon](/programming/2018/08/06/hocon.html) 포스팅을 참고하도록 한다.

## 2. 예제 코드

### 2-1) json file 읽기

```scala
// 1. 파일 읽기: classpath에 존재하는 파일
println(scala.io.Source.fromInputStream(getClass.getClassLoader.getResourceAsStream("conf.json")).mkString)

// 2. 파일 읽기: 절대 경로로 입력
//    위 방식과 차이점: getClassLoader()가 없다
println(scala.io.Source.fromInputStream(getClass.getResourceAsStream("/conf.json")).mkString)

// 3. 파일 경로 출력하기
val res = getClass.getResource("/conf.json")
println(res.getPath)
```

실행 결과에서 파일 내용이 출력되는 것은 당연하다.

`res.getPath`의 경로를 잘 봐두면 좋은데 IntelliJ 등 IDE에서 실행하면 경로가 아래처럼 출력된다.

`/path/to/project/target/scala-2.12/classes/conf.json`

fat jar에서 수행하면 다음과 같이 출력된다.

`file:/path/to/project/target/scala-2.12/example.jar!/conf.json`

즉, 소스 코드 상태의 resources 디렉터리와 상관없이 수행 시에는 build 경로로 이동하는 것을 볼 수 있다. 특히 assembly 이후에는 jar 안에 포함된다. 따라서 assembly 이후에는 원본 파일을 수정한다 하더라도 수정된 내용이 jar에 반영되지 않는다.

### 2-2) hocon file 사용하기

단순히 hocon file을 읽는 것은 쉬워서 `include()`가 제대로 작동하는 예제를 작성하였다.

```scala
val configUrl: URL = getClass.getClassLoader.getResource("app1.conf")

val myConfig: MyConfig = getConfigFromURL(configUrl)

println(myConfig)

private def getConfigFromURL(config: URL): MyConfig = {
  implicit def hint[T] = ProductHint[T](ConfigFieldMapping(CamelCase, CamelCase))

  pureconfig.loadConfigOrThrow[MyConfig](ConfigFactory.parseURL(config).resolve)
}
```

위 테스트에서는 다음과 같이 두 개의 파일을 사용하였다.

- `app1.conf`
    ```conf
    {
      name = my-program
      include classpath("include/inc.conf")
    }
    ```
- `include/inc.conf`
    ```conf
    {
      server {
        hostname = localhost
        port = 8080
      }
    }
    ```

실행 결과는 다음과 같다.

```
MyConfig(my-program,Server(localhost,8080))
```

## 3. sbt에서 resource 디렉터리 변경하기

기본 resources 디렉터리는 `src/main/resources/` 이다. `build.sbt`에 아래 내용을 추가하면 경로를 변경할 수 있다.

```
Compile / resourceDirectory := baseDirectory.value / "resources"
```

아래의 내용을 추가하면 임의의 경로를 resources에 추가할 수 있다.

```
Compile / unmanagedResourceDirectories += baseDirectory.value / "conf"
```

## 4. 참고 자료

- https://stackoverflow.com/a/42155532/2930152
- http://fruzenshtein.com/scala-working-with-resources-folders-files/
- https://www.scala-sbt.org/1.x/docs/Howto-Customizing-Paths.html
