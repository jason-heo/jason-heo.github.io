---
layout: post
title: "sbt multi project 예제"
categories: "programming"
---

## 목차

- [1. 개요](#1-개요)
- [2. 예제 project](#2-예제-project)
- [3. build.sbt 내용](#3-buildsbt-내용)
- [4. build 방법](#4-build-방법)
  - [4-1) 모든 project를 build하는 방법](#4-1-모든-project를-build하는-방법)
  - [4-2) 특정 1개 project build하는 방법](#4-2-특정-1개-project-build하는-방법)

## 1. 개요

간단하게 시작했던 프로젝트가 방대해지면서 경우 소스 코드 Repository를 어떻게 관리하는 게 좋을지 고민을 하게 된다.

1개 Repository에 모든 소스 코드를 저장하자니 fat jar의 size가 커지고 의존성 관리가 어려워진다. 그렇다고 기능 단위로 Repository를 나누자니 공통 라이브러리 소스 코드가 양쪽에 중복되어 저장된다.

이럴 때 사용할 수 있는 것이 sbt multi project 기능이다.

https://www.scala-sbt.org/1.x/docs/Multi-Project.html

각각의 project는 자체의 소스 코드를 가지며 far jar 파일도 project 단위로 생성된다. 하지만 project 서로 간의 소스 코드를 참조할 수 있으므로 앞의 고민을 해결해줄 수 있는 솔루션이다.

## 2. 예제 project

실제 작동하는 `build.sbt` 예제와 소스 코드를 github에 올려 두었다.

https://github.com/jason-heo/sbt-multi-project-example

위 예제는 총 3개의 project로 구성되어 있다.

1. `lib`: 라이브러리 함수
1. `client`: client 프로그램
1. `server`: server 프로그램

예제용 프로그램이기 때문에 단순히 `println()`만 하는 프로그램이다.

## 3. build.sbt 내용

```scala
ThisBuild / scalaVersion := "2.12.6"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.example"
ThisBuild / organizationName := "example"

lazy val lib = (project in file("lib"))
  .settings(
    libraryDependencies ++= commonDependencies,
    assemblyJarName in assembly := s"${name.value}.jar"
  )

lazy val client = (project in file("client"))
  .settings(
    libraryDependencies ++= commonDependencies ++ clientDependencies,
    assemblyJarName in assembly := s"${name.value}.jar"
  ).dependsOn(lib % "test->test;compile->compile")

lazy val server = (project in file("server"))
  .settings(
    libraryDependencies ++= commonDependencies ++ serverDependencies,
    assemblyJarName in assembly := s"${name.value}.jar"
  ).dependsOn(lib % "compile->compile")

lazy val commonDependencies = Seq(
  "org.scalatest" % "scalatest_2.12" % "3.0.5" % "test"
)

lazy val clientDependencies = Seq(
  "org.apache.httpcomponents" % "httpclient" % "4.5.3" % "provided"
)

lazy val serverDependencies = Seq(
  "org.apache.commons" % "commons-pool2" % "2.7.0"
)
```

`dependsOn()`을 이용하여 해당 project가 어떤 project의 의존적인지를 지정한다.

`client`의 경우 `lib % test->test`를 지정했는데 이것은 `lib` project의 test code도 참조하겠다는 것을 의미한다.

## 4. build 방법

### 4-1) 모든 project를 build하는 방법

아래와 같은 명령을 입력하는 경우 3개 project를 모두 build한다.

```console
$ sbt assembly
```

fat jar file은 아래와 같이 4개가 생성된다.

```console
$ find . -name "*.jar"
./target/scala-2.12/sbtmultiprojectexample-assembly-0.1.0-SNAPSHOT.jar
./server/target/scala-2.12/server.jar
./lib/target/scala-2.12/lib.jar
./client/target/scala-2.12/client.jar
```

### 4-2) 특정 1개 project build하는 방법

```console
$ sbt client/assembly
```

fat jar file은 client 1개만 생성된다.

```console
$ find . -name "*.jar"
./client/target/scala-2.12/client.jar
```
