---
layout: post
title: "MongoDB 위키피디아 번역"
date: 2011-04-16 21:34:00
categories: computer
---

MongoDB(humongous - '거대한'에서 유래)는 C++ 언어로 작성된 오픈 소스 데이터베이스로서, 고성능을 발휘하고 고정된 스키마로부터자유로운 문서 지향 데이터베이스이다. 복잡한 계층으로 구성된 BSON 문서의 컬렉션을 관리하며 질의 및 인덱스하기 쉽기 때문에 많은 응용에서 자기 자신의 타입과 구조에 맞고 자연스럽게 데이터를 저장하기 쉽다.

MongoDB는 2007년 10월에 10gen에서 개발이 시작되었고, 2009년 2월에 공개되었다.

## 특징

다음과 같은 특징이 있다.

- 일관된 UTF-8 인코딩 : UTF-8이 아닌 데이터도 저장이 되고 질의될 수 있으며, 특별한 바이너리 데이터 타입으로 값을 얻어올 수 있다.

- 다양한 플랫폼 지원 : Windows, Linux, OS X과 솔라리스의 바이너리 버전이 제공되고 있으며 거의 대부분의 리틀 엔디안(little-endian) 시스템에서 컴파일 할 수 있다.

- 다양한 타입 지원 : 날짜형, 정규식형, 코드, 바이너리 데이터 및 많은 데이터 타입을 지원한다.

- 질의 결과에 대한 커서를 지원한다.

## 임시 방편의 질의 (Ad hoc queries)

MongoDB에서는 언제든지 임의의 필드를 질의할 수 있다. MongoDB는 범위 질의, 정규식 검색, 정확 일치, 다른 특별한 질의 형식을 제공한다. 질의에서는 Javascript로 만들어진 사용자 정의 함수도 사용할 수 있다. (함수가 true를 반환하면 문서가 조건을 만족한 것으로 판단함)

질의는 문서의 특정 필드(문서 전체가 아닌)에 대해서 정렬, 제외, LIMIT을 한 뒤 반환할 수 있다.

## Nested된 필드에 대한 질의

질의를 통하여 Object와 배열에 접근할 수 있다. 만약 다음과 같은 문서가 users라는 컬랙션에 저장되었다면,

    {
        "username" : "bob", "address" : { "street" : "123 Main Street", "city" : "Springfield", "state" : "NY" }
    }

 다음과 같은 질의를 통하여 주소가 NY(New York)인 문서를 찾을 수 있다.

    > db.users.find({"address.state" : "NY"})

배열 원소는 다음과 같이 찾을 수 있다.

    > db.food.insert({"fruit" : ["peach", "plum", "pear"]})
    > db.food.find({"fruit" : "pear"})

## 인덱싱

MongoDB는 단일 키, 복합 키, 유일키, 공간 인덱스를 지원한다. nested된 필드 또한 인덱스화 할 수 있고 배열 타입에서는 배열 원소 각각을 인덱스화 할 수 있다.

MonoDB의 질의 최적기(Optimizer)는 질의가 수행될 때 주기적인 샘플링을 이용하여 각기 다른 방식의 질의 수행 계획을 시도해 보고 그중에 가장 빠른 계획을 사용한다. 개발자는 `explain` 함수를 이용하여 어떤 인덱스가 사용될 지 볼 수 있으며 `hint` 함수를 사용하여 다른 인덱스를 사용하라고 명시할 수 있다. 인덱스는 아무 때나 생성, 삭제 될 수 있다.

## 집합 (Aggregation)

임시 방편의 질의(ad-hoc query)에 부가적으로 MongoDB는 집합을 위한 도구와 MapReduce, 그룹 함수(SQL의 GROUP BY과 같은)을 제공한다.

## 파일 저장소

MongoDB는 Database로부터 파일을 저장하고 가져오기 위해 GridFS라는 프로토콜을 구현한다. 이 파일 저장소 기법은 NXINX와 lighttpd를 위한 플러그인에서 사용된 적이 있다.

## 서버 측 Javascript 실행

Javascript는 MongoDB의 공용어이며 질의, 집합 함수(MapReduce 같은)에서 사용될 수 있으며 database로 Javascript를 전송하여 바로  실행할 수도 있다.

질의에서의 Javascript 예:

    > db.foo.find({$where : function() { return this.x == this.y; }})

Database에 전송되어 바로 실행되는 Javascript 코드 예 ("Hello, Joe를 반환한다.)

    > db.eval(function(name) { return "Hello, "+name; }, ["Joe"])

Javascript 변수는 Database에 저장될 수 있으며 전역 변수로 다른 Javascript에서 사용될 수 있다. 함수, 객체 등 올바른 Javascript 형식은 MongoDB에 저장될 수 있으므로 Javascript는 저장 프로시저(Stored Procedure)로 사용될 수 있다.

## Capped 컬렉션

MongoDB는 고정 크기의 컬렉션을 지줭하며 이를 capped 컬렉션이라고 부른다. capped 컬렉션은 생성 시에 집합의 크기와 옵션으로 원소의 개수를 입력해서 생성된다. capped 컬렉션은 삽입 순서를 보장하는 유일한 방법이다. 지정된 크기에 도달하는 순간 capped 컬렉션은 순환 큐(circular queue)처럼 동작한다.

tailable 커서라고 불리는 특별한 커서는 capped 컬렉션에서 사용될 수 있다. "tail -f"와 비슷하기에 tailable 커서라고 불리며 질의가 결과를 모두 반환하더라도 종료하지 않고 문서가 capped collection에 입력이 되어 새로운 결과가 반환될 때까지 기다린다.

## 배포

MongoDB는 소스코드로부터 컴파일되어 설치될 수도 있지만 일반적으로 바이너리 패키지를 이용하여 설치한다. 요즘은 많은 리눅스 패키지 관리 시스템이 MongoDB 패키지를 포함하고 있다. CentOS, Fedora, Debian, Ubuntu, Gentoo, Arch Linux 등. 또한 공식 웹 사이트에서 구할 수도 있다.

MongoDB는 메모리 매핑된 파일을 사용한다. 따라서 32bit에서는 데이터 사이즈가 2GB로 제한된다. (64 bit 시스템에서는 훨씬 큼). MongoDB 서버는 리틀 엔디안 시스템에서만 실행할 수 있지만, 드라이버 프로그램은 상관없으므로 클라이언트는 리틀 엔디안과 빅 엔디안에서 모두 사용할 수 있다.

